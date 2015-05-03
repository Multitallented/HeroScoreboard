package multitallented.plugins.heroscoreboard.listeners;

import multitallented.plugins.heroscoreboard.HeroScoreboard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Multitallented
 */
public class PluginListener implements Listener {
    private final HeroScoreboard plugin;
    
    public PluginListener(HeroScoreboard plugin) {
        this.plugin = plugin;
    }
}
