package net.sasha.main;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
import com.sk89q.worldguard.protection.managers.RegionManager;

public class Main extends JavaPlugin implements Listener{
  private String blitzItemName;
  private String primeItemName;
  
  private String cdMessage;
  
  private int primeDmg;
  private int standardDmg;
  
  private String primeHookMsg;
  private String standardHookMsg;
  
  private Map<Snowball, Player> shooterProjectileMap;
  private Set<Player> playersOnCd;
  
  private FileSystem fileSystem;
  
  private Logger logger = getServer().getLogger();
  
  private WorldGuardPlugin wg;
  
  @Override
  public void onEnable() {
    fileSystem = new FileSystem(this);
    
    setupConfigDefaults();
    
    shooterProjectileMap = new HashMap<Snowball, Player>();
    playersOnCd = new HashSet<Player>();
    
    getServer().getPluginManager().registerEvents(this, this);
    
    wg = getWorldGuard();
    
    runProjectileChecker();
    super.onEnable();
  }

  private void setupConfigDefaults() {
    FileConfiguration config = fileSystem.getConfig();
    
    blitzItemName = config.getString("blitz.item-name");
    if(blitzItemName == null) {
      blitzItemName = "Blitz-Hook";
      config.set("blitz.item-name", blitzItemName);
    }
    blitzItemName = stripColor(color(blitzItemName));
    
    primeItemName = config.getString("prime.item-name");
    if(primeItemName == null) {
      primeItemName = "Prime-Hook";
      config.set("prime.item-name", primeItemName);
    }
    primeItemName = stripColor(color(primeItemName));
    
    cdMessage = config.getString("cooldown-Message");
    if(cdMessage == null) {
      cdMessage = "&6&lYou are on cooldown!";
      config.set("cooldown-Message", cdMessage);
    }
    cdMessage = color(cdMessage);
    
    primeDmg = config.getInt("prime.damage");
    if(primeDmg == 0) {
      config.set("prime.damage", primeDmg);
    }
    
    standardDmg = config.getInt("blitz.damage");
    if(standardDmg == 0) {
      config.set("blitz.damage", standardDmg);
    }
    
    primeHookMsg = config.getString("prime.hooked-by-msg");
    if(primeHookMsg == null) {
      primeHookMsg = "&6&lYou have been hooked by [shooter]";
      config.set("prime.hooked-by-msg", primeHookMsg);
    }
    primeHookMsg = color(primeHookMsg);
    
    standardHookMsg = config.getString("blitz.hooked-by-msg");
    if(standardHookMsg == null) {
      standardHookMsg = "&6&lYou have been hooked by [shooter]";
      config.set("blitz.hooked-by-msg", standardHookMsg);
    }
    standardHookMsg = color(standardHookMsg);
    
    fileSystem.saveCoreData();
    
    
  }
  
  private String color(String toColor) {
    return ChatColor.translateAlternateColorCodes('&', toColor);
  }
  
  private String stripColor(String toStrip) {
    return ChatColor.stripColor(toStrip);
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    ItemStack clickedItem = event.getItem();
    
    if(clickedItem.getType() == Material.SNOW_BALL) {
     Action action = event.getAction();
     
     if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
       String itemName 
              = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
       if (itemName != null && (itemName.equalsIgnoreCase(blitzItemName) 
           || itemName.equalsIgnoreCase(primeItemName))) {
         Player launcher = event.getPlayer();
         
         event.setCancelled(true);
         if(!playersOnCd.contains(launcher)) {
           playersOnCd.add(launcher);
      
           Snowball newBlitzHook = launcher.launchProjectile(Snowball.class);
           
           newBlitzHook.setMetadata("blitzHookData", 
                                    new FixedMetadataValue(this, itemName));
           
           shooterProjectileMap.put(newBlitzHook, launcher);
           
           scheduleCoolDown(launcher);
         }
         else
           launcher.sendMessage(cdMessage);
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
  
  private void scheduleCoolDown(Player launcher) {
    getServer().getScheduler().runTaskLater(this, new Runnable() {
      
      @Override
      public void run() {
        playersOnCd.remove(launcher);
        
      }
    }, 60L);
    
  }

  public void runProjectileChecker() {
    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      
      @Override
      public void run() {
        Iterator<Entry<Snowball, Player>> entryIterator 
                                          = shooterProjectileMap.entrySet().iterator();
        while(entryIterator.hasNext()) {
          Entry<Snowball, Player> shooterProjectilePair = entryIterator.next();
          
          Player shooter = shooterProjectilePair.getValue();
          Snowball blitzHook = shooterProjectilePair.getKey();
          
          if(!shooter.isOnline() 
             || shooter.getLocation().distanceSquared(blitzHook.getLocation()) 
                >= 169) {
            
            blitzHook.remove();
            entryIterator.remove();
          }
          
        }
      }
    }, 0L, 1L);
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void onHit(EntityDamageEvent event) {
    if(!event.isCancelled())
      if(event instanceof EntityDamageByEntityEvent)
       onEntityDamageByEntity((EntityDamageByEntityEvent) event);
        
  }

  private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
   if(event.getDamager() instanceof Snowball) {
     logger.info("here");
     Snowball damager = (Snowball) event.getDamager();
     
     if(damager.hasMetadata("blitzHookData") 
         && event.getEntity() instanceof Player) {
       String hookType = damager.getMetadata("blitzHookData").get(0).asString();
       logger.info(hookType);
       Player shooter = (Player) damager.getShooter();
       Location shooterLoc = shooter.getLocation();
       Vector shooterVec = shooterLoc.toVector();
       
       ApplicableRegionSet set 
        = wg.getRegionManager(shooterLoc.getWorld()).getApplicableRegions(shooterLoc);
       
       if(set.allows(DefaultFlag.PVP)) {
         Player hookedPlayer = (Player) event.getEntity();
         Location hookedLoc = hookedPlayer.getLocation();
         Vector hookedVector = hookedLoc.toVector();
         
         Vector direction = shooterVec.subtract(hookedVector).normalize();
         
         double distance = hookedLoc.distance(shooterLoc);
         
         String hookMessage = hookType.equals(blitzItemName) 
                               ? standardHookMsg : primeHookMsg;
         
         int dmg = hookType.equalsIgnoreCase(blitzItemName) ? standardDmg : primeDmg;
         
         hookedPlayer.sendMessage(hookMessage.replace("[shooter]", 
                                  shooter.getName()));
         
         shooter.sendMessage(ChatColor.RED 
                             + (ChatColor.BOLD + "You have hooked ")
                             + hookedPlayer.getName());
         
         hookedPlayer.damage(dmg, shooter);
         
         new BukkitRunnable() {
          
          int taskCounter = 0;
          
          @Override
          public void run() {
           if(taskCounter > distance - 1)
             this.cancel();
           
           
           hookedPlayer.setVelocity(direction);
           
           taskCounter ++;
            
          }
        }.runTaskTimer(this, 0L, 1L);
       }
     }
   }
  }

}
