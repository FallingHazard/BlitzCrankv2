package net.sasha.mechanics;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;

import net.sasha.utils.MutableInteger;

@Singleton
public class ProjectileData {
  /* Maps the projectile to the person who fired it */
  private final Map<Snowball, Player> shooterProjectileMap
    = new HashMap<Snowball, Player>();
  
  /* Maps a person on CD to their CD value. */
  private final Map<Player, MutableInteger> playersOnCd
    = new HashMap<Player, MutableInteger>();
  
  /* Maps a traced location to the projectile that traced it */
  private final Map<Location, Snowball> tracedLocations
    = new HashMap<Location, Snowball>();
  
  public Map<Snowball, Player> getProjectileShooterMap() {
    return shooterProjectileMap;
  }
  
  public Map<Player, MutableInteger> getPlayerCooldownMap() {
    return playersOnCd;
  }
  
  public Map<Location, Snowball> getTracedLocationMap() {
    return tracedLocations;
  }

}
