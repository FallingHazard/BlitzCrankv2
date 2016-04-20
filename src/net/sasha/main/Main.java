package net.sasha.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

import net.sasha.mechanics.CooldownUpdater;
import net.sasha.mechanics.ProjectileData;
import net.sasha.mechanics.ProjectileUpdater;
import net.sasha.utils.MutableInteger;
import net.sasha.utils.ParticleUtils;
import net.sasha.utils.TextUtils;

public class Main extends JavaPlugin implements Listener {
  private TextUtils utils;
  private String blitzItemName;
  private String primeItemName;

  private String cdMessage;

  private int primeDmg;
  private int standardDmg;

  private String primeHookMsg;
  private String standardHookMsg;

  private ProjectileData projData;

  private FileSystem fileSystem;

  private WorldGuardPlugin wg;

  @Override
  public void onEnable() {
    fileSystem = new FileSystem(this);

    setupConfigDefaults();

    
    getServer().getPluginManager().registerEvents(this, this);
    getCommand("blitzreload").setExecutor(new CommandHandler(this));

    wg = getWorldGuard();
    
    projData = new ProjectileData();
    
    new ProjectileUpdater(projData, this).runTaskTimer(this, 0L, 1L);
    
    new CooldownUpdater(projData).runTaskTimer(this, 0L, 1L);
    
    super.onEnable();
  }

  @Override
  public void onDisable() {
    fileSystem.reloadConfig();

    getServer().getScheduler().cancelTasks(this);

    super.onDisable();
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    ItemStack clickedItem = event.getItem();

    if (clickedItem != null && clickedItem.getType() == Material.SNOW_BALL) {
      Action action = event.getAction();

      if (action == Action.RIGHT_CLICK_AIR
          || action == Action.RIGHT_CLICK_BLOCK) {
        String itemName = ChatColor
            .stripColor(clickedItem.getItemMeta().getDisplayName());
        if (itemName != null && (itemName.equalsIgnoreCase(blitzItemName)
            || itemName.equalsIgnoreCase(primeItemName))) {
          Player launcher = event.getPlayer();

          event.setCancelled(true);

          Map<Player, MutableInteger> playerCdMap = projData.getPlayerCooldownMap();
          
          if (!playerCdMap.containsKey(launcher)) {
            playerCdMap.put(launcher, new MutableInteger(60));

            Snowball newBlitzHook = launcher.launchProjectile(Snowball.class);

            newBlitzHook.setMetadata("blitzHookData",
                new FixedMetadataValue(this, itemName));

            projData.getProjectileShooterMap().put(newBlitzHook, launcher);
          } else {
            launcher.sendMessage(cdMessage
                                  .replace("[cooldown]",
                                           playerCdMap.get(launcher).value() / 20.0 
                                            + " seconds"));
          }
        }
      }
    }
  }

  private WorldGuardPlugin getWorldGuard() {
    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

    // WorldGuard may not be loaded
    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
      return null;
    }

    return (WorldGuardPlugin) plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onHit(EntityDamageEvent event) {
    if (!event.isCancelled())
      if (event instanceof EntityDamageByEntityEvent)
        onEntityDamageByEntity((EntityDamageByEntityEvent) event);
  }

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

        ApplicableRegionSet set = wg.getRegionManager(shooterLoc.getWorld())
            .getApplicableRegions(shooterLoc);

        if (set.allows(DefaultFlag.PVP)) {
          Player hookedPlayer = (Player) event.getEntity();
          Location hookedLoc = hookedPlayer.getLocation();

          String hookMessage = hookType.equals(blitzItemName) ? standardHookMsg
                                                                : primeHookMsg;

          hookedPlayer
              .sendMessage(hookMessage.replace("[shooter]", shooter.getName()));

          hookedPlayer.playSound(hookedLoc, Sound.ARROW_HIT, 2.0f, 2.0f);

          shooter.sendMessage(ChatColor.RED
              + (ChatColor.BOLD + "You have hooked ") + hookedPlayer.getName());

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
                int dmg = hookType.equalsIgnoreCase(blitzItemName) ? standardDmg
                                                                     : primeDmg;

                hookedPlayer.damage(dmg, shooter);

                this.cancel();
              }
            }
          }.runTaskTimer(this, 0L, 1L);
        }
      }
    }
  }

  private void setupConfigDefaults() {
    FileConfiguration config = fileSystem.getConfig();

    blitzItemName = config.getString("blitz.item-name");
    if (blitzItemName == null) {
      blitzItemName = "Blitz-Hook";
      config.set("blitz.item-name", blitzItemName);
    }
    blitzItemName = TextUtils.stripColor(TextUtils.color(blitzItemName));

    primeItemName = config.getString("prime.item-name");
    if (primeItemName == null) {
      primeItemName = "Prime-Hook";
      config.set("prime.item-name", primeItemName);
    }
    primeItemName = TextUtils.stripColor(TextUtils.color(primeItemName));

    cdMessage = config.getString("cooldown-Message");
    if (cdMessage == null) {
      cdMessage = "&6&lCooldown: [cooldown] !";
      config.set("cooldown-Message", cdMessage);
    }
    cdMessage = TextUtils.color(cdMessage);

    primeDmg = config.getInt("prime.damage");
    if (primeDmg == 0) {
      config.set("prime.damage", primeDmg);
    }

    standardDmg = config.getInt("blitz.damage");
    if (standardDmg == 0) {
      config.set("blitz.damage", standardDmg);
    }

    primeHookMsg = config.getString("prime.hooked-by-msg");
    if (primeHookMsg == null) {
      primeHookMsg = "&6&lYou have been hooked by [shooter]";
      config.set("prime.hooked-by-msg", primeHookMsg);
    }
    primeHookMsg = TextUtils.color(primeHookMsg);

    standardHookMsg = config.getString("blitz.hooked-by-msg");
    if (standardHookMsg == null) {
      standardHookMsg = "&6&lYou have been hooked by [shooter]";
      config.set("blitz.hooked-by-msg", standardHookMsg);
    }
    standardHookMsg = TextUtils.color(standardHookMsg);

    fileSystem.saveCoreData();
  }

}
