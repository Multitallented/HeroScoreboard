package multitallented.plugins.heroscoreboard.listeners;

import com.herocraftonline.heroes.api.events.SkillUseEvent;
import multitallented.plugins.heroscoreboard.HeroScoreboard;
import multitallented.plugins.heroscoreboard.PlayerStatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class SkillListener implements Listener {
    private final PlayerStatManager psm;
    public SkillListener(PlayerStatManager psm) {
        this.psm = psm;
    }
    
    @EventHandler
    public void onCustomEvent(SkillUseEvent event) {
        if (HeroScoreboard.heroes == null) {
            return;
        }
        
        if (event.isCancelled() || psm.containsIgnoredSkill(event.getSkill().getName()))
            return;
        
        Player player = event.getPlayer();
        psm.addSkill(player.getName(), event.getSkill().getName());
    }
}
