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
import org.bukkit.Sound;
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
  private Map<Player, Counter> playersOnCd;
  
  private FileSystem fileSystem;
  
  private Logger logger = getServer().getLogger();
  
  private WorldGuardPlugin wg;
  
  @Override
  public void onEnable() {
    fileSystem = new FileSystem(this);
    
    setupConfigDefaults();
    
    shooterProjectileMap = new HashMap<Snowball, Player>();
    playersOnCd = new HashMap<Player, Counter>();
  
    
    getServer().getPluginManager().registerEvents(this, this);
    getCommand("blitzreload").setExecutor(new CommandHandler(this));
    
    wg = getWorldGuard();
    
    runProjectileChecker();
    
    runCoolDown();
    super.onEnable();
  }
  
  

  @Override
  public void onDisable() {
    fileSystem.reloadConfig();
    
    getServer().getScheduler().cancelTasks(this);
    
    super.onDisable();
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
    
    if(clickedItem != null && clickedItem.getType() == Material.SNOW_BALL) {
     Action action = event.getAction();
     
     if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
       String itemName 
              = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
       if (itemName != null && (itemName.equalsIgnoreCase(blitzItemName) 
           || itemName.equalsIgnoreCase(primeItemName))) {
         Player launcher = event.getPlayer();
         
         event.setCancelled(true);
         if(!playersOnCd.containsKey(launcher)) {
           playersOnCd.put(launcher, new Counter(60));
      
           Snowball newBlitzHook = launcher.launchProjectile(Snowball.class);
           
           newBlitzHook.setMetadata("blitzHookData", 
                                    new FixedMetadataValue(this, itemName));
           
           shooterProjectileMap.put(newBlitzHook, launcher);
         }
         else
           launcher.sendMessage(cdMessage.replace("[cooldown]", 
                                                  playersOnCd.get(launcher).value() / 20.0
                                                  + " seconds"));
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
  
 /* private void scheduleCoolDown(Player launcher) {
    new BukkitRunnable() {
    int cd = 60;
    
      @Override
      public void run() {
        if(cd == -1) {
          playersOnCd.remove(launcher);
          this.cancel();
        }
        else {
          cd --;
          playersOnCd.put(launcher, new Integer(cd));
        }
 
      }
    }.runTaskTimer(this, 0L, 1L);
    
  }*/
  
  private void runCoolDown() {
    new BukkitRunnable() {
      
      @Override
      public void run() {
        for(Iterator<Entry<Player, Counter>> cdIterator = playersOnCd.entrySet().iterator();
            cdIterator.hasNext();) {
          Entry<Player, Counter> playerAndCd = cdIterator.next();
          
          Counter cooldown = playerAndCd.getValue();
          cooldown.decrement();
          
          if(cooldown.value() == 0)
            cdIterator.remove();
        }      
      }
    }.runTaskTimer(this, 0L, 1L);
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
     Snowball damager = (Snowball) event.getDamager();
     
     if(damager.hasMetadata("blitzHookData")
         && event.getEntity() instanceof Player) {
       String hookType = damager.getMetadata("blitzHookData").get(0).asString();
       
       shooterProjectileMap.remove(damager);
 
       Player shooter = (Player) damager.getShooter();
       Location shooterLoc = shooter.getLocation();
       
       ApplicableRegionSet set 
        = wg.getRegionManager(shooterLoc.getWorld()).getApplicableRegions(shooterLoc);
       
       if(set.allows(DefaultFlag.PVP)) {
         Player hookedPlayer = (Player) event.getEntity();
         Location hookedLoc = hookedPlayer.getLocation();
         
         String hookMessage = hookType.equals(blitzItemName) 
                               ? standardHookMsg : primeHookMsg;
         
         int dmg = hookType.equalsIgnoreCase(blitzItemName) ? standardDmg : primeDmg;
         
         hookedPlayer.sendMessage(hookMessage.replace("[shooter]", 
                                  shooter.getName()));
         
         hookedPlayer.playSound(hookedLoc, Sound.ARROW_HIT, 2.0f, 2.0f);
         
         shooter.sendMessage(ChatColor.RED 
                             + (ChatColor.BOLD + "You have hooked ")
                             + hookedPlayer.getName());
         
         hookedPlayer.damage(dmg, shooter);
         
         
         
         new BukkitRunnable() {
                    
          @Override
          public void run() {
           Location newHookedLoc = hookedPlayer.getLocation();
           Location newShooterLoc  = shooter.getLocation();
           
           double distance = newHookedLoc.distanceSquared(newShooterLoc);
           
           if(distance > 1) {
             Vector newShootVec = newShooterLoc.toVector();
             Vector newHookedVec = newHookedLoc.toVector();
             
             Vector newDirection 
                     = newShootVec.subtract(newHookedVec).normalize();
             
             hookedPlayer.setVelocity(newDirection);
           }
           else
             this.cancel(); 
          }
        }.runTaskTimer(this, 0L, 1L);
       }
     }
   }
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
      cdMessage = "&6&lCooldown: [cooldown] !";
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

}
