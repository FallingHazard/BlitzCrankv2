package net.sasha.main;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sasha.file.BlitzConfig;
import net.sasha.mechanics.CooldownUpdater;
import net.sasha.mechanics.EventListener;
import net.sasha.mechanics.ProjectileUpdater;

@Singleton @RequiredArgsConstructor(onConstructor=@__({@Inject}))
public class BlitzMain implements Listener {
  private final EventListener listener;
  private final BlitzConfig blitzConfig; 
  private final CommandHandler cmdHandler;  
  
  private final ProjectileUpdater projUpdater; 
  private final CooldownUpdater cdUpdater; 
  
  private final BlitzPlugin blitzPlugin;
  
  @Getter private WorldGuardPlugin wg;

  public void onEnable() {
    blitzConfig.setupConfigDefaults();

    blitzPlugin.registerEvents(listener, blitzPlugin);
    blitzPlugin.registerCommand("blitzreload", cmdHandler);

    wg = getWorldGuard();
    
    projUpdater.runTaskTimer(blitzPlugin, 0L, 1L);
    
    cdUpdater.runTaskTimer(blitzPlugin, 0L, 1L);
  }

  public void onDisable() {
    blitzConfig.reloadConfig();

    blitzPlugin.cancelAllTasks();
  }

  private WorldGuardPlugin getWorldGuard() {
    Plugin plugin = blitzPlugin.getServerPlugin("WorldGuard");

    // WorldGuard may not be loaded
    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
      return null;
    }

    return (WorldGuardPlugin) plugin;
  }

  public FixedMetadataValue createMetaVal(String itemName) {
    return new FixedMetadataValue(blitzPlugin, itemName);
  }

  public void runJobTimer(long delay, long period, BukkitRunnable bukkitRunnable) {
    bukkitRunnable.runTaskTimer(blitzPlugin, delay, period);
  }
  
  public Collection<? extends Player> getOnlinePlayers() {
    return blitzPlugin.getServer().getOnlinePlayers();
  }

}
