package com.silentneeb.Slogger.Listeners;

//import java.util.logging.Level;
//import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.Silentneeb.Slogger.Slogger;

public class SloggerPlayerListener implements Listener{

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event){
		//Logger.getLogger("Minecraft").log(Level.INFO, "Recieved Join event!!");
		String name = event.getPlayer().getName();
		Slogger.playerList.put(name, Slogger.db.getPlayer(name, false));
		Slogger.db.insertWorldChange(name, event.getPlayer().getWorld().getName() , event.getPlayer().getWorld().getName());
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerKick(PlayerKickEvent event){
		Slogger.playerList.remove(event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event){
		Slogger.playerList.remove(event.getPlayer().getName());
	}
	
	/*@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event){
		Player player = event.getPlayer();
		Slogger.db.insertWorldChange(player.getName(), event.getFrom().getName(), player.getWorld().getName());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		Player player = event.getPlayer();
		Slogger.db.insertCommand(player.getName(), event.getMessage());
	}*/
}
