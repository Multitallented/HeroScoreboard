package multitallented.plugins.heroscoreboard;

import org.bukkit.plugin.java.JavaPlugin;

public class Heroscoreboard extends JavaPlugin {
    @Override
    public void onDisable() {
        // TODO: Place any custom disable code here.
        System.out.println(this + " is now disabled!");
    }

    @Override
    public void onEnable() {
        // TODO: Place any custom enable code here, such as registering events
        
        System.out.println(this + " is now enabled!");
    }
}
