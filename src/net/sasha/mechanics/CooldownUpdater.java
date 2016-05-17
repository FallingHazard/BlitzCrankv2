package net.sasha.mechanics;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.RequiredArgsConstructor;
import net.sasha.utils.MutableInteger;

@Singleton @RequiredArgsConstructor(onConstructor=@__({@Inject}))
public class CooldownUpdater extends BukkitRunnable{
  private final ProjectileData projData;
  
  @Override
  public void run() {
    Map<Player, MutableInteger> playersOnCd = projData.getPlayerCooldownMap();
    
    for (Iterator<Entry<Player, MutableInteger>> cdIterator = playersOnCd.entrySet().iterator();
         cdIterator.hasNext();) {
      Entry<Player, MutableInteger> playerAndCd = cdIterator.next();

      MutableInteger cooldown = playerAndCd.getValue();
      cooldown.decrement();

      if (cooldown.value() == 0)
        cdIterator.remove();
    }
  }

}
