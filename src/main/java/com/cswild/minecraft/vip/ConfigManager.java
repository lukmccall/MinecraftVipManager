package com.cswild.minecraft.vip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.cswild.minecraft.utility.ChatManager;


public class ConfigManager {

	private FileConfiguration mainConfig;
	private File mainFile;
	private FileConfiguration langConfig;
	private File langFile;
	
	public ConfigManager() {
		setUp();
			
	}
	
	public void setUp() {
		try {
			mainSetUp();
			langSetUp();
		} catch (IOException e) {
			Bukkit.getServer().getConsoleSender().sendMessage("[VipManger]"+ChatColor.RED+" could not create config files");
		}
	}
	
	public void reloadConfig() {
		reloadMainConfig();
		reloadLangConfig();
	}
	
	private void mainSetUp() throws IOException{
		if(!VipManager.instance.getDataFolder().exists())
			VipManager.instance.getDataFolder().mkdir();
		
		mainFile = new File(VipManager.instance.getDataFolder(), "config.yml");
		if(!mainFile.exists()) {
			mainFile.createNewFile();
			ChatManager.sendMsg(Bukkit.getServer().getConsoleSender(),"[VipManager]" + ChatColor.GREEN + " config.yml create");
		}
		
    	reloadMainConfig();
	}
	
	private void langSetUp() throws IOException{
		File folder = new File(VipManager.instance.getDataFolder(), "lang/");
		if(!folder.exists())
			folder.mkdir();
		
		langFile = new File(VipManager.instance.getDataFolder(),"lang/"+mainConfig.getString("lang")+".yml");
		if(!langFile.exists()) {
			langFile.createNewFile();
			ChatManager.sendMsg(Bukkit.getServer().getConsoleSender(),"[VipManager]" + ChatColor.GREEN + " lang file create");
		}
		
		reloadLangConfig();
	}
	
	public FileConfiguration getMainConfig() {
		return mainConfig;
	}
	
	public FileConfiguration getLangConfig() {
		return langConfig;
	}
	
	public void saveMainConfig() {
		try {
			mainConfig.save(mainFile);
		} catch(IOException e) {
			ChatManager.sendMsgPrefix(Bukkit.getServer().getConsoleSender(), ChatColor.RED + "could not save config.yml");
		}
	}
	
	public void saveLangConfig() {
		try {
			langConfig.save(langFile);
		} catch(IOException e) {
			ChatManager.sendMsgPrefix(Bukkit.getServer().getConsoleSender(), ChatColor.RED + "could not save lang file");
		}
	}
	
	private void reloadMainConfig() {
		mainConfig = YamlConfiguration.loadConfiguration(mainFile);	
		 // Look for defaults in the jar
	    Reader defConfigStream = null;
		try {
			defConfigStream = new InputStreamReader(VipManager.instance.getResource("config.yml"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        mainConfig.setDefaults(defConfig);
	        mainConfig.options().copyDefaults(true);
	        saveMainConfig();
	        try {
				defConfigStream.close();
			} catch (IOException e) {
				ChatManager.sendMsgPrefix(Bukkit.getServer().getConsoleSender(), ChatColor.RED + "could not save def config.yml");
			}
	    }
	}
	
	private void reloadLangConfig() {
		langConfig = YamlConfiguration.loadConfiguration(langFile);	
		 // Look for defaults in the jar
	    Reader defConfigStream = null;
		try {
			InputStream res = VipManager.instance.getResource("lang/"+mainConfig.getString("lang")+".yml");
			if(res!=null)
				defConfigStream = new InputStreamReader(res, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        langConfig.setDefaults(defConfig);
	        langConfig.options().copyDefaults(true);
	        saveLangConfig();
	        try {
				defConfigStream.close();
			} catch (IOException e) {
				ChatManager.sendMsgPrefix(Bukkit.getServer().getConsoleSender(), ChatColor.RED + "could not save def lang file");
			}
	    }
	}
	
}
