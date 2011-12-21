package multitallented.plugins.heroscoreboard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
    
    public PlayerStatManager(HeroScoreboard plugin, FileConfiguration config) {
        this.plugin = plugin;
        
        ignoredSkills = (List<String>) config.getStringList("ignored-skills");
        levelRange = config.getInt("level-range");
        killCooldown = config.getInt("repeat-kill-cooldown") * 1000;
        pvpDrops = processItemStackList(config.getStringList("pvp-drops"));
        
        pointBase = config.getDouble("points.base-reward");
        pointValuables = processEnumMap(config.getStringList("points.valuable-items"));
        pointHalfHealth = config.getDouble("points.half-health-bonus");
        pointQuarterHealth = config.getDouble("points.quarter-health-bonus");
        pointBonusKillStreak = config.getDouble("points.bonus-per-killstreak");
        pointBonusKillJoy = config.getDouble("points.bonus-per-killjoy");
        pointBonusLevel = config.getDouble("points.bonus-per-level");
        
        econBase = config.getDouble("economy.base-reward");
        econPercentStolen = config.getDouble("economy.percent-stolen");
        econBaseDrop = config.getDouble("economy.base-dropped");
        econPercentDrop = config.getDouble("economy.percent-dropped");
        econValuables = processEnumMap(config.getStringList("economy.valuable-items"));
        econHalfHealth = config.getDouble("economy.half-health-bonus");
        econQuarterHealth = config.getDouble("economy.quarter-health-bonus");
        econBonusKillStreak = config.getDouble("economy.bonus-per-killstreak");
        econBonusKillJoy = config.getDouble("economy.bonus-per-killjoy");
        econBonusLevel = config.getDouble("economy.bonus-per-level");
        
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
                playerStats.put(dataFile.getName().replace(".yml", ""), ps);
            } catch (Exception e) {
                System.out.println("[HeroScoreboard] failed to load data from " + dataFile.getName());
                System.out.println(e.getStackTrace());
            }
        }
    }
    
    private List<ItemStack> processItemStackList(List<String> list) {
        ArrayList<ItemStack> tempArray = new ArrayList<ItemStack>();
        for (String s : list) {
            String[] params = s.split(":");
            tempArray.add(new ItemStack(Material.getMaterial(params[0]), Integer.parseInt(params[1])));
        }
        return tempArray;
    }
    
    private EnumMap<Material, Double> processEnumMap(List<String> list) {
        EnumMap<Material, Double> tempMap = new EnumMap<Material, Double>(Material.class);
        for (String s : list) {
            String[] params = s.split(":");
            tempMap.put(Material.getMaterial(params[0]), Double.parseDouble(params[1]));
        }
        return tempMap;
    }
    
    private HashMap<String, Integer> processListMap(List<String> list) {
        HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
        for (String s : list) {
            String[] args = s.split(":");
            tempMap.put(args[0], Integer.parseInt(args[1]));
        }
        return tempMap;
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
        dataConfig.set("weapons", ps.getWeapon());
        dataConfig.set("skills", ps.getSkill());
        dataConfig.set("nemeses", ps.getNemesis());
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            System.out.println("[HeroScoreboard] could not save file: " + name + ".yml");
            return;
        }
    }
    
    public List<String> getIgnoredSkills() {
        return this.ignoredSkills;
    }
    public int getLevelRange() {
        return this.levelRange;
    }
    public long getRepeatKillCooldown() {
        return this.killCooldown;
    }
}
