package com.cswild.minecraft.vip;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.cswild.minecraft.utility.ChatManager;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class VipManager extends JavaPlugin {
	// componets 
	public static Permission permission = null;
	public static ConfigManager config = null;
	public static CommandManager cm = null;
	public static VipManager instance = null;
	public static MysqlManager mysql = null;
	public static Economy economy = null;
	
	@Override
	public void onEnable() {
		instance = this;
		config = new ConfigManager();
		
		setupCommand();
		
		getServer().getPluginManager().registerEvents(new EventManager(),this);
		
		if(!setupPermissions()) {
			ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED+"Permission plugin is missing!");
			return; //todo: da sie to zrobic ladniej?
		}
		
		if(!setupEconomy()) {
			ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED+"Economy plugin is missing!");
			return; //todo: da sie to zrobic ladniej?	
		}
		
		ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), "Using " + ChatColor.RED + permission.getName());
		ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), "Using " + ChatColor.RED + economy.getName());
		
		mysql = new MysqlManager();
		
		PlayerManager.checkAndAddAllVip();
		
		ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.GREEN + "Working...");
	}
	
	@Override
	public void onDisable() {
		PlayerManager.removeAllVip();
		try {
			mysql.close();
		} catch (SQLException e) {
			ChatManager.sendMsgPrefix(Bukkit.getConsoleSender(), ChatColor.RED+"could not close connection");	
		}
	}
	private void setupCommand() {
		cm = new CommandManager();
		getCommand(CommandManager.cmd).setExecutor(cm);
	}
	
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        return permission != null;
    }
	
	 private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
	
}
