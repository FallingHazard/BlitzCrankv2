package net.sasha.main;

import java.io.File;
import java.io.IOException;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/* Represents the file system,
 * has a plugin and server it works on,
 * as well as a core data file 
 * and config for that core Data.
 */
public class FileSystem {
  private final Plugin plugin;
  private File coreData;
  private FileConfiguration coreDataConfig;
  
  /* Sets up the plugin and Server
   * references, then initialises.
   */
  public FileSystem(Plugin plugin) {
    this.plugin = plugin;
    this.init();
  }
  
  /* Will create a data folder as well as an
   * empty config file if they do not already 
   * exist. Will then load the yaml config
   * from the core data file.
   */
  private void init() {
	  Server server = plugin.getServer();
    File pluginFolder = plugin.getDataFolder();
    
    if(!pluginFolder.exists()) {
      pluginFolder.mkdir();
      server.getLogger().info("Plugin folder created!");   
    }
    
    this.coreData = new File(pluginFolder.getAbsolutePath()
            + File.separator + "CoreData.yml");
    
    try {
      if(!this.coreData.exists()) {
        this.coreData.createNewFile();
        server.getLogger().info("Core data file created!");  
      }
      
      this.coreDataConfig = YamlConfiguration.loadConfiguration(coreData);
    } catch (IOException e) {
      server.getLogger().info("Failed to load make new config! Shutting down.");
      server.shutdown();
    }
    catch(Exception e) {
      server.getLogger().info("Something unexpected went wrong!");
      server.getLogger().info(e.toString());
      server.shutdown();
    }
    
  }

  /* Saves the core Data config to the core Data file */
  public void saveCoreData() {
    Server server = plugin.getServer();
	
    try {
      this.coreDataConfig.save(this.coreData);
    } catch (IOException e) {
      server.getLogger().severe("Error Saving Config File!");
      server.getLogger().severe("Disabling plugin");
      server.getPluginManager().disablePlugin(this.plugin);
    }
  }
  
  /* Allows the game to modify the core config.*/
  public FileConfiguration getConfig() {
    return coreDataConfig;
  }
  
}
