package me.itoncek.rays.hideandrayseeksyou;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class DevelopementVersion implements Listener, CommandExecutor {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return true;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Bukkit.getLogger().info("Dev mode");
		event.getPlayer().showTitle(Title.title(Component.text(ChatColor.GOLD + "⚠"), Component.text(ChatColor.DARK_RED + "Server is in Development mode!")));
	}
}
