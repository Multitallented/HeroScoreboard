package multitallented.plugins.heroscoreboard.listeners;

import com.herocraftonline.dev.heroes.hero.Hero;
import multitallented.plugins.heroscoreboard.HeroScoreboard;
import multitallented.plugins.heroscoreboard.PlayerStatManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;

/**
 *
 * @author Multitallented
 */
public class PvPListener extends EntityListener {
    private final HeroScoreboard plugin;
    private final PlayerStatManager psm;

    public PvPListener(HeroScoreboard aThis, PlayerStatManager psm) {
        this.plugin=aThis;
        this.psm = psm;
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageByEntityEvent) || event.getDamage() == 0 ||
                (plugin.heroes != null && event.getDamage() < plugin.heroes.getHeroManager().getHero((Player) event.getEntity()).getHealth()) ||
                (plugin.heroes == null && ((Player) event.getEntity()).getHealth() > event.getDamage()))
            return;
        EntityDamageByEntityEvent edBy = (EntityDamageByEntityEvent) event;
        Entity damager = edBy.getDamager(); 
        if (event.getCause() == DamageCause.PROJECTILE) {
            damager = ((Projectile)damager).getShooter();
        }
        if (!(damager instanceof Player)) {
            return;
        }
        Player player = (Player) edBy.getEntity();
        Hero hero = null;
        if (plugin.heroes != null)
            hero = plugin.heroes.getHeroManager().getHero(player);
        Player dPlayer = (Player) damager;
        Hero dHero = null;
        if (hero != null)
            dHero = plugin.heroes.getHeroManager().getHero(dPlayer);
        
        //Record the info
        
        //Display the info using an async task
        
    }
    
}
