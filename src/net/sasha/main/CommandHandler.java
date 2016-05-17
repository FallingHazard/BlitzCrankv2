package net.sasha.main;

import net.md_5.bungee.api.ChatColor;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dagger.Lazy;
import lombok.RequiredArgsConstructor;

@Singleton @RequiredArgsConstructor(onConstructor=@__({@Inject}))
public class CommandHandler implements CommandExecutor {
  private final Lazy<BlitzMain> blitzMain;

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label,
      String[] args) {
    if (sender.hasPermission("blitz.admin")) {
      blitzMain.get().onDisable();
      blitzMain.get().onEnable();

      sender.sendMessage(ChatColor.GOLD 
                         + (ChatColor.BOLD + "[Blitz] Config reloaded"));
    }
    return false;
  }

}
