package com.silentneeb.Slogger.Listeners;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.Silentneeb.Slogger.Slogger;
import com.bekvon.bukkit.residence.event.*;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class ResidenceEventListener implements Listener{

	public ResidenceEventListener(Slogger plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onResidenceOwnerChangeEvent(ResidenceOwnerChangeEvent event){
		//Logger log = Logger.getLogger("Minecraft");
		//log.log(Level.INFO, "[Slogger] : Recieved an custom event!");
		//log.log(Level.INFO, "[Slogger] : Event is... " + event.getEventName());
		
			ownerChange(((ResidenceOwnerChangeEvent) event).getNewOwner(), ((ResidenceOwnerChangeEvent) event).getResidence()); 
		}
	
	@EventHandler
	public void onResidenceCreationEvent (ResidenceCreationEvent event){
		Player player = event.getPlayer();
		CuboidArea townArea = new CuboidArea(new Location(Bukkit.getServer().getWorld("Silencia2"), 517, 0, 517), new Location(Bukkit.getServer().getWorld("Silencia2"), -517, 255, -517));
		if(Slogger.permission.playerHas(player, "slogger.admin.create")){
			plotCreated(event.getPlayer(), event.getResidence());
			return;
		}
		
		if(player.getWorld() != Bukkit.getWorld("Silencia2")){
			event.getPlayer().sendMessage("§4 You cannot create a plot here.");
			event.setCancelled(true);
		}
		if(!event.getPhysicalArea().checkCollision(townArea)){  //TODO Test
		//else if(player.getLocation().getX() >= 518 || player.getLocation().getX() <= -517){
			//if(player.getLocation().getZ() >= 518 || player.getLocation().getZ() <= -517){
				//Logger.getLogger("Minecraft").log(Level.INFO, "[Slogger] out of town");
				plotCreated(event.getPlayer(), event.getResidence());
			//}
		}
		else{
			event.getPlayer().sendMessage("§4 You cannot create a plot here.");
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onResidenceDeleteEvent (ResidenceDeleteEvent event){
		if(Slogger.permission.playerHas(((ResidenceDeleteEvent) event).getPlayer().getWorld(), ((ResidenceDeleteEvent) event).getPlayer().getName(), "slogger.admin.remove")){
			plotDeleted(event.getResidence().getName());
		}
		else{
			((ResidenceDeleteEvent) event).getPlayer().sendMessage("§4You cannot delete a plot. If you are trying to change your lot use /lot unclaim.");
			((ResidenceDeleteEvent) event).setCancelled(true);
		}
		plotDeleted(event.getResidence().getName());
	}
	
	@EventHandler
	public void onResidenceRentEvent (ResidenceRentEvent event){
		//TODO
		//onRentEvent(((ResidenceRentEvent) event));
	}
	
	
	void ownerChange (String newOwner, ClaimedResidence cr){  //TODO if player is online than update their plot in Slogger.playerlist
		Logger.getLogger("Minecraft").log(Level.INFO, "[Slogger] Changing owner");
		if(newOwner.equalsIgnoreCase("server land")) newOwner = "server";
		/*if(Slogger.playerList.containsKey(newOwner)){
			Slogger.playerList.get(newOwner).home_plot = cr;
		}*/
		Slogger.db.updatePlotOwner(newOwner, cr);
	}
	
	void plotCreated (Player player, ClaimedResidence cr){
		Slogger.db.addPlot(cr.getName(), cr.getWorld(), cr.getOwner());
	}
	
	void plotDeleted (String plotName){
		Slogger.db.deletePlot(plotName);
	}
	
	void onRentEvent(){
		
	}
}
