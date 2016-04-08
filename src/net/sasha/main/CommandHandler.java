package net.sasha.main;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {
  private final Main plugin;

  public CommandHandler(Main main) {
    plugin = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label,
      String[] args) {
    if (sender.hasPermission("blitz.admin")) {
      plugin.onDisable();
      plugin.onEnable();

      sender.sendMessage(ChatColor.GOLD 
                         + (ChatColor.BOLD + "[Blitz] Config reloaded"));
    }
    return false;
  }

}
