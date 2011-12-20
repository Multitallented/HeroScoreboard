package multitallented.plugins.heroscoreboard;

import multitallented.plugins.heroscoreboard.listeners.PvPListener;
import com.herocraftonline.dev.heroes.Heroes;
import multitallented.plugins.heroscoreboard.listeners.PluginListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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
        // TODO: Place any custom disable code here.
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
        //TODO handle command /heroscore
        
        //TODO handle command /heroscore list pagenumber
        
        //TODO handle command /heroscore stats playername
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
