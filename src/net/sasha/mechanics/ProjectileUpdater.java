package net.sasha.mechanics;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;

import dagger.Lazy;
import lombok.RequiredArgsConstructor;
import net.sasha.main.BlitzMain;
import net.sasha.main.BlitzPlugin;
import net.sasha.utils.ParticleUtils;

@Singleton @RequiredArgsConstructor(onConstructor=@__({@Inject}))
public class ProjectileUpdater extends BukkitRunnable{
  private final ProjectileData projectileData;
  private final Lazy<BlitzMain> blitzMain;
  
  public void run() {
    trackProjectiles();
    traceProjectiles();
  }
  
  /* Checks if the snowball is to far away from its shooter or if the 
   * shooter logged out. In either case marks it for deletion. 
   * 
   * If not marked for deletion prepares the snowball for tracing.
   */
  private void trackProjectiles() {
    /* Part 1 track the projectiles */
    Iterator<Entry<Snowball, Player>> entryIterator 
                                       = projectileData
                                          .getProjectileShooterMap()
                                           .entrySet().iterator();
    
    while (entryIterator.hasNext()) {
      Entry<Snowball, Player> shooterProjectilePair = entryIterator.next();

      Player shooter = shooterProjectilePair.getValue();
      Snowball blitzHook = shooterProjectilePair.getKey();

      if (!shooter.isOnline() 
          || shooter.getLocation().distanceSquared(blitzHook.getLocation()) 
             >= 169) {
        blitzHook.remove();
        // to do remove from tracker.
      }

      if (!blitzHook.isDead())
        projectileData.getTracedLocationMap().put(blitzHook.getLocation(),
                                                  blitzHook);
      else
        entryIterator.remove();
    }
  }

  private void traceProjectiles() {
    /* Part 2 trace the projectile path */
    Iterator<Entry<Location, Snowball>> traceIterator 
                                         = projectileData
                                            .getTracedLocationMap()
                                             .entrySet().iterator();

    while (traceIterator.hasNext()) {
      Entry<Location, Snowball> tracedEntry = traceIterator.next();

      Snowball tracedBall = tracedEntry.getValue();

      if (tracedBall.isDead())
        traceIterator.remove();
      else {
        Location locationToTrace = tracedEntry.getKey();

        for (Player onlinePlayer : blitzMain.get().getOnlinePlayers())
          if (onlinePlayer
               .getWorld().getUID().equals(locationToTrace.getWorld().getUID())) {
            ParticleUtils
             .witchParticleAt(locationToTrace.add(0, 0.5, 0)).sendPacket(onlinePlayer);
            
            ParticleUtils
            .witchParticleAt(locationToTrace.add(0, -0.5, 0)).sendPacket(onlinePlayer);
          }
      }
    }
  }

}
