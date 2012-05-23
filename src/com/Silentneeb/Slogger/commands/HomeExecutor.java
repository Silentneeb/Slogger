package com.Silentneeb.Slogger.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Silentneeb.Slogger.Slogger;

public class HomeExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] arg3) {
		
		Player player = null;
		if(sender instanceof Player)
		{
			player = (Player) sender;
			if(Slogger.playerList.get(player.getName()).home_plot != null){
				Slogger.playerList.get(player.getName()).home_plot.tpToResidence(player, player, true);
			}
			else player.sendMessage("Sorry, but you don't have a home. :(");
		}
		return true;
	}

}
