package multitallented.plugins.heroscoreboard;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multitallented
 */

public class PlayerStatManager {
    private final HeroScoreboard plugin;
    private Map<String, PlayerStats> playerStats = new HashMap<String, PlayerStats>();
    private List<String> ignoredSkills;
    private int levelRange;
    private long killCooldown;
    private List<ItemStack> pvpDrops;
    private double pointBase;
    private EnumMap<Material, Double> pointValuables;
    private double pointHalfHealth;
    private double pointQuarterHealth;
    private double pointBonusKillStreak;
    private double pointBonusKillJoy;
    private double pointBonusLevel;
    private double econBase;
    private double econPercentStolen;
    private double econBaseDrop;
    private double econPercentDrop;
    private EnumMap<Material, Double> econValuables;
    private double econHalfHealth;
    private double econQuarterHealth;
    private double econBonusKillStreak;
    private double econBonusKillJoy;
    private double econBonusLevel;
    private double pointLoss;
    private double econBaseStolen;
    private final int combatTagDuration;
    private final boolean useLogoutPenalty;
    private Map<Player, Long> damageMap = new HashMap<Player, Long>();
    private Map<Player, LivingEntity> whoDamagedMap = new HashMap<Player, LivingEntity>();
    private double percentPenalty;
    private boolean itemsOnDeath = false;
    
    public PlayerStatManager(HeroScoreboard plugin, FileConfiguration config) {
        this.plugin = plugin;
        
        percentPenalty = config.getInt("percent-health-penalty");
        percentPenalty = percentPenalty / 100;
        useLogoutPenalty = config.getBoolean("use-logout-penalty");
        combatTagDuration = config.getInt("combat-tag-duration") * 1000;
        ignoredSkills = (List<String>) config.getStringList("ignored-skills");
        levelRange = config.getInt("level-range");
        killCooldown = config.getInt("repeat-kill-cooldown") * 1000;
        pvpDrops = processItemStackList(config.getStringList("pvp-drops"));
        killCooldown = killCooldown < 1000 ? 1000 : killCooldown;
        
        pointBase = config.getDouble("points.base-reward");
        pointValuables = processEnumMap(config.getStringList("points.valuable-items"));
        pointHalfHealth = config.getDouble("points.half-health-bonus");
        pointQuarterHealth = config.getDouble("points.quarter-health-bonus");
        pointBonusKillStreak = config.getDouble("points.bonus-per-killstreak");
        pointBonusKillJoy = config.getDouble("points.bonus-per-killjoy");
        pointBonusLevel = config.getDouble("points.bonus-per-level");
        pointLoss = config.getDouble("points.points-lost-on-death");
        
        econBase = config.getDouble("economy.base-reward");
        econBaseStolen = config.getDouble("economy.base-stolen");
        econPercentStolen = config.getDouble("economy.percent-stolen");
        econBaseDrop = config.getDouble("economy.base-dropped");
        econPercentDrop = config.getDouble("economy.percent-dropped");
        econValuables = processEnumMap(config.getStringList("economy.valuable-items"));
        econHalfHealth = config.getDouble("economy.half-health-bonus");
        econQuarterHealth = config.getDouble("economy.quarter-health-bonus");
        econBonusKillStreak = config.getDouble("economy.bonus-per-killstreak");
        econBonusKillJoy = config.getDouble("economy.bonus-per-killjoy");
        econBonusLevel = config.getDouble("economy.bonus-per-level");
        itemsOnDeath = config.getBoolean("keep-items-on-death", false);
        
        File playerFolder = new File(plugin.getDataFolder(), "data"); // Setup the Data Folder if it doesn't already exist
        playerFolder.mkdirs();
        FileConfiguration dataConfig = null;
        for (File dataFile : playerFolder.listFiles()) {
            try {
                //Load saved region data
                dataConfig = new YamlConfiguration();
                dataConfig.load(dataFile);
                PlayerStats ps = new PlayerStats();
                ps.setNemesis(processListMap(dataConfig.getStringList("nemeses")));
                ps.setSkill(processListMap(dataConfig.getStringList("skills")));
                ps.setWeapon(processListMap(dataConfig.getStringList("weapons")));
                ps.setDeaths(dataConfig.getInt("deaths"));
                ps.setKills(dataConfig.getInt("kills"));
                ps.setPoints(dataConfig.getDouble("points"));
                ps.setKillstreak(dataConfig.getInt("killstreak"));
                ps.setHighestKillstreak(dataConfig.getInt("highest-killstreak"));
                playerStats.put(dataFile.getName().replace(".yml", ""), ps);
            } catch (Exception e) {
                System.out.println("[HeroScoreboard] failed to load data from " + dataFile.getName());
                e.printStackTrace();
            }
        }
    }
    
    private List<ItemStack> processItemStackList(List<String> list) {
        ArrayList<ItemStack> tempArray = new ArrayList<ItemStack>();
        if (list == null)
            return tempArray;
        for (String s : list) {
            try {
                String[] params = s.split(",");
                tempArray.add(new ItemStack(Material.getMaterial(params[0]), Integer.parseInt(params[1])));

            } catch (Exception e) {

            }
        }
        return tempArray;
    }
    
    private EnumMap<Material, Double> processEnumMap(List<String> list) {
        EnumMap<Material, Double> tempMap = new EnumMap<Material, Double>(Material.class);
        if (list == null)
            return tempMap;
        for (String s : list) {
            try {
                String[] params = s.split(",");
                tempMap.put(Material.getMaterial(params[0]), Double.parseDouble(params[1]));
            } catch (Exception e) {
                
            }
        }
        return tempMap;
    }
    
    private HashMap<String, Integer> processListMap(List<String> list) {
        HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
        if (list == null)
            return tempMap;
        for (String s : list) {
            try {
                String[] args = s.split(",");
                //TODO fix this NPE
                tempMap.put(args[0], Integer.parseInt(args[1]));
            } catch (Exception e) {
                //:P
            }
        }
        return tempMap;
    }
    
    public double getPercentHealthPenalty() {
        return percentPenalty;
    }
    
    public void setWhoDamaged(Player p, LivingEntity le) {
        whoDamagedMap.put(p, le);
    }
    
    public boolean getKeepItemsOnDeath() {
        return itemsOnDeath;
    }
    
    public LivingEntity getWhoDamaged(Player p) {
        if (!whoDamagedMap.containsKey(p)) {
            return null;
        }
        return whoDamagedMap.get(p);
    }
    
    public void putDamagedPlayer(Player p) {
        damageMap.put(p, System.currentTimeMillis());
    }
    
    public long getLoggingPlayer(Player p) {
        if (!damageMap.containsKey(p)) {
            return -1;
        }
        return damageMap.get(p);
    }
    
    public boolean getUseCombatTag() {
        return useLogoutPenalty;
    }
    
    public int getCombatTagDuration() {
        return combatTagDuration;
    }
    
    public PlayerStats getPlayerStats(String name) {
        if (playerStats.containsKey(name)) {
            return playerStats.get(name);
        } else {
            return null;
        }
    }
    public void setPlayerStats(String name, PlayerStats ps) {
        playerStats.put(name, ps);
        File dataFile =  new File(plugin.getDataFolder() + "/data", name + ".yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {
                System.out.println("[HeroScoreboard] could not create file: " + name + ".yml");
                return;
            }
        }
        FileConfiguration dataConfig = new YamlConfiguration();
        try {
            dataConfig.load(dataFile);
        } catch (Exception ex) {
            System.out.println("[HeroScoreboard] could not load file: " + name + ".yml");
            return;
        }
        dataConfig.set("kills", ps.getKills());
        dataConfig.set("deaths", ps.getDeaths());
        dataConfig.set("killstreak", ps.getKillstreak());
        dataConfig.set("points", ps.getPoints());
        dataConfig.set("weapons", ps.getWeapons());
        dataConfig.set("skills", ps.getSkills());
        dataConfig.set("nemeses", ps.getNemeses());
        dataConfig.set("highest-killstreak", ps.getHighestKillstreak());
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            System.out.println("[HeroScoreboard] could not save file: " + name + ".yml");
            return;
        }
    }
    public void addPlayerStatsDeath(String name) {
        File dataFile =  new File(plugin.getDataFolder() + "/data", name + ".yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {
                System.out.println("[HeroScoreboard] could not create file: " + name + ".yml");
                return;
            }
        }
        FileConfiguration dataConfig = new YamlConfiguration();
        try {
            dataConfig.load(dataFile);
        } catch (Exception ex) {
            System.out.println("[HeroScoreboard] could not load file: " + name + ".yml");
            return;
        }
        if (playerStats.containsKey(name)) {
            PlayerStats ps = playerStats.get(name);
            ps.setDeaths(ps.getDeaths()+1);
            ps.setKillstreak(0);
            ps.setPoints(ps.getPoints() - pointLoss);
            playerStats.put(name, ps);
            dataConfig.set("deaths", ps.getDeaths());
            dataConfig.set("killstreak", 0);
            dataConfig.set("points", ps.getPoints());
        } else {
            PlayerStats ps = new PlayerStats();
            ps.setDeaths(1);
            dataConfig.set("kills", 0);
            dataConfig.set("deaths", 1);
            dataConfig.set("killstreak", 0);
            dataConfig.set("points", -pointLoss);
            dataConfig.set("weapons", new ArrayList<String>());
            dataConfig.set("skills", new ArrayList<String>());
            dataConfig.set("nemeses", new ArrayList<String>());
            playerStats.put(name, ps);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            System.out.println("[HeroScoreboard] could not save file: " + name + ".yml");
            return;
        }
    }
    public void addSkill(String name, String skillName) {
        File dataFile =  new File(plugin.getDataFolder() + "/data", name + ".yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {
                System.out.println("[HeroScoreboard] could not create file: " + name + ".yml");
                return;
            }
        }
        FileConfiguration dataConfig = new YamlConfiguration();
        try {
            dataConfig.load(dataFile);
        } catch (Exception ex) {
            System.out.println("[HeroScoreboard] could not load file: " + name + ".yml");
            return;
        }
        if (playerStats.containsKey(name)) {
            PlayerStats ps = playerStats.get(name);
            ps.addSkill(skillName);
            playerStats.put(name, ps);
            dataConfig.set("skills", ps.getSkill());
        } else {
            ArrayList<String> tempArray = new ArrayList<String>();
            tempArray.add(skillName);
            PlayerStats ps = new PlayerStats();
            ps.addSkill(skillName);
            dataConfig.set("kills", 0);
            dataConfig.set("deaths", 0);
            dataConfig.set("killstreak", 0);
            dataConfig.set("points", 0);
            dataConfig.set("weapons", new ArrayList<String>());
            dataConfig.set("skills", tempArray);
            dataConfig.set("nemeses", new ArrayList<String>());
            playerStats.put(name, ps);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            System.out.println("[HeroScoreboard] could not save file: " + name + ".yml");
            return;
        }
    }
    
    public Set<String> getPlayerStatKeys() {
        return playerStats.keySet();
    }
    public List<String> getIgnoredSkills() {
        return this.ignoredSkills;
    }
    public boolean containsIgnoredSkill(String skillname) {
        return ignoredSkills.contains(skillname);
    }
    public int getLevelRange() {
        return this.levelRange;
    }
    public long getRepeatKillCooldown() {
        return this.killCooldown;
    }
    public List<ItemStack> getPVPDrops() {
        return pvpDrops;
    }
    public EnumMap<Material, Double> getPointValuables() {
        return this.pointValuables;
    }
    public double getPointHalfHealth() {
        return this.pointHalfHealth;
    }
    public double getPointQuarterHealth() {
        return this.pointQuarterHealth;
    }
    public double getPointBonusKillStreak() {
        return this.pointBonusKillStreak;
    }
    public double getPointBonusKillJoy() {
        return this.pointBonusKillJoy;
    }
    public double getPointBase() {
        return this.pointBase;
    }
    public double getPointBonusLevel() {
        return this.pointBonusLevel;
    }
    public double getPointLoss() {
        return this.pointLoss;
    }
    public double getEconBase() {
        return this.econBase;
    }
    public double getEconBaseStolen() {
        return this.econBaseStolen;
    }
    public double getEconPercentStolen() {
        return this.econPercentStolen;
    }
    public double getEconBaseDrop() {
        return this.econBaseDrop;
    }
    public double getEconPercentDrop() {
        return this.econPercentDrop;
    }
    public double getEconBonusKillJoy() {
        return this.econBonusKillJoy;
    }
    public double getEconBonusKillStreak() {
        return this.econBonusKillStreak;
    }
    public double getEconBonusLevel() {
        return this.econBonusLevel;
    }
    public double getEconHalfHealth() {
        return this.econHalfHealth;
    }
    public double getEconQuarterHealth() {
        return this.econQuarterHealth;
    }
    public EnumMap<Material, Double> getEconValuables() {
        return this.econValuables;
    }
}
