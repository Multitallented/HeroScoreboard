package multitallented.plugins.heroscoreboard;

import multitallented.plugins.heroscoreboard.listeners.PvPListener;
import com.herocraftonline.dev.heroes.Heroes;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import multitallented.plugins.heroscoreboard.listeners.PluginListener;
import multitallented.plugins.heroscoreboard.listeners.SkillListener;
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
        pm.registerEvent(Type.CUSTOM_EVENT, new SkillListener(playerStatManager), Priority.Monitor, this);
        
        System.out.println("[HeroScoreboard] is now enabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length > 1 && (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("who"))) {
            Player p = this.getServer().getPlayer(args[1]);
            if (p != null) {
                PlayerStats ps = playerStatManager.getPlayerStats(p.getName());
                if (ps != null) {
                    String message = ChatColor.GRAY + "[HeroScoreboard] " + p.getName();
                    message += " K:" + ChatColor.RED + ps.getKills() + ChatColor.GRAY;
                    message += " D:" + ChatColor.RED + ps.getDeaths() + ChatColor.GRAY;
                    message += " K/D:" + ChatColor.RED + NumberFormat.getPercentInstance().format(ps.getKills() / (ps.getDeaths()==0 ? 1 : ps.getDeaths())) + ChatColor.GRAY;
                    message += " pts:" + ChatColor.RED + (int) ps.getPoints() + ChatColor.GRAY;
                    message += " weapon:" + ChatColor.RED + ps.getWeapon() + ChatColor.GRAY;
                    message += " skill:" + ChatColor.RED + ps.getSkill() + ChatColor.GRAY;
                    message += " nemesis:" + ChatColor.RED + ps.getNemesis() + ChatColor.GRAY;
                    sender.sendMessage(message);
                    return true;
                }
            }
            OfflinePlayer op = this.getServer().getOfflinePlayer(args[1]);
            if (op != null) {
                PlayerStats ps = playerStatManager.getPlayerStats(op.getName());
                if (ps != null) {
                    String message = ChatColor.GRAY + "[HeroScoreboard] " + p.getName();
                    message += " K:" + ChatColor.RED + ps.getKills() + ChatColor.GRAY;
                    message += " D:" + ChatColor.RED + ps.getDeaths() + ChatColor.GRAY;
                    message += " K/D:" + ChatColor.RED + NumberFormat.getPercentInstance().format(ps.getKills() / (ps.getDeaths()==0 ? 1 : ps.getDeaths())) + ChatColor.GRAY;
                    message += " pts:" + ChatColor.RED + (int) ps.getPoints() + ChatColor.GRAY;
                    message += " weapon:" + ChatColor.RED + ps.getWeapon() + ChatColor.GRAY;
                    message += " skill:" + ChatColor.RED + ps.getSkill() + ChatColor.GRAY;
                    message += " nemesis:" + ChatColor.RED + ps.getNemesis() + ChatColor.GRAY;
                    sender.sendMessage(message);
                    return true;
                }
            }
            sender.sendMessage(ChatColor.GRAY + "[HeroScoreboard] Could not find a player by the name of " + args[1]);
            return true;
        } else if (args.length > 0 && (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("top"))) {
            int pageNumber = 1;
            if (args.length > 1) {
                try {
                    pageNumber = Integer.parseInt(args[1]);
                    pageNumber = pageNumber > 0 ? pageNumber : 1;
                } catch (Exception e) {
                    pageNumber = 1;
                }
            }
            List<PlayerStats> topPlayers = new ArrayList<PlayerStats>();
            List<String> players = new ArrayList<String>();
            for (String s : playerStatManager.getPlayerStatKeys()) {
                if (topPlayers.isEmpty()) {
                    players.add(s);
                    topPlayers.add(playerStatManager.getPlayerStats(s));
                } else {
                    PlayerStats currentStats = playerStatManager.getPlayerStats(s);
                    for (int i=pageNumber * 8 -1; i >= 0; i--) {
                        if (topPlayers.size()-1 >= i && topPlayers.get(i).getPoints() >= currentStats.getPoints()) {
                            if (i == pageNumber * 8 -1)
                                break;
                            if (topPlayers.size() >= pageNumber * 8) {
                                topPlayers.remove(topPlayers.size()-1);
                                players.remove(players.size()-1);
                            }
                            if (topPlayers.size() <= i+1) {
                                topPlayers.add(currentStats);
                                players.add(s);
                            } else {
                                topPlayers.add(i+1, currentStats);
                                players.add(i+1, s);
                            }
                            break;
                        } else if (i==0) {
                            if (topPlayers.size() >= topPlayers.size()-1) {
                                topPlayers.remove(topPlayers.size()-1);
                                players.remove(players.size()-1);
                            }
                            topPlayers.add(0, currentStats);
                            players.add(0, s);
                        }
                    }
                }
            }
            sender.sendMessage(ChatColor.GRAY + "[HeroStronghold] Top Players Page " + pageNumber);
            for (int i=(pageNumber-1) * 8; i < topPlayers.size(); i++) {
                PlayerStats ps = topPlayers.get(i);
                sender.sendMessage(ChatColor.GRAY + "" + (i+1) + ". " + ChatColor.RED + players.get(i) + ChatColor.GRAY + " K:" + ChatColor.RED + ps.getKills() + ChatColor.GRAY +
                        " D:" + ChatColor.RED + ps.getDeaths() + ChatColor.GRAY
                        + " K/D:" + ChatColor.RED + NumberFormat.getPercentInstance().format(ps.getKills() / (ps.getDeaths()==0 ? 1 : ps.getDeaths())) + ChatColor.GRAY + 
                        " pts:" + ChatColor.RED + ((int) ps.getPoints()));
            }
            return true;
        }
        sender.sendMessage(ChatColor.GRAY + "[HeroScoreboard] by Multitallented v0.1");
        sender.sendMessage(ChatColor.GRAY + "1. /heroscore list <pagenumber> (shows a list of the top players)");
        sender.sendMessage(ChatColor.GRAY + "2. /heroscore who <playername> (Shows stats of that playername)");
        return true;
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
