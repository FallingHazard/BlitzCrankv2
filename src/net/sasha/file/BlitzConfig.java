package net.sasha.file;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sasha.utils.TextUtils;

@Singleton @RequiredArgsConstructor(onConstructor=@__({@Inject}))
public class BlitzConfig {
  @Getter private String blitzItemName;
  @Getter private String primeItemName;

  @Getter private String cdMessage;

  @Getter private int primeDmg;
  @Getter private int standardDmg;

  @Getter private String primeHookMsg;
  @Getter private String standardHookMsg;
  
  private final FileSystem fileSystem;
  
  public void setupConfigDefaults() {
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

  public void reloadConfig() {
    fileSystem.reloadConfig();
  }
  
}
