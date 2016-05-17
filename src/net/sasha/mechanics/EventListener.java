package net.sasha.mechanics;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

import dagger.Lazy;
import lombok.RequiredArgsConstructor;
import net.sasha.file.BlitzConfig;
import net.sasha.main.BlitzMain;
import net.sasha.utils.MutableInteger;

@Singleton @RequiredArgsConstructor(onConstructor=@__({@Inject}))
public class EventListener implements Listener {
  private final BlitzConfig blitzConfig;
  private final ProjectileData projData;
  private final Lazy<BlitzMain> blitzMain;
  
  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    ItemStack clickedItem = event.getItem();

    if (clickedItem != null && clickedItem.getType() == Material.SNOW_BALL) {
      Action action = event.getAction();

      if (action == Action.RIGHT_CLICK_AIR
          || action == Action.RIGHT_CLICK_BLOCK) {
        String itemName = ChatColor
            .stripColor(clickedItem.getItemMeta().getDisplayName());
        if (itemName != null && (itemName.equalsIgnoreCase(blitzConfig.getBlitzItemName())
            || itemName.equalsIgnoreCase(blitzConfig.getPrimeItemName()))) {
          Player launcher = event.getPlayer();

          event.setCancelled(true);

          Map<Player, MutableInteger> playerCdMap = projData.getPlayerCooldownMap();
          
          if (!playerCdMap.containsKey(launcher)) {
            playerCdMap.put(launcher, new MutableInteger(60));

            Snowball newBlitzHook = launcher.launchProjectile(Snowball.class);

            newBlitzHook.setMetadata("blitzHookData", blitzMain.get().createMetaVal(itemName));

            projData.getProjectileShooterMap().put(newBlitzHook, launcher);
          } else {
            launcher.sendMessage(blitzConfig.getCdMessage()
                                  .replace("[cooldown]",
                                           playerCdMap.get(launcher).value() / 20.0 
                                            + " seconds"));
          }
        }
      }
    }
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void onHit(EntityDamageEvent event) {
    if (!event.isCancelled())
      if (event instanceof EntityDamageByEntityEvent)
        onEntityDamageByEntity((EntityDamageByEntityEvent) event);
  }

  @SuppressWarnings("deprecation")
  private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Snowball) {
      Snowball damager = (Snowball) event.getDamager();

      if (damager.hasMetadata("blitzHookData")
          && event.getEntity() instanceof Player) {
        String hookType 
               = damager.getMetadata("blitzHookData").get(0).asString();

        projData.getProjectileShooterMap().remove(damager);

        Player shooter = (Player) damager.getShooter();
        Location shooterLoc = shooter.getLocation();
       
        WorldGuardPlugin wg = blitzMain.get().getWg();
        
        if (wg == null 
            || wg.getRegionManager(shooterLoc.getWorld())
                .getApplicableRegions(shooterLoc).allows(DefaultFlag.PVP)) {
          Player hookedPlayer = (Player) event.getEntity();
          Location hookedLoc = hookedPlayer.getLocation();

          String hookMessage = hookType.equals(blitzConfig.getBlitzItemName()) 
                                               ? blitzConfig.getStandardHookMsg()
                                                 : blitzConfig.getPrimeHookMsg();

          hookedPlayer
              .sendMessage(hookMessage.replace("[shooter]", shooter.getName()));

          hookedPlayer.playSound(hookedLoc, Sound.ARROW_HIT, 2.0f, 2.0f);

          shooter.sendMessage(ChatColor.RED
              + (ChatColor.BOLD + "You have hooked ") + hookedPlayer.getName());

          blitzMain.get().runJobTimer(0L, 1L,
          new BukkitRunnable() {

            @Override
            public void run() {
              Location newHookedLoc = hookedPlayer.getLocation();
              Location newShooterLoc = shooter.getLocation();

              double distance = newHookedLoc.distanceSquared(newShooterLoc);

              if (distance > 4) {
                Vector newShootVec = newShooterLoc.toVector();
                Vector newHookedVec = newHookedLoc.toVector();

                Vector newDirection = newShootVec.subtract(newHookedVec)
                    .normalize();

                hookedPlayer.setVelocity(
                    newDirection.multiply(1).add(new Vector(0, 0.1, 0)));
              } else {
                int dmg = hookType.equalsIgnoreCase(blitzConfig.getBlitzItemName()) 
                                                    ? blitzConfig.getStandardDmg()
                                                    : blitzConfig.getPrimeDmg();

                hookedPlayer.damage(dmg, shooter);

                this.cancel();
              }
            }
          });
        }
      }
    }
  }
  

}
