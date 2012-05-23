package com.Silentneeb.Slogger.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Silentneeb.Slogger.PlayerStats;
import com.Silentneeb.Slogger.Slogger;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class VisitExecutor  implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel,
			String[] args) {
		
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (player == null){
			sender.sendMessage("Ingame only");
			return true;
		}
		
		if(args.length != 1){
			return false;
		}
		
		PlayerStats pStats = null;
		
		pStats = Slogger.playerList.get(args[0]);
		
		//pull from Residence than use the db
		if(pStats == null){  
			ClaimedResidence res = Residence.getResidenceManager().getByName(args[0]);
			if(res != null){
				pStats = new PlayerStats();
				pStats.home_plot = res;
			}
		}
		if(pStats == null){
			pStats = Slogger.db.getPlayer(args[0], true);
		}
		
		if(pStats != null){
			pStats.home_plot.tpToResidence(player, player, true);
			return true;
		}
		else{
			player.sendMessage("Could not find any plot or player by that name.");
			return true;
		}
	}

}
