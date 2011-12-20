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
    
    @Override
    public void onDisable() {
        // TODO: Place any custom disable code here.
        System.out.println(this + " is now disabled!");
    }

    @Override
    public void onEnable() {
        new PluginListener(this);
        
        //setup configs
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
        
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvent(Type.ENTITY_DAMAGE, new PvPListener(this), Priority.Monitor, this);
        
        System.out.println(this + " is now enabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
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
