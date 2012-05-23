package com.Silentneeb.Slogger;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.Silentneeb.Slogger.commands.*;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.silentneeb.Slogger.Listeners.ResidenceEventListener;
import com.silentneeb.Slogger.Listeners.SloggerPlayerListener;

//TODO: keep a list of online player's homes, adds/removes when players leave/join  DONE??
//TODO add a reload method to load all online players
//TODO update to bukkit 1.1-r6/1.2

public class Slogger extends JavaPlugin {
	//public final String serverid = this.getConfig().getString("mysql.server-id", "SP-smp1");
	public static Map<String, PlayerStats> playerList = new HashMap<String, PlayerStats>();
	
	public static final String serverid = "SP-smp1";
	public static Permission permission = null;
	
	public static DbHandler db = new DbHandler();
	private final SloggerPlayerListener playerListener = new SloggerPlayerListener();
	//TODO: add dictionary/map for online players and their res
	
	
	@Override
	public void onDisable() {
		Logger log = Logger.getLogger("Minecraft");
		log.info("Slogger V1.0 disabled!!");
		
	}

	@Override
	public void onEnable() {
		
		db.initialize(this);		
		
		new ResidenceEventListener(this);
		//getServer().getPluginManager().registerEvents(new ResidenceEventListener(), this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(playerListener, this);

		// init commands
		getCommand("home").setExecutor(new HomeExecutor());
		getCommand("visit").setExecutor(new VisitExecutor());
		getCommand("lot").setExecutor(new LotExecutor());
		getCommand("pinfo").setExecutor(new PinfoExecutor());
		getCommand("lotadmin").setExecutor(new LotAdminExecutor());
		
		setupPermissions();
		//FlagPermissions.addFlag("canRemove");
		
		Logger log = Logger.getLogger("Minecraft");
		log.info("[SLOGGER] Silent Productions Logger V1.0 Enabled!!");
	}
	
	/*public boolean onCommand(CommandSender sender, Command cmd, String CommandLabel, String[] args){
		/*if(cmd.getName().equalsIgnoreCase("home")){
			//sender.sendMessage("HELLO WORLD!!");
			
			Player player = null;
			if(sender instanceof Player)
			{
				player = (Player) sender;
				if(playerList.get(player.getName()).home_plot != null){
					player.sendMessage( playerList.get(sender.getName()).home_plot.getName());
					playerList.get(player.getName()).home_plot.tpToResidence(player, player, true);
				}
				else player.sendMessage("Sorry, but you don't have a home. :(");
			}
			return true;
		}
		return false;
	}*/
	
	public Boolean setupPermissions(){
		RegisteredServiceProvider<Permission> permProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permProvider != null) {
            permission = permProvider.getProvider();
        }
		return (permission != null);
	}
	
	public static void newPlayer(String name){
		Player player = Bukkit.getPlayer(name);
		
		if(player != null){
			player.sendMessage("§9Welcome to Silent Productions.");
			player.sendMessage("§9Here are a few things to get you started.");
			player.getInventory().addItem(new ItemStack(271, 1, (short)0));  //wood axe
			player.getInventory().addItem(new ItemStack(320, 2));  //cooked porkchop
			player.getInventory().addItem(new ItemStack(297, 5));  //bread
			player.getInventory().addItem(new ItemStack(6, 1));  //saplings
		}
	}
	
	public static boolean inTown(CuboidArea area){
		CuboidArea townArea = new CuboidArea(new Location(Bukkit.getServer().getWorld("Silencia2"), 517, 0, 517), new Location(Bukkit.getServer().getWorld("Silencia2"), -517, 255, -517));
		return area.checkCollision(townArea);
	}
	
	public String getServerId(){
		return serverid;
	}
	
}
