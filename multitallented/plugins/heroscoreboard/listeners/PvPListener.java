package multitallented.plugins.heroscoreboard.listeners;

import com.herocraftonline.dev.heroes.hero.Hero;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import multitallented.plugins.heroscoreboard.HeroScoreboard;
import multitallented.plugins.heroscoreboard.PlayerStatManager;
import multitallented.plugins.heroscoreboard.PlayerStats;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multitallented
 */
public class PvPListener extends EntityListener {
    private final HeroScoreboard plugin;
    private final PlayerStatManager psm;
    private final Map<Player, Long> lastKilled = new HashMap<Player, Long>();

    public PvPListener(HeroScoreboard aThis, PlayerStatManager psm) {
        this.plugin=aThis;
        this.psm = psm;
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageByEntityEvent) || event.getDamage() == 0 ||
                (HeroScoreboard.heroes != null && event.getDamage() < HeroScoreboard.heroes.getHeroManager().getHero((Player) event.getEntity()).getHealth()) ||
                (HeroScoreboard.heroes == null && ((Player) event.getEntity()).getHealth() > event.getDamage()))
            return;
        EntityDamageByEntityEvent edBy = (EntityDamageByEntityEvent) event;
        Entity damager = edBy.getDamager(); 
        if (event.getCause() == DamageCause.PROJECTILE) {
            damager = ((Projectile)damager).getShooter();
        }
        if (!(damager instanceof Player)) {
            return;
        }
        Player player = (Player) edBy.getEntity();
        Hero hero = null;
        if (HeroScoreboard.heroes != null)
            hero = HeroScoreboard.heroes.getHeroManager().getHero(player);
        final Player dPlayer = (Player) damager;
        if (!HeroScoreboard.permission.has(player.getWorld().getName(), dPlayer.getName(), "heroscoreboard.participate"))
            return;
        Hero dHero = null;
        if (hero != null)
            dHero = HeroScoreboard.heroes.getHeroManager().getHero(dPlayer);
        //Check if repeat kill
        if (lastKilled.containsKey(player) && (new Date()).getTime() - lastKilled.get(player) < psm.getRepeatKillCooldown())
            return;
        
        lastKilled.put(player, new Date().getTime());
        //Check if level range too great
        if (hero != null && dHero.getLevel() - hero.getLevel() > psm.getLevelRange()) {
            dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Level difference (" + (dHero.getLevel() - hero.getLevel()) + ") too large. No points given.");
            return;
        }
        
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
        if (econ != null) {
            double balance = econ.bankBalance(player.getName()).balance;
            if (balance < psm.getEconBaseStolen()) {
                if (balance > 0) {
                    econBonus += balance;
                    econPay = econBonus;
                }
                if (balance - econPay > 0) {
                    econBonus += (balance - econPay) * psm.getEconPercentStolen();
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
            }
            econBonus += psm.getEconBase();
        }
        
        PlayerStats psv = psm.getPlayerStats(player.getName());
        if (psv == null) {
            psv = new PlayerStats();
        }
        psv.setDeaths(psv.getDeaths()+1);
        
        ps.addWeapon(dPlayer.getItemInHand().getType().name().replace("_", " ").toLowerCase());
        ps.addNemesis(player.getDisplayName());
        
        double killStreakBonus = psm.getPointBonusKillStreak() * ps.getKillstreak();
        ps.setKillstreak(ps.getKillstreak()+1);
        econBonus += ps.getKillstreak() * psm.getEconBonusKillStreak();
        if (ps.getKillstreak() >= 3) {
            plugin.getServer().broadcastMessage(ChatColor.GRAY + "[HeroScoreboard] " + dPlayer.getDisplayName() + " is on a killstreak of " + ChatColor.RED + ps.getKillstreak());
        }
        double killJoyBonus = psm.getPointBonusKillJoy() * psv.getKillstreak();
        econBonus += psv.getKillstreak() * psm.getEconBonusKillJoy();
        if (psv.getKillstreak() >= 3) {
            plugin.getServer().broadcastMessage(ChatColor.GRAY + "[HeroScoreboard] " + dPlayer.getDisplayName() + " just ended "
                    + player.getDisplayName() + "'s killstreak of " + ChatColor.RED + psv.getKillstreak());
        }
        
        double points = psm.getPointBase();
        double preTotalValuables = 0;
        points += killStreakBonus + killJoyBonus;
        EnumMap<Material, Double> pointValuables = psm.getPointValuables();
        EnumMap<Material, Double> econValuables = psm.getEconValuables();
        for (ItemStack is : player.getInventory().getContents()) {
            if (is != null && pointValuables.containsKey(is.getType()))
                preTotalValuables += pointValuables.get(is.getType());
            if (is != null && econValuables.containsKey(is.getType()))
                econBonus += econValuables.get(is.getType());
        }
        points += preTotalValuables;
        
        double healthBonus = 0;
        if ((dHero == null && dPlayer.getHealth() <= 5)
                || (dHero != null && dHero.getHealth() <= dHero.getMaxHealth() / 4)) {
            healthBonus += psm.getPointQuarterHealth() + psm.getPointHalfHealth();
            econBonus += psm.getEconHalfHealth() + psm.getEconQuarterHealth();
        } else if ((dHero == null && dPlayer.getHealth() <= 10)
                || (dHero != null && dHero.getHealth() <= dHero.getMaxHealth() / 2)) {
            healthBonus += psm.getPointHalfHealth();
            econBonus += psm.getEconHalfHealth();
        }
        points += healthBonus;
        
        double pointLevelBonus = 0;
        if (hero != null) {
            int levelDifference = hero.getLevel() - dHero.getLevel();
            if (levelDifference > 0) {
                pointLevelBonus = levelDifference * psm.getPointBonusLevel();
                points += pointLevelBonus;
                econBonus += levelDifference * psm.getEconBonusLevel();
            }
        }
        //pay econ bonus
        if (econ != null)
            econ.bankDeposit(dPlayer.getName(), econBonus); 
        //save points
        ps.setPoints(ps.getPoints() + points);
        
        psm.setPlayerStats(dPlayer.getName(), ps);
        psm.addPlayerStatsDeath(player.getName());
        
        player.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Death: -" + ChatColor.RED + psm.getPointLoss() + "pts");
        
        //display points
        long interval = 10L;
        if (points > 0) {
            final double pointBase = points;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Kill: +" + ChatColor.RED + pointBase + "pts");

            }
            }, interval);
            interval += 10L;
        }
        if (preTotalValuables > 0) {
            final double pointValuable = preTotalValuables;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Enemy Items: +" + ChatColor.RED + pointValuable + "pts");
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
                dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] KillStreak: +" + ChatColor.RED + pts + "pts");
            }
            }, interval);
            interval += 10L;
        }
        if (killJoyBonus > 0) {
            final double pts = killJoyBonus;
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] KillJoy: +" + ChatColor.RED + pts + "pts");
            }
            }, interval);
            interval += 10L;
        }
        final double pts = points;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
        @Override
        public void run() {
            dPlayer.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Total: +" + ChatColor.RED + pts + "pts");
        }
        }, interval);
        
    }
    
}
