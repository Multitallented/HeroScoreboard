package multitallented.plugins.heroscoreboard.listeners;

import com.herocraftonline.dev.heroes.Heroes;
import multitallented.plugins.heroscoreboard.HeroScoreboard;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Multitallented
 */
public class PluginListener extends ServerListener {
    private final HeroScoreboard plugin;
    
    public PluginListener(HeroScoreboard plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();

        if (name.equals("Heroes")) {
            System.out.println("[HeroScoreboard] " + name + " has been disabled! No longer hooking into Heroes.");
            plugin.disableHeroes();
        } else if (name.equals("iConomy") || name.equals("BOSEconomy") || name.equals("Essentials")) {
            System.out.println("[HeroScoreboard] is no longer hooked into " + name);
            plugin.econ = null;
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();
        PluginManager pm = this.plugin.getServer().getPluginManager();
        
        if (name.equals("Heroes")) {
            plugin.enableHeroes((Heroes) currentPlugin);
        } else if (name.equals("Vault") && (pm.isPluginEnabled("iConomy") || pm.isPluginEnabled("BOSEconomy") || pm.isPluginEnabled("Essentials"))
                && plugin.econ == null) {
            this.plugin.setupEconomy();
        } else if ((name.equals("iConomy") || name.equals("BOSEconomy") || name.equals("Essentials")) && pm.isPluginEnabled("Vault")
                && plugin.econ == null) {
            this.plugin.setupEconomy();
        }
    }
}