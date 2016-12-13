package multitallented.plugins.heroscoreboard.listeners;

import java.io.File;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import multitallented.plugins.heroscoreboard.HeroScoreboard;
import multitallented.plugins.heroscoreboard.PlayerStatManager;
import multitallented.plugins.heroscoreboard.PlayerStats;
import net.milkbowl.vault.economy.Economy;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

/**
 *
 * @author Multitallented
 */
public class PvPListener implements Listener {
    private final HeroScoreboard plugin;
    private final PlayerStatManager psm;
    private final Map<Player, Long> lastKilled = new HashMap<Player, Long>();
    private final HashMap<String, HashSet<ItemStack>> itemsOnDeath = new HashMap<String, HashSet<ItemStack>>();

    public PvPListener(HeroScoreboard aThis, PlayerStatManager psm) {
        this.plugin=aThis;
        this.psm = psm;
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Player) || e.getDamage() < 1) {
            return;
        }
        Player player = (Player) e.getEntity();
        
        if (!(e instanceof EntityDamageByEntityEvent)) {
            return;
        }
        
        EntityDamageByEntityEvent edBy = (EntityDamageByEntityEvent) e;
        Entity damageSource = edBy.getDamager();
        ProjectileSource source = null;
        if (e.getCause() == DamageCause.PROJECTILE) {
            source = ((Projectile)damageSource).getShooter();
        }
        LivingEntity damager = getLivingEntity(damageSource, source);

        if (damager == null) {
            return;
        }

        psm.putDamagedPlayer(player);
        psm.setWhoDamaged(player, damager);
        psm.setCombat(player);
        if (damager instanceof Player) {
            psm.setCombat((Player) damager);
        }
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (itemsOnDeath.containsKey(player.getName())) {
            for (ItemStack is : itemsOnDeath.get(player.getName())) {
                player.getInventory().addItem(is);
            }
            itemsOnDeath.remove(player.getName());    
        }
    }

    private LivingEntity getLivingEntity(Entity source, ProjectileSource damageSource) {
        if (source != null && source instanceof LivingEntity) {
            return (LivingEntity) source;
        } else if (damageSource instanceof LivingEntity) {
            return (LivingEntity) damageSource;
        } else {
            return null;
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) e.getEntity();
        
        //Items on Death
        if (psm.getKeepItemsOnDeath()) {
            HashSet<ItemStack> deathItems = new HashSet<ItemStack>();
            if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
                deathItems.add(player.getItemInHand());
                player.getInventory().remove(player.getInventory().getHeldItemSlot());
                e.getDrops().remove(player.getInventory().getHeldItemSlot());
            }
            if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() != Material.AIR) {
                deathItems.add(player.getInventory().getHelmet());
                player.getInventory().remove(player.getInventory().getHelmet());
                e.getDrops().remove(player.getInventory().getHelmet());
            }
            if (player.getInventory().getChestplate()!= null && player.getInventory().getChestplate().getType() != Material.AIR) {
                deathItems.add(player.getInventory().getChestplate());
                player.getInventory().remove(player.getInventory().getChestplate());
                e.getDrops().remove(player.getInventory().getChestplate());
            }
            if (player.getInventory().getLeggings()!= null && player.getInventory().getLeggings().getType() != Material.AIR) {
                deathItems.add(player.getInventory().getLeggings());
                player.getInventory().remove(player.getInventory().getLeggings());
                e.getDrops().remove(player.getInventory().getLeggings());
            }
            if (player.getInventory().getBoots()!= null && player.getInventory().getBoots().getType() != Material.AIR) {
                deathItems.add(player.getInventory().getBoots());
                player.getInventory().remove(player.getInventory().getBoots());
                e.getDrops().remove(player.getInventory().getBoots());
            }
            itemsOnDeath.put(player.getName(), deathItems);
        }
        
        EntityDamageEvent event = e.getEntity().getLastDamageCause();
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return;
        }
        
        EntityDamageByEntityEvent edBy = (EntityDamageByEntityEvent) event;
        Entity damageSource = edBy.getDamager();
        ProjectileSource source = null;
        if (event.getCause() == DamageCause.PROJECTILE) {
            source = ((Projectile) damageSource).getShooter();
        }
        LivingEntity damager = getLivingEntity(damageSource, source);
        if (damager == null) {
            return;
        }


        psm.setWhoDamaged(player, damager);

        if (!(damager instanceof Player)) {
            return;
        }
        
        final Player dPlayer = (Player) damager;
        if (!HeroScoreboard.permission.has(player.getWorld().getName(), dPlayer.getName(), "heroscoreboard.participate")) {
            return;
        }

        //Delete homes nearby for the victim
        File essentialsPlayerFile = new File("plugins/Essentials/userdata/" + player.getUniqueId() + ".yml");
        if (essentialsPlayerFile.exists() && plugin.getConfig().getBoolean("delete-essentials-home-on-death", false)) {
            FileConfiguration essentialsPlayerConfig = new YamlConfiguration();
            try {
                essentialsPlayerConfig.load(essentialsPlayerFile);
            } catch (Exception ex) {
                System.out.println("[HeroScoreboard] failed to read " + player.getUniqueId() + ".yml from essentials");
            }
            ConfigurationSection homeSection = essentialsPlayerConfig.getConfigurationSection("homes");
            if (homeSection != null) {
                for (String key : homeSection.getKeys(false)) {
                    ConfigurationSection currentHomeSection = homeSection.getConfigurationSection(key);
                    Location homeLocation = null;
                    try {
                        homeLocation = new Location(Bukkit.getWorld(currentHomeSection.getString("world")),
                                currentHomeSection.getDouble("x"),
                                currentHomeSection.getDouble("y"),
                                currentHomeSection.getDouble("z"));
                    } catch (Exception ex) {
                        System.out.println("[HeroScoreboard] failed to read location for home " + key + " for player " + player.getName());
                    }
                    if (homeLocation == null) {
                        continue;
                    }
                    if (homeLocation.distanceSquared(player.getLocation()) < plugin.getHomeDeleteDistanceSquared()) {
                        boolean isOp = dPlayer.isOp();
                        if (!isOp) {
                            dPlayer.setOp(true);
                        }
                        dPlayer.performCommand("delhome " + player.getName() + ":" + key);
                        if (!isOp) {
                            dPlayer.setOp(false);
                        }
                        player.sendMessage(ChatColor.RED + "[HeroScoreboard] Your home " + key + " was destroyed by " + dPlayer.getDisplayName());
                        dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] You destroyed " + ChatColor.RED + player.getDisplayName() + ChatColor.GRAY + "'s home");
                    }
                }
            }
        }

        
        //Check if repeat kill
        if (lastKilled.containsKey(player) && (new Date()).getTime() - lastKilled.get(player) < psm.getRepeatKillCooldown()) {
            PlayerStats ps = psm.getPlayerStats(dPlayer.getName());
            if (ps == null) {
                ps = new PlayerStats();
            }
            if (ps.getKillstreak() < 1) {
                dPlayer.sendMessage(ChatColor.RED + "[HeroScoreboard] Repeat kill detected.");
                return;
            }
            ps.setKarma(ps.getKarma() - ps.getKillstreak());
            dPlayer.sendMessage(ChatColor.RED + "[HeroScoreboard] Repeat kill detected. -" + ChatColor.LIGHT_PURPLE + ps.getKillstreak() + ChatColor.RED + " karma");
            psm.setPlayerStats(dPlayer.getName(), ps);
            return;
        }
        
        lastKilled.put(player, new Date().getTime());
        //Check if level range too great
        /*if (hero != null && dHero.getTieredLevel(dHero.getHeroClass()) - hero.getTieredLevel(hero.getHeroClass()) > psm.getLevelRange()) {
            dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Level difference (" + (dHero.getLevel() - hero.getLevel()) + ") too large. No points given.");
            if (dHero.getHeroClass().hasExperiencetype(ExperienceType.PVP)) {
                dHero.setExperience(dHero.getHeroClass(), dHero.getExperience(dHero.getHeroClass()) - 
                        dHero.getHeroClass().getExpModifier()*HeroScoreboard.heroes.properties.playerKillingExp);
            }
            return;
        }*/
        
        //Drop any items in the PvPDrops list
        for (ItemStack is : psm.getPVPDrops()) {
            player.getWorld().dropItemNaturally(player.getLocation(), is);
        }
        
        double econBonus = 0;
        double econPay = 0;
        PlayerStats ps = psm.getPlayerStats(dPlayer.getName());
        if (ps == null) {
            ps = new PlayerStats();
        }
        ps.setKills(ps.getKills()+1);
        Economy econ = HeroScoreboard.econ;
        double econStolen = 0;
        if (econ != null) {
            double balance = econ.bankBalance(player.getName()).balance;
            if (balance > 0) {
                econStolen = Math.round(Math.max(0, balance * psm.getEconPercentStolen()));

                econPay = econStolen;
                econPay -= Math.min(balance - econStolen, psm.getEconBaseDrop());

                econBonus += econStolen;
                /*econBonus += balance;
                econPay = econBonus;
                if (balance - econPay > 0) {
                    econStolen = Math.round((balance - econPay) * psm.getEconPercentStolen());
                    econBonus += econStolen;
                    econPay = econBonus;
                }
                if (balance - econPay - psm.getEconBaseDrop() >0) {
                    econPay += psm.getEconBaseDrop();
                } else if (balance > 0) {
                    econPay = balance;
                }
                balance = econ.bankWithdraw(player.getName(), econPay).balance;
                if (balance >0) {
                    econPay = balance * psm.getEconPercentDrop();
                    econ.bankWithdraw(player.getName(), econPay);
                }
                econBonus += psm.getEconBase();*/


            }
        }
        final double finalEconStolen = econStolen;

        PlayerStats psv = psm.getPlayerStats(player.getName());
        if (psv == null) {
            psv = new PlayerStats();
        }

        if (dPlayer.getItemInHand() != null) {
            if (dPlayer.getItemInHand().getItemMeta() != null &&
                    dPlayer.getItemInHand().getItemMeta().getDisplayName() != null) {
                ps.addWeapon(dPlayer.getItemInHand().getItemMeta().getDisplayName());
            } else {
                ps.addWeapon(dPlayer.getItemInHand().getType().name().replace("_", " ").toLowerCase());
            }
        }

        //ps.addWeapon(dPlayer.getItemInHand().getType().name().replace("_", " ").toLowerCase());
        ps.addNemesis(player.getDisplayName());
        
        double killStreakBonus = psm.getPointBonusKillStreak() * ps.getKillstreak();
        ps.setKillstreak(ps.getKillstreak()+1);
        econBonus += ps.getKillstreak() * psm.getEconBonusKillStreak();
        final double finalKillstreakEcon = ps.getKillstreak() * psm.getEconBonusKillStreak();
        if (ps.getKillstreak() >= 3) {
            plugin.getServer().broadcastMessage(ChatColor.GRAY + "[HeroScoreboard] " + dPlayer.getDisplayName() + " is on a killstreak of " + ChatColor.RED + ps.getKillstreak());
        }
        double killJoyBonus = psm.getPointBonusKillJoy() * psv.getKillstreak();
        econBonus += psv.getKillstreak() * psm.getEconBonusKillJoy();
        final double finalKillJoyEcon = psv.getKillstreak() * psm.getEconBonusKillJoy();
        if (psv.getKillstreak() >= 3) {
            plugin.getServer().broadcastMessage(ChatColor.GRAY + "[HeroScoreboard] " + dPlayer.getDisplayName() + " just ended "
                    + player.getDisplayName() + "'s killstreak of " + ChatColor.RED + psv.getKillstreak());
        }
        if (ps.getHighestKillstreak() < ps.getKillstreak()) {
            ps.setHighestKillstreak(ps.getKillstreak());
        }
        
        double points = psm.getPointBase();
        double preTotalValuables = 0;
        points += killStreakBonus + killJoyBonus;
        EnumMap<Material, Double> pointValuables = psm.getPointValuables();
        EnumMap<Material, Double> econValuablesItems = psm.getEconValuables();
        double econValuables = 0;
        for (ItemStack is : player.getInventory().getContents()) {
            if (is != null && pointValuables.containsKey(is.getType()))
                preTotalValuables += pointValuables.get(is.getType());
            if (is != null && econValuablesItems.containsKey(is.getType()))
                econValuables += econValuablesItems.get(is.getType());
        }
        final double finalEconValuables = econValuables;
        econBonus += econValuables;
        points += preTotalValuables;
        
        double healthBonus = 0;
        if (dPlayer.getHealth() <= dPlayer.getMaxHealth() / 4) {
            healthBonus += psm.getPointQuarterHealth();
            econBonus += psm.getEconQuarterHealth();
        } else if (dPlayer.getHealth() <= dPlayer.getMaxHealth() / 2) {
            healthBonus += psm.getPointHalfHealth();
            econBonus += psm.getEconHalfHealth();
        }
        points += healthBonus;
        
        double pointLevelBonus = 0;

        //Stolen Points
        int pointsStolen = (int) Math.round(((double) ps.getPoints()) * psm.getPointsPercentStolen());
        final int finalPointsStolen = pointsStolen;
        points += pointsStolen;

        //Karma
        double karmaEcon = Math.max(0, -psm.getPricePerKarma() * ((double) (psv.getKarma() - ps.getKarma())));
        if (psv.getKarma() > 1) {
            karmaEcon = 0;
        }
        int karma = psm.getKarmaPerKill() + psm.getKarmaPerKillStreak() * (ps.getKillstreak() - psv.getKillstreak());
        ps.setKarma(ps.getKarma() - karma);
//        psv.setKarma(psv.getKarma() + karma);

        if (econ != null && karmaEcon != 0) {
            econ.bankWithdraw(player.getName(), karmaEcon);
            econ.bankDeposit(dPlayer.getName(), karmaEcon);
        }

        //pay econ bonus
        final double finalEconBonus = econBonus;
        if (econ != null) {
            if (econBonus != 0) {
                econ.bankDeposit(dPlayer.getName(), econBonus);
            }
            if (econPay != 0) {
                econ.bankWithdraw(player.getName(), econPay);
            }
        }
        //save points
        ps.setPoints(ps.getPoints() + points);


        /////////////////////////////////////////////////////////////////////////////////////
        //////SAVE PLAYER STATS//////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////
        psm.setPlayerStats(dPlayer.getName(), ps);
        psm.addPlayerStatsDeath(player.getName(), pointsStolen, karma);

        if (econPay != 0) {
            player.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Death: -" + ChatColor.RED + (psm.getPointLoss() + pointsStolen) + "pts, " + ChatColor.GREEN + econPay + " money lost");
        } else {
            player.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Death: -" + ChatColor.RED + (psm.getPointLoss() + pointsStolen) + "pts");
        }

        final double finalKillEcon = psm.getEconBase();

        //display points
        if (karma != 0) {
            if (karmaEcon == 0) {
                player.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Karma: +" + ChatColor.LIGHT_PURPLE + karma);
                dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Karma: -" + ChatColor.LIGHT_PURPLE + karma);
            } else {
                player.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Karma: +" + ChatColor.LIGHT_PURPLE + karma + ", " + ChatColor.GREEN + karmaEcon + " money lost");
                dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Karma: -" + ChatColor.LIGHT_PURPLE + karma + ", " + ChatColor.GREEN + karmaEcon + " money gained");
            }
        }
        long interval = 10L;
        if (points > 0) {
            final double pointBase = psm.getPointBase();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (finalKillEcon == 0) {
                    dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Kill: +" + ChatColor.RED + pointBase + "pts");
                } else {
                    dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Kill: +" + ChatColor.RED + pointBase + "pts, " + ChatColor.GREEN + finalKillEcon + " money gained");
                }

            }
            }, interval);
            interval += 10L;
        }
        if (pointsStolen > 0) {
            final double finalpointsStolen = pointsStolen;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (finalEconStolen == 0) {
                    dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Steal: +" + ChatColor.RED + finalPointsStolen + "pts");
                } else {
                    dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Steal: +" + ChatColor.RED + finalPointsStolen + "pts, " + ChatColor.GREEN + finalEconStolen + " money gained");
                }

            }
            }, interval);
            interval += 10L;
        }
        if (preTotalValuables > 0) {
            final double pointValuable = preTotalValuables;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (finalEconValuables == 0) {
                    dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Enemy Items: +" + ChatColor.RED + pointValuable + "pts");
                } else {
                    dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Enemy Items: +" + ChatColor.RED + pointValuable + "pts, " + ChatColor.GREEN + finalEconValuables + " money gained");
                }
            }
            }, 10L);
            interval += 10L;
        }
        if (pointLevelBonus > 0) {
            final double pts = pointLevelBonus;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Level Difference: +" + ChatColor.RED + pts + "pts");
            }
            }, interval);
            interval += 10L;
        }
        if (healthBonus > 0) {
            final double ptsHealth = healthBonus;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Low Health: +" + ChatColor.RED + ptsHealth + "pts");
            }
            }, interval);
            interval += 10L;
        }
        if (killStreakBonus > 0) {
            final double pts = killStreakBonus;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (finalKillstreakEcon == 0) {
                    dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] KillStreak: +" + ChatColor.RED + pts + "pts");
                } else {
                    dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] KillStreak: +" + ChatColor.RED + pts + "pts, " + ChatColor.GREEN + finalKillstreakEcon + " money gained");
                }
            }
            }, interval);
            interval += 10L;
        }
        if (killJoyBonus > 0) {
            final double pts = killJoyBonus;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (finalKillJoyEcon == 0) {
                    dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] KillJoy: +" + ChatColor.RED + pts + "pts");
                } else {
                    dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] KillJoy: +" + ChatColor.RED + pts + "pts, " + ChatColor.GREEN + finalKillJoyEcon + " money gained");
                }
            }
            }, interval);
            interval += 10L;
        }
        final double pts = points;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
        @Override
        public void run() {
            if (finalEconBonus == 0) {
                dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Total: +" + ChatColor.RED + pts + "pts");
            } else {
                dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Total: +" + ChatColor.RED + pts + "pts, " + ChatColor.GREEN + finalEconBonus + " money gained");
            }
        }
        }, interval);
        
    }
    
}
