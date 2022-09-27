package me.itoncek.rays.hideandrayseeksyou;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TagCommand implements CommandExecutor {
	/**
	 * Executes the given command, returning its success.
	 * <br>
	 * If false is returned, then the "usage" plugin.yml entry for this command
	 * (if defined) will be sent to the player.
	 *
	 * @param sender  Source of the command
	 * @param command Command which was executed
	 * @param label   Alias of the command which was used
	 * @param args    Passed command arguments
	 *
	 * @return true if a valid command, otherwise false
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(sender.isOp()) {
			String name = args[0];
			Player player = Bukkit.getPlayer(name);
			if(player != null) {
				HideAndRaySeeksYou.hunters.add(player);
				sender.sendMessage(ChatColor.GREEN + "Added " + name + " to the list");
			} else {
				sender.sendMessage(ChatColor.RED + "Unable to find this player, please check it out: " + name);
			}
		}
		return true;
	}
}
