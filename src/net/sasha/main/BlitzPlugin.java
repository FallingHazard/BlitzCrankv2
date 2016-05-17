package net.sasha.main;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import dagger.ObjectGraph;
import net.sasha.mechanics.EventListener;
import net.sasha.module.BlitzInjectModule;

public class BlitzPlugin extends JavaPlugin{
  private BlitzMain blitzMain;

  @Override
  public void onDisable() {
    super.onDisable();
  }

  @Override
  public void onEnable() {
    ObjectGraph graph = ObjectGraph.create(new BlitzInjectModule(this));
    
    blitzMain = graph.get(BlitzMain.class);
    blitzMain.onEnable();
    super.onEnable();
  }

  protected void registerEvents(EventListener listener, BlitzPlugin blitzPlugin) {
    getServer().getPluginManager().registerEvents(listener, blitzPlugin);
  }

  protected void registerCommand(String name, CommandHandler cmdHandler) {
    getCommand(name).setExecutor(cmdHandler);
  }

  protected void cancelAllTasks() {
    getServer().getScheduler().cancelTasks(this);
  }

  protected Plugin getServerPlugin(String name) {
    return getServer().getPluginManager().getPlugin(name);
  }

}
