package com.cswild.minecraft.vip;

import java.sql.*;

import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import com.cswild.minecraft.utility.ChatManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class MysqlManager {
	private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;
	
	public MysqlManager() {
		setup();
	}
	
	public void setup() {
		ConfigurationSection sec = VipManager.config.getMainConfig().getConfigurationSection("database");
		String host = sec.getString("host");
		String user = sec.getString("user");
		String password = sec.getString("password");
		String name = sec.getString("name");
		int port = sec.getInt("port");
		
		config.setJdbcUrl("jdbc:mysql://"+host+":"+port+"/"+name);
		config.setUsername(user);
		config.setPassword(password);
		config.addDataSourceProperty("cachePrepStmts" , "true");
        config.addDataSourceProperty("prepStmtCacheSize" , "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit" , "2048");
        ds = new HikariDataSource(config);
        
        Connection connection = null;
		Statement statement = null;
		
		try {
			synchronized(this) {
				connection = ds.getConnection();
				statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS `vipmanager_vips` (" + 
						"	`uuid` varchar(255) NOT NULL," + 
						"	`name` varchar(255)," + 
						"	`server` varchar(255)," + 
						"	`buydate` datetime," + 
						"	`enddate` datetime," + 
						"	`groupid` INTEGER," + 
						"   `addmoney` BOOL," +
						"   `executed` BOOL," +
						"	PRIMARY KEY( `uuid` )" + 
						");");
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS `vipmanager_groups` (" + 
						"	`id` int(10) NOT NULL auto_increment," + 
						"	`group` varchar(255)," + 
						"	`money` INTEGER," + 
						"   `commands` varchar(255)," +
						"	`description` varchar(255)," +
						"	PRIMARY KEY( `id` )" + 
						");");
			
				ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.GREEN+"Mysql connect");
			}
			
		}catch(SQLException e) {
			ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED+"Could not connect to mysql database");
		}finally {
			try {
				if(statement != null )
					statement.close();
				if(connection != null)
					connection.close();
			} catch (SQLException e) {
				ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED+"Could not close statement");
			}
		}
	}
	
	public void PlayerAddGroup(CommandSender sender, OfflinePlayer player, String group) {
		PlayerAddGroup(sender, player, group, 30);
	}
	
	public void PlayerAddGroup(CommandSender sender,OfflinePlayer player, String group, int days) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		try {
			connection = ds.getConnection();
			statement = connection.prepareStatement("SELECT `id` FROM `vipmanager_groups` WHERE `group` = ?;");
			statement.setString(1, group);
			rs = statement.executeQuery();
			if(rs.next()) {
				int id = rs.getInt("id");
				statement.close();
				statement = connection.prepareStatement("SELECT `enddate` FROM `vipmanager_vips` WHERE `groupid` = ? AND `uuid` = ? AND `server` = ?;");
				statement.setInt(1, id);
				statement.setString(2, player.getUniqueId().toString());
				statement.setString(3, VipManager.config.getMainConfig().getString("server_id"));
				rs2 = statement.executeQuery();
				if(!rs2.next()) {
					statement.close();
					statement = connection.prepareStatement("INSERT INTO `vipmanager_vips` (uuid,name,server,buydate,enddate,groupid,addmoney,executed)" +
							"VALUES (?,?,?,NOW(),?,?,0,0); ");
					statement.setString(1, player.getUniqueId().toString());
					statement.setString(2, player.getName());
					statement.setString(3, VipManager.config.getMainConfig().getString("server_id"));
					statement.setTimestamp(4, new java.sql.Timestamp(DateUtils.addDays(new java.util.Date(), days).getTime()));
					statement.setInt(5, id);
					statement.executeUpdate();
					ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("successful_add"));
				} else {
					Timestamp endTimestamp = rs2.getTimestamp("enddate");
					Date endDate = new Date(endTimestamp.getTime());

					statement.close();
					statement = connection.prepareStatement("UPDATE `vipmanager_vips` SET `addmoney` = 0, `executed` = 0, `enddate` = ? WHERE `server` = ? AND `uuid` = ?;");
					statement.setTimestamp(1, new java.sql.Timestamp(DateUtils.addDays(endDate, days).getTime()));
					statement.setString(2, VipManager.config.getMainConfig().getString("server_id"));
					statement.setString(3, player.getUniqueId().toString());
					statement.executeUpdate();
					ChatManager.sendMsgPrefix(sender,VipManager.config.getLangConfig().getString("successful_update"));
				}
			} else
				ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("invalid_group"));
				
		} catch(SQLException e) {
			e.printStackTrace();
			ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "PlayerAddGroup failed");
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(rs2 != null)
					rs2.close();
				if(statement != null)
					statement.close();
				if(connection != null)
					connection.close();
			} catch (SQLException e) {
				ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "PlayerAddGroup close failed");
			}
		}
	}
	
	public String getPlayerGroup(Player player) {
		String ret = "";
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = ds.getConnection();
			statement = connection.prepareStatement("SELECT g.`group` as `group` FROM `vipmanager_groups` as g, `vipmanager_vips` as v WHERE v.uuid = ? AND v.server = ? AND v.groupid = g.id ");
			statement.setString(1, player.getUniqueId().toString());
			statement.setString(2, VipManager.config.getMainConfig().getString("server_id"));
			rs = statement.executeQuery();
			if(rs.next())
				ret = rs.getString("group");
		
		} catch (SQLException e) {
			e.printStackTrace();
			ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "GetPlayerInfo from database failed");
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(statement != null)
					statement.close();
				if(connection != null)
					connection.close();
			} catch (SQLException e) {
				ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "GetPlayerInfo statement or resultSet closing failed");
			}
		}
		return ret;
	}
	
	public int getPlayerMoneyToAdd(Player player) {
		int ret = 0;
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = ds.getConnection();
			statement = connection.prepareStatement("SELECT g.`money` as `money` FROM `vipmanager_groups` as g, `vipmanager_vips` as v WHERE v.uuid = ? AND v.server = ? AND v.groupid = g.id AND v.`addmoney` = 0");
			statement.setString(1, player.getUniqueId().toString());
			statement.setString(2, VipManager.config.getMainConfig().getString("server_id"));
			rs = statement.executeQuery();
			while(rs.next())
				ret += rs.getInt("money");
		
		} catch (SQLException e) {
			ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "GetPlayerMoneyToAdd from database failed");
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(statement != null)
					statement.close();
				if(connection != null)
					connection.close();
			} catch (SQLException e) {
				ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "GetPlayerMoneyToAdd statement close failde");
			}
		}
		return ret;
	}
	
	public void giveMoney(Player player) {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = ds.getConnection();
			statement = connection.prepareStatement("UPDATE `vipmanager_vips` SET `addmoney` = 1 WHERE uuid = ? AND server = ?");
			statement.setString(1, player.getUniqueId().toString());
			statement.setString(2, VipManager.config.getMainConfig().getString("server_id"));
			
			statement.executeUpdate();
			
		} catch (SQLException e) {
			ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "GiveMoney database failed");
			e.printStackTrace();
		} finally {
			try {
				if(statement != null)
					statement.close();
				if(connection != null)
					connection.close();
			} catch (SQLException e) {
				ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "giveMoney statement close failde");
			}
		}
	}
	
	public String getPlayerCommand(Player player) {
		String ret = "";
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try {
			connection = ds.getConnection();
			statement = connection.prepareStatement("SELECT g.`commands` as `commands` FROM `vipmanager_groups` as g, `vipmanager_vips` as v WHERE v.uuid = ? AND v.server = ? AND v.groupid = g.id AND v.`executed` = 0");
			statement.setString(1, player.getUniqueId().toString());
			statement.setString(2, VipManager.config.getMainConfig().getString("server_id"));
			rs = statement.executeQuery();
			while(rs.next()) 
				ret += rs.getString("commands");
		
		} catch (SQLException e) {
			ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "getPlayerCommand from database failed");
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(statement != null)
					statement.close();
				if(connection != null)
					connection.close();
			} catch (SQLException e) {
				ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "getPlayerCommand statement close failde");
			}
		}
		return ret;
	}
	
	public void executeCommands(Player player) {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = ds.getConnection();
			statement = connection.prepareStatement("UPDATE `vipmanager_vips` SET `executed` = 1 WHERE uuid = ? AND server = ?");
			statement.setString(1, player.getUniqueId().toString());
			statement.setString(2, VipManager.config.getMainConfig().getString("server_id"));
			
			statement.executeUpdate();
			
		} catch (SQLException e) {
			ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "executeCommands database failed");
			e.printStackTrace();
		} finally {
			try {
				if(statement != null)
					statement.close();
				if(connection != null)
					connection.close();
			} catch (SQLException e) {
				ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED + "executeCommands statement close failde");
			}
		}
	}
	
	public void close() throws SQLException {
		if(ds != null && ds.isClosed())
			ds.close();
	}
}
