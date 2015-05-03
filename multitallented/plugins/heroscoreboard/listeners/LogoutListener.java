package multitallented.plugins.heroscoreboard.listeners;

import multitallented.plugins.heroscoreboard.HeroScoreboard;
import multitallented.plugins.heroscoreboard.PlayerStatManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Multitallented
 */
public class LogoutListener implements Listener {
    private final PlayerStatManager psm;
    public LogoutListener(PlayerStatManager psm) {
        this.psm = psm;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        try {
            long lastDamage = psm.getLoggingPlayer(player);
            int combatTagDuration = psm.getCombatTagDuration();
            LivingEntity le = psm.getWhoDamaged(player);
            if (lastDamage + combatTagDuration > System.currentTimeMillis() && (le == null || !le.isDead())) {
                if (le != null) {
                    player.damage((int) (player.getMaxHealth() * psm.getPercentHealthPenalty()), le);
                } else {
                    player.damage((int) (player.getMaxHealth() * psm.getPercentHealthPenalty()));
                }
            }
        } catch (NullPointerException npe) {
            
        }
    }
}
