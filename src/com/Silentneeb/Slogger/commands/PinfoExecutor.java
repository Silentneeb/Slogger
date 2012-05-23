// '§'  colour symbol
package com.Silentneeb.Slogger.commands;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Silentneeb.Slogger.PlayerStats;
import com.Silentneeb.Slogger.Slogger;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class PinfoExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2,
			String[] args) {
		PlayerStats pStats = null;
		
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		if(args.length == 0 && player != null){
			ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
			if(res == null){
				player.sendMessage("Please stand in a valid lot.");
				return true;
			}
			pStats = Slogger.playerList.get(res.getOwner());
			if(pStats == null){
				pStats = Slogger.db.getPlayer(res.getOwner(), true);
			}
			if(pStats == null){
				player.sendMessage("Couldn't retrieve lot owner.");
				return true;
			}
			else{
				sendInfo(pStats, res.getOwner(), player);
				return true;
			}
		}
		else if(args.length == 0 && player == null){
			sender.sendMessage("Please provide a player name when using from the console.");
			return true;
		}
		else if (args.length == 1){
			pStats = Slogger.playerList.get(args[0]);
			if(pStats == null){
				pStats = Slogger.db.getPlayer(args[0], true);
			}
			if(pStats == null){
				player.sendMessage("Could not find anybody with that name.");
				return true;
			}
			else{
				sendInfo(pStats, args[0], sender);
				return true;
			}
		}
		return false;
	}
	
	private void sendInfo(PlayerStats pStats, String targetplayer, CommandSender sender){
		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.CANADA);
		sender.sendMessage(targetplayer + "'s INFO");
		sender.sendMessage("  ");
		try{
			sender.sendMessage("§2First Joined: §f" + df.parse(DateFormat.getDateInstance().format(new Date(pStats.first_login))));
			sender.sendMessage("§2Last Login: §f" + df.parse(DateFormat.getDateInstance().format(new Date(pStats.last_login))));
		
		}
		catch(Exception e){
			sender.sendMessage("Unable to parse login dates.");
		}
		if(pStats.home_server != null || pStats.home_plot != null)
			sender.sendMessage("§4Home - Server: §f" + pStats.home_server + "  §4Lot: §f" + pStats.home_plot.getName());
		else sender.sendMessage("§4Could not parse Server and/or Plot.");
	}

}
