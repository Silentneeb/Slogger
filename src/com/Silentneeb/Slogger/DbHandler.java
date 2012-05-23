package com.Silentneeb.Slogger;
import java.util.List;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
/**
 * @author keith
 *
 */
public class DbHandler {
	static Slogger plugin;
	String serverid = "SP-smp1";

	Logger log = Logger.getLogger("Minecraft");
	
	public Connection getSQLConnection(){
		/*String address = plugin.getConfig().getString("mysql.mysql-user","jdbc:mysql://localhost:3306/minecraft");
		String user = plugin.getConfig().getString("mysql.mysql-user", "root");
		String password = plugin.getConfig().getString("mysql.mysl-password", "sckclosed838647");*/
		
		String address = "jdbc:mysql://localhost:3306/Minecraft";
		String user = "root";
		String password = "FuckMySQL1920";
		//String address = "jdbc:mysql://localhost:3306/minecraft";
		//String user = "root";
		//String password = "sckclosed838647";
		
		try{
			return DriverManager.getConnection(address + "?autoReconnect=true&user=" + user + "&password=" + password);
		}
		catch(Exception e){
			Logger.getLogger("Minecraft").log(Level.SEVERE, "COULD NOT CONNECT TO DATABASE!! Shutting down Slogger.");
		}
		
		return null;
	}
		
	protected String SQLCreatePlayerTable = "CREATE TABLE IF NOT EXISTS `SP_player` (" + 
	"`id` int(11) NOT NULL AUTO_INCREMENT," +
	"`playername` varchar(32) NOT NULL," +
	"`first_login` bigint(20) NOT NULL," + 
	"`last_login` bigint(20) NOT NULL," + 
	"`home_server` varchar(7) NOT NULL DEFAULT 'SP-smp1'," + 
	"PRIMARY KEY (`id`)" + 
	")";
	
	protected String SQLCreatePlotTable = "CREATE TABLE IF NOT EXISTS `SP_plot` (" +
	"`id` int(11) NOT NULL AUTO_INCREMENT," +
	"`plot` varchar(32) NOT NULL," +
	"`world` varchar(10) NOT NULL," +
	"`server` varchar(7) NOT NULL DEFAULT 'SP-smp1'," +
	"`claimdate` bigint(20) NOT NULL," +
	"`owner` int(11) NOT NULL," +
	"PRIMARY KEY (`id`)," +
	"FOREIGN KEY (`owner`) REFERENCES SP_player(`ID`)" +
	");";
	
	protected String SQLCreateRentedPlot = "CREATE TABLE IF NOT EXISTS `SP_rented` (" +
	"`id` int(11) NOT NULL AUTO_INCREMENT," +
	"`plot` varchar(32) NOT NULL," +
	"`world` varchar(10) NOT NULL," +
	"`server` varchar(7) NOT NULL DEFAULT 'SP-smp1'," +
	"`owner` int(11) NOT NULL," +
	"`renter` int(11) NOT NULL," +
	"PRIMARY KEY (`id`)," +
	"FOREIGN KEY (`owner`) REFERENCES SP_player(`ID`)," +
	"FOREIGN KEY (`renter`) REFERENCES SP_player(`ID`)" +
	");";
	
	protected String SQLAddServer = "INSERT INTO SP_player VALUES(0, 'Server', 0, 0, 'SP-smp1');";   
	
	public void initialize(Slogger plugin){
		DbHandler.plugin = plugin;
		boolean doFirstRun = false;
		//plugin = Bukkit.getServer().getPluginManager().getPlugin("Slogger");
		
		if(plugin == null){
			Logger.getLogger("Minecraft").log(Level.SEVERE, "COULD NOT RETRIEVE SLOGGER INSTANCE!!");
		}
		
		Connection conn = getSQLConnection();
		
		if(conn == null) {		
			plugin.getPluginLoader().disablePlugin(plugin);
			return;
		}
		else {
			ResultSet rs = null;
			Statement st = null;

			try{
				DatabaseMetaData dbm = conn.getMetaData();
				rs = dbm.getTables(null, null, "SP_player", null);
	            	if (!rs.next()){
	            		conn.setAutoCommit(false);
	            		st = conn.createStatement();
	            		st.execute(this.SQLCreatePlayerTable);
	            		st.execute(this.SQLCreatePlotTable);
	            		st.execute(this.SQLCreateRentedPlot);
	            		st.execute(this.SQLAddServer);
	            		conn.commit();
	            		log.info("[Slogger] Table 'SP-player' created.");
	            		log.info("[Slogger] Table 'SP-plot' created.");
	            		log.info("[Slogger] Table 'SP-rented' created.");
	            		doFirstRun = true;
				
	            	}
			}
	        catch (SQLException ex) {
				log.info("[Slogger] Database Error: No Table Found");
				log.info(ex.getMessage());
            }
			finally {
				try {
					if (rs != null)
						rs.close();
					if (conn != null)
						conn.close();
				} catch (SQLException ex) {
					log.info("[Slogger] Failed to close MySQL connection: ");
				}
			}
			
			//UpdateDB();
			
			try {
				if (!plugin.isEnabled()){
					return;
				}
				conn.close();
				log.info("[Slogger] Initialized db connection" );
			} catch (SQLException e) {
				e.printStackTrace();
				plugin.getServer().getPluginManager().disablePlugin(plugin);
			}
		
		}
		
		if(doFirstRun){
    		log.info("[Slogger] Doing first run!");
    		firstRun();
		}
	}
	
	public PlayerStats getPlayer(String name, boolean getinfo)
	{
		//TODO Cleanup
		PlayerStats pStats = new PlayerStats();
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		int id = -1;
		String plot = null;
		
		try{
			conn = getSQLConnection();			
			st = conn.prepareStatement("SELECT * FROM `SP_player` WHERE `playername` = '" + name + "';");
			rs = st.executeQuery();
			
			if(rs.next()){
				id = rs.getInt("id");
				pStats.first_login = rs.getLong("first_login");
				if(getinfo)
					pStats.last_login = rs.getLong("last_login");
				else
					pStats.last_login = new Date().getTime();
				pStats.home_server = rs.getString("home_server");
			}
			else{
				Logger.getLogger("Minecraft").log(Level.INFO, "[Slogger] Adding new player to database.");
				pStats = addPlayer(name);
			}
			
			rs = null;
			rs = st.executeQuery("SELECT `plot` FROM SP_plot WHERE `owner` = " + id + ";");
			
			try{
				if(rs.next()){
					plot = rs.getString("plot");
					pStats.home_plot = Residence.getResidenceManager().getByName(plot);
				}
			}
			catch(Exception e){
			}
			
			if(!getinfo){
				st = conn.prepareStatement("UPDATE `SP_player` SET `last_login` = " + pStats.last_login + " WHERE `id` = " + id + ";");
				st.executeUpdate();
			}
		}
		catch(Exception e){
			Logger.getLogger("Minecraft").log(Level.INFO, "[Slogger] Something went wrong retrieving player info!!");
			Logger.getLogger("Minecraft").log(Level.INFO, e.getMessage());
			pStats = null;
		}
		finally{
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
			}
		}
		
		return pStats;
	}
	
	public void updatePlotOwner(String newOwner, ClaimedResidence res){
		Connection conn = null;
		PreparedStatement st = null;
		
		if(newOwner.equalsIgnoreCase("server")) newOwner = "Server Land";
		
		try{
			conn = getSQLConnection();			
			st = conn.prepareStatement("UPDATE `SP_plot` SET `owner` = (SELECT `id` FROM `SP_player` WHERE `playername` = '" + newOwner + "') WHERE `plot` = '" + res.getName() + "';");
			st.executeUpdate();
		}
		catch(Exception e){
			Logger.getLogger("Minecraft").log(Level.INFO, "[Slogger] Something went wrong changing plot owner!!");
			Logger.getLogger("Minecraft").log(Level.INFO, e.getMessage());
		}
		finally{
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
			}
		}
	}
	
	public boolean doesSQLPlotExsist(String name){
		boolean exsists = false;
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try{
			conn = getSQLConnection();			
			st = conn.prepareStatement("SELECT * FROM `SP_plot` WHERE `plot` = '" + name + "';");
			rs = st.executeQuery();
			
			if(rs.next()){
				exsists = true;
			}
		}catch(Exception e){
			Logger.getLogger("Minecraft").log(Level.INFO, "[Slogger] Something went wrong checking for a plot!!");
			Logger.getLogger("Minecraft").log(Level.INFO, e.getMessage());
		}
		finally{
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
			}
		}
		
		return exsists;
	}
	
	public PlayerStats addPlayer(String name){
		PlayerStats pStats = new PlayerStats();
		pStats.first_login = new Date().getTime();
		pStats.last_login = pStats.first_login;
		pStats.home_server = Slogger.serverid;
		
		Connection conn = null;
		Statement st = null;
		
		try{
			conn = getSQLConnection();
			conn.setAutoCommit(false);
			st = conn.createStatement();
			st.execute("INSERT INTO `SP_player`(playername, first_login, last_login, home_server) VALUES('" + name + "', " + pStats.first_login + ", " + pStats.last_login + ", '" + pStats.home_server + "');");
			conn.commit();
			
		}
		catch (Exception e) {
			pStats = null;
			Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to add new player: " + e.getMessage());
		}
		finally{
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
				} catch (SQLException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
			}
		}
		
		Slogger.newPlayer(name);
		
		return pStats;
	}
	
	public void addPlot(String plotName, String world, String player){
		Connection conn = null;
		Statement st = null;
		
		if(player.equalsIgnoreCase("server land")) player = "Server";
		
		try{
			conn = getSQLConnection();
			conn.setAutoCommit(false);
			st = conn.createStatement();
			st.execute("INSERT INTO `SP_plot`(plot, world, server, claimdate, owner) " +
					"VALUES('" + plotName + "', '" + world + "', '" + serverid + "', " + new Date().getTime() + ", " +
							"(SELECT `id` FROM `SP_player` WHERE `playername` = '" + player + "'));");
			conn.commit();
			
		}
		catch (Exception e) {
			Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to add new plot: "  + e.getMessage());
		}
		finally{
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
				} catch (SQLException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
			}
		}
	}
	
	public void deletePlot(String plotName){
		Connection conn = null;
		Statement st = null;
		
		try{
			conn = getSQLConnection();
			conn.setAutoCommit(false);
			st = conn.createStatement();
			st.execute("DELETE FROM `SP_plot` WHERE `plot` = '" + plotName + "';");
			conn.commit();
		}
		catch (Exception e) {
			Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to delete plot: "  + e.getMessage());
		}
		finally{
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
				} catch (SQLException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
			}
		}
		
	}
	
	public ClaimedResidence getRandomLot(){
		ClaimedResidence res = null;
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try{
			conn = getSQLConnection();			
			st = conn.prepareStatement("SELECT `plot` FROM `SP_plot` WHERE `owner` = 1 ORDER BY RAND() LIMIT 1;");
			rs = st.executeQuery();
			
			if(!rs.next()){  //just in case a 4 got thrown in somewhere
				st = conn.prepareStatement("SELECT `plot` FROM `SP_plot` WHERE `owner` = 4 ORDER BY RAND() LIMIT 1;");
				rs = st.executeQuery();
			}
						
			if(rs.next()){
				Logger.getLogger("Minecraft").log(Level.INFO, "plot: " + rs.getString("plot"));
				res = Residence.getResidenceManager().getByName(rs.getString("plot"));
			}
			
		}catch(Exception e){
			//Logger.getLogger("Minecraft").log(Level.INFO, "ID: " + id + ",  plot: " + plot + ".  first: " + pStats.first_login + ".  last: " + pStats.last_login + ".  server: " + pStats.home_server);
			Logger.getLogger("Minecraft").log(Level.INFO, "[Slogger] Something went wrong checking for a plot!!");
			Logger.getLogger("Minecraft").log(Level.INFO, e.getMessage());
		}
		finally{
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
			}
		}
		
		return res;
	}
	
	public void insertWorldChange(String player, String preWorld, String newWorld){
		Connection conn = null;
		Statement st = null;
		
		try{
			conn = getSQLConnection();
			conn.setAutoCommit(false);
			st = conn.createStatement();
			st.execute("INSERT INTO `SP_world_log`(`player`, `pre_world`, `to_world`, `changedate`)" +
					"VALUES('" + player + "', '" + preWorld +"', '" + newWorld + "', (SELECT NOW()))" +
					";");
			conn.commit();
			
		}
		catch (Exception e) {
			Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to add world change: "  + e.getMessage());
		}
		finally{
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
				} catch (SQLException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
			}
		}
	}
	
	public void insertCommand(String player, String command){
			Connection conn = null;
			Statement st = null;
			
			if(command.length() > 20){
				command = command.substring(0, 19);
			}
			
			try{
				conn = getSQLConnection();
				conn.setAutoCommit(false);
				st = conn.createStatement();
				st.execute("INSERT INTO `SP_command_log`(`player`, `command`, `usagedate`)" +
						"VALUES('" + player + "', '" + command + "', (SELECT NOW()))" +
						";");
				conn.commit();
				
			}
			catch (Exception e) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to add command usuage: "  + e.getMessage());
			}
			finally{
				try {
					if (st != null)
						st.close();
					if (conn != null)
						conn.close();
					} catch (SQLException ex) {
					Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
				}
			}
	}
	
	public Map<String, String> getDerelict(long date){
		Map<String, String> derelict = new HashMap<String, String>();
		
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try{
			conn = getSQLConnection();			
			st = conn.prepareStatement("SELECT * FROM  `SP_player` p JOIN  `SP_plot` pl ON p.id = pl.owner WHERE last_login <= " + date + ";");
			rs = st.executeQuery();
			
			try{
				while(rs.next()){
					derelict.put(rs.getString("playername"), rs.getString("plot"));
				}
			}
			catch(Exception e){
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] HERE" + e);
			}
			
		}catch(Exception e){
			//Logger.getLogger("Minecraft").log(Level.INFO, "ID: " + id + ",  plot: " + plot + ".  first: " + pStats.first_login + ".  last: " + pStats.last_login + ".  server: " + pStats.home_server);
			Logger.getLogger("Minecraft").log(Level.INFO, "[Slogger] Something went wrong when executing a query");
			Logger.getLogger("Minecraft").log(Level.INFO, e.getMessage());
		}
		finally{
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
			}
		}
		
		return derelict;
	}
	
	@Deprecated
	public ResultSet executeQuery(String query){
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try{
			conn = getSQLConnection();			
			st = conn.prepareStatement(query);
			rs = st.executeQuery();
			
		}catch(Exception e){
			//Logger.getLogger("Minecraft").log(Level.INFO, "ID: " + id + ",  plot: " + plot + ".  first: " + pStats.first_login + ".  last: " + pStats.last_login + ".  server: " + pStats.home_server);
			Logger.getLogger("Minecraft").log(Level.INFO, "[Slogger] Something went wrong when executing a query");
			Logger.getLogger("Minecraft").log(Level.INFO, e.getMessage());
		}
		finally{
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				Logger.getLogger("Minecraft").log(Level.SEVERE, "[Slogger] Failed to close MySQL connection: ", ex);
			}
		}
		
		return rs;
	}
	
	public void UpdateDB(){
		Connection conn = getSQLConnection();
	
		ResultSet rs = null;
		Statement st = null;
		String createWorldLog = "CREATE TABLE IF NOT EXISTS `SP_world_log` (" +
				"`id` int(11) NOT NULL AUTO_INCREMENT," +
				"`player` varchar(32) NOT NULL," +
				"`pre_world` varchar(20) NOT NULL," +
				"`to_world` varchar(20) NOT NULL," +
				"`changedate` datetime NOT NULL," +
				"PRIMARY KEY (`id`)" +
				");";
		String createCommandLog = "CREATE TABLE IF NOT EXISTS `SP_command_log` (" +
			"`id` int(11) NOT NULL AUTO_INCREMENT," +
			"`player` varchar(20) NOT NULL," +
			"`command` varchar(20) NOT NULL," +
			"`usagedate` datetime NOT NULL," +
			"PRIMARY KEY (`id`)" +
			");";
		
		try{
			DatabaseMetaData dbm = conn.getMetaData();
			rs = dbm.getTables(null, null, "SP_world_log", null);
            	if (!rs.next()){
            		conn.setAutoCommit(false);
            		st = conn.createStatement();
            		st.execute(createWorldLog);
            		st.execute(createCommandLog);
            		conn.commit();
            		log.info("[Slogger] Table 'SP_world_log' created.");
            		log.info("[Slogger] Table 'SP_command_log' created.");
			
            	}
		}
        catch (SQLException ex) {
			log.info("[Slogger] Database Error: No Table Found");
			log.info(ex.getMessage());
        }
		finally {
			try {
				if (rs != null)
					rs.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				log.info("[Slogger] Failed to close MySQL connection: ");
			}
		}
	}
	
	public void firstRun(){
		List<String> residencesNames = Arrays.asList(Residence.getResidenceManager().getResidenceList());
		List<ClaimedResidence> residences = new ArrayList<ClaimedResidence>();
		
		for(int i = 0; i < residencesNames.size(); i++){
			ClaimedResidence res = Residence.getResidenceManager().getByName(residencesNames.get(i));
			residences.add(res);
			String name = res.getOwner();
			if(getPlayer(name, true) == null) addPlayer(name);
		}
		
		for(int i = 0; i < residences.size(); i++){
			if(!doesSQLPlotExsist(residences.get(i).getName())){
				addPlot(residences.get(i).getName(), residences.get(i).getWorld(), residences.get(i).getOwner());
			}
		}
	}
}
