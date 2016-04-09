package net.sasha.utils;

import org.bukkit.ChatColor;

public class TextUtils {

  public static String stripColor(String toStrip) {
    return ChatColor.stripColor(toStrip);
  }

  public static String color(String toColor) {
    return ChatColor.translateAlternateColorCodes('&', toColor);
  }

}
