package com.Silentneeb.Slogger.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Silentneeb.Slogger.PlayerStats;
import com.Silentneeb.Slogger.Slogger;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
//import com.bekvon.bukkit.residence.protection.CuboidArea;

public class LotExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2,
			String[] args) {
		
		if(args.length < 1){
			return false;
		}

		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		if (player == null){
			sender.sendMessage("Ingame only");
			return false;
		}
		
		PlayerStats pStats = Slogger.playerList.get(player.getName());
		if(pStats == null){
			player.sendMessage("Uh Oh!! Apparently you don't exsist. Please rejoin.");
			return true;
		}
		
		if(args[0].equalsIgnoreCase("unclaim")){
			if(pStats.home_plot == null){
				player.sendMessage("You don't have a lot to unclaim.");
				return true;
			}
			if(Slogger.inTown(pStats.home_plot.getArea("main"))){
				player.sendMessage("You can only unclaim if your lot is in the town.");
				return true;
			}
			pStats.home_plot.getPermissions().setOwner("Server Land", true);
			Residence.getTransactionManager().putForSale(pStats.home_plot.getName(), player, 0, true);
			pStats.home_plot = null;
			return true;
		}
		else if(args[0].equalsIgnoreCase("claim")){
			ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
			if(res == null){
				player.sendMessage("You are not standing in a valid lot.");
				return false;
			}
			Residence.getTransactionManager().buyPlot(res.getName(), player, false);
			return true;
		}
		else if(args[0].equalsIgnoreCase("open")){
			//TODO Teleport to random open lot
			
			//player.sendMessage("Command is broken right now will be fixed later...");
			//return true;
			
			ClaimedResidence res = Slogger.db.getRandomLot();
			
			if(res != null){
				res.tpToResidence(player, player, false);
				return true;
			}
			else {
				player.sendMessage("Could not find an open lot.");
				return true;
			}
		}
		else if(args[0].equalsIgnoreCase("create")){
			//TODO Create a new lot out in the town wilderness    TEST
			player.sendMessage("§4This command is still being tested, it will be released soon.");
			return true;
			/*String name = player.getName();
			if(args.length >= 2){
				name = args[1];
			}
			Location playerLoc = player.getLocation();
			Location loc1 = new Location(playerLoc.getWorld(), playerLoc.getX() - 39, playerLoc.getY() - 39, 1);
			Location loc2 = new Location(playerLoc.getWorld(), playerLoc.getX() + 40, playerLoc.getY() + 40, 256);  //will not work in 1.1
			
			//TODO remove ***
			//Residence.getSelectionManager().placeLoc1(player.getName(), loc1);
			//Residence.getSelectionManager().placeLoc2(player.getName(), loc2);
			
			//Residence.getSelectionManager().showSelectionInfo(player);  //for debugging
			//**
			
			Residence.getResidenceManager().addResidence(name, player.getName(), loc1, loc2);				
			
			ClaimedResidence createdRes = Residence.getResidenceManager().getByName(name);
			
			if(createdRes != null){
				createdRes.setTpLoc(player, false);
				player.sendMessage("Lot created successfully!");
				return true;
			}
			else{
				player.sendMessage("Could not create a lot here.");
				return true;
			}
			//return false;
			*/
		}
		else if(args[0].equalsIgnoreCase("list")){
			
			if(args.length == 1 || (args[1].equalsIgnoreCase("buy") || args[1].equalsIgnoreCase("open"))){
				Residence.getTransactionManager().printForSaleResidences(player);
				return true;
			}
			else if(args[1].equalsIgnoreCase("rent")){
				Residence.getRentManager().printRentableResidences(player);
				return true;
			}
			
		}		
		return false;
	}

}
