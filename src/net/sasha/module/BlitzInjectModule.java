package net.sasha.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import net.sasha.main.BlitzMain;
import net.sasha.main.BlitzPlugin;
import net.sasha.mechanics.EventListener;
import net.sasha.mechanics.ProjectileData;

@Module(injects = {BlitzMain.class})
public class BlitzInjectModule {
  private final BlitzPlugin plugin;
  private final ProjectileData projData = new ProjectileData();

  public BlitzInjectModule(BlitzPlugin blitzPlugin) {
    plugin = blitzPlugin;
  }
  
  @Provides @Singleton public BlitzPlugin providesPlugin() {
    return plugin;
  }
  
  @Provides @Singleton public ProjectileData providesProjData() {
    return projData;
  }
  
}
