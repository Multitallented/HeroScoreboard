package multitallented.plugins.heroscoreboard;

import multitallented.plugins.heroscoreboard.listeners.PvPListener;
import com.herocraftonline.dev.heroes.Heroes;
import java.text.NumberFormat;
import multitallented.plugins.heroscoreboard.listeners.PluginListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroScoreboard extends JavaPlugin {
    public Economy econ;
    public Heroes heroes;
    protected FileConfiguration config;
    private PlayerStatManager playerStatManager;
    
    @Override
    public void onDisable() {
        //TODO write any disable code here
        System.out.println("[HeroScoreboard] is now disabled!");
    }

    @Override
    public void onEnable() {
        new PluginListener(this);
        
        //setup configs
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
        
        playerStatManager = new PlayerStatManager(this, config);
        
        //Register the pvp listener
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvent(Type.ENTITY_DAMAGE, new PvPListener(this, playerStatManager), Priority.Monitor, this);
        
        System.out.println("[HeroScoreboard] is now enabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length > 1 && args[0].equalsIgnoreCase("stats")) {
            Player p = this.getServer().getPlayer(args[1]);
            if (p != null) {
                PlayerStats ps = playerStatManager.getPlayerStats(p.getName());
                if (ps != null) {
                    String message = ChatColor.GRAY + "[HeroScoreboard] " + p.getName() + "= K:" + ps.getKills();
                    message += " D:" + ps.getDeaths() + " K/D: " + NumberFormat.getPercentInstance().format(ps.getKills() / ps.getDeaths());
                    message += " weapon:" + ps.getWeapon();
                    message += " skill:" + ps.getSkill();
                    message += " nemesis: " + ps.getNemesis();
                    sender.sendMessage(message);
                    return true;
                }
            }
            OfflinePlayer op = this.getServer().getOfflinePlayer(args[1]);
            if (op != null) {
                PlayerStats ps = playerStatManager.getPlayerStats(op.getName());
                if (ps != null) {
                    String message = ChatColor.GRAY + "[HeroScoreboard] " + p.getName() + "= K:" + ps.getKills();
                    message += " D:" + ps.getDeaths();
                    message += " K/D: " + NumberFormat.getPercentInstance().format(ps.getKills() / ps.getDeaths());
                    message += " pts: " + ps.getPoints();
                    message += " weapon:" + ps.getWeapon();
                    message += " skill:" + ps.getSkill();
                    message += " nemesis: " + ps.getNemesis();
                    sender.sendMessage(message);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Could not find a player by the name of " + args[1]);
            return true;
        }
        //TODO handle command /heroscore
        
        //TODO handle command /heroscore list pagenumber
        return false;
    }
    
    public void disableHeroes() {
        this.heroes = null;
    }
    public void enableHeroes(Heroes heroes) {
        this.heroes = heroes;
    }
    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
        }
        return econ != null;
    }
}
