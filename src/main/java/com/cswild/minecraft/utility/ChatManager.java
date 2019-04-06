package com.cswild.minecraft.utility;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.cswild.minecraft.vip.VipManager;

public class ChatManager {
	
	public static String colorParser(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public static void sendMsg(CommandSender who, String msg) {
		who.sendMessage(colorParser(msg));
	}
	
	public static void sendMsgPrefix(CommandSender who, String msg) {
		who.sendMessage(colorParser(VipManager.config.getMainConfig().getString("plugin_prefix")+" "+msg));
	}
}
