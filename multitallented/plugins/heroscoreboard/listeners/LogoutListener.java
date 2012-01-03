package multitallented.plugins.heroscoreboard.listeners;

import com.herocraftonline.dev.heroes.hero.Hero;
import multitallented.plugins.heroscoreboard.HeroScoreboard;
import multitallented.plugins.heroscoreboard.PlayerStatManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Multitallented
 */
public class LogoutListener extends PlayerListener {
    private final PlayerStatManager psm;
    public LogoutListener(PlayerStatManager psm) {
        this.psm = psm;
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        try {
            long lastDamage = psm.getLoggingPlayer(player);
            int combatTagDuration = psm.getCombatTagDuration();
            LivingEntity le = psm.getWhoDamaged(player);
            if (lastDamage + combatTagDuration > System.currentTimeMillis() && !le.isDead()) {
                if (HeroScoreboard.heroes != null) {
                    Hero hero = HeroScoreboard.heroes.getHeroManager().getHero(player);
                    player.damage((int) (hero.getMaxHealth() * psm.getPercentHealthPenalty()), le);
                }
                player.damage((int) (20 * psm.getPercentHealthPenalty()), le);
            }
        } catch (NullPointerException npe) {
            
        }
    }
}
