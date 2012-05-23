package com.Silentneeb.Slogger.commands;


import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Silentneeb.Slogger.Slogger;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class LotAdminExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		if(args.length != 1){
			return false;
		}
		if(args[0].equalsIgnoreCase("derelict")){
			checkDerelict(player);
			return true;
		}
		if(args[0].equalsIgnoreCase("reclaim") && player != null){
			reclaimLot(Residence.getResidenceManager().getByLoc(player.getLocation()), player);
			return true;
		}
		else if (player == null){
			sender.sendMessage("Ingame only");
			return true;
		}
		return false;
	}

	void checkDerelict(Player player){
		//TODO
		long derelictDate = 0;
		derelictDate = new Date().getTime() - 1209606000;
		Map<String, String> derelictMap = Slogger.db.getDerelict(derelictDate);
		StringBuilder sb = new StringBuilder();
		
		try{
			for(Map.Entry<String, String> entry : derelictMap.entrySet()){
				sb.append("§9" + entry.getKey() + " §f- §a" + entry.getValue() + "§f, ");
			}
			//sb.delete(sb.length() - 3, sb.length() - 1);
		}
		catch(Exception e){
			Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] HERE" + e);
		}
		
		player.sendMessage(sb.toString());
		
	}
	
	void reclaimLot(ClaimedResidence res, Player player){
		res.getPermissions().setOwner("Server Land", true);
		Residence.getTransactionManager().putForSale(res.getName(), player, 0, true);
	}
}
