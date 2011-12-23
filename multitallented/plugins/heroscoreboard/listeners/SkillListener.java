package multitallented.plugins.heroscoreboard.listeners;

import com.herocraftonline.dev.heroes.api.SkillUseEvent;
import multitallented.plugins.heroscoreboard.HeroScoreboard;
import multitallented.plugins.heroscoreboard.PlayerStatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public class SkillListener extends CustomEventListener {
    private final PlayerStatManager psm;
    public SkillListener(PlayerStatManager psm) {
        this.psm = psm;
    }
    
    @Override
    public void onCustomEvent(Event event) {
        if (HeroScoreboard.heroes == null || !(event instanceof SkillUseEvent))
            return;
        
        SkillUseEvent sue = (SkillUseEvent) event;
        if (sue.isCancelled() || psm.containsIgnoredSkill(sue.getSkill().getName()))
            return;
        
        Player player = sue.getPlayer();
        psm.addSkill(player.getName(), sue.getSkill().getName());
    }
}
