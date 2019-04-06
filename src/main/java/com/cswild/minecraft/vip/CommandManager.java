package com.cswild.minecraft.vip;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.cswild.minecraft.utility.ChatManager;
import org.bukkit.entity.Player;

//import net.minecraft.server.v1_12_R1.IChatBaseComponent;
//import net.minecraft.server.v1_12_R1.IChatBaseComponent.ChatSerializer;

public class CommandManager implements CommandExecutor{
	public static String cmd = "vipmanager";
	
	public void executeFail(CommandSender sender) {
		ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("dont_have_permission"));
	}
			
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(command.getName().equalsIgnoreCase("vipmanager")) {
			if(args.length > 0) {
				if( args[0].equalsIgnoreCase("reload")) {
					if(sender.hasPermission("vipmanager.reload")) {
						VipManager.config.reloadConfig();
						VipManager.mysql.setup();
						ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("reload"));
					} else 
						executeFail(sender);
					return true;
				} else if(args[0].equalsIgnoreCase("add")) {
					if(sender.hasPermission("vipmanger.add")) {
						if(args.length < 2) {
							ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("invalid_command"));
							return true;
						}
						OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(args[1]);
						if(target == null) {
							ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("could_not_find_player"));
							return false;
						}
						if(args.length == 4) {
							int days = Integer.parseInt(args[3]);
							VipManager.mysql.PlayerAddGroup(sender, target, args[2], days);
						} else {
							VipManager.mysql.PlayerAddGroup(sender, target, args[2]);
						}
						if(target.isOnline())
							PlayerManager.checkAndAddVip(Bukkit.getServer().getPlayer(args[1]));
						
						return true;
					} else
						executeFail(sender);
				}
				if(sender instanceof Player) {
					if( args[0].equalsIgnoreCase("uuid")) {
//						 IChatBaseComponent comp = ChatSerializer.a("{\"text\":[COPY], color: \"red\", bold: \"true\", clickEvent:}");
						if(args.length == 1)
							ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("your_uuid") + ((Player)sender).getUniqueId().toString());
						else {
							Player target = Bukkit.getServer().getPlayer(args[1]);
							if(target == null)
								ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("could_not_find_player"));
							else 
								ChatManager.sendMsgPrefix(sender, ChatColor.GREEN + args[1] + " uuid: " + ChatColor.DARK_BLUE + target.getUniqueId().toString()); 
						}
						return true;
					} else if( args[0].equalsIgnoreCase("info")) {
						if(args.length ==  1) {
							ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("your_groups"));
							for(String s : VipManager.permission.getPlayerGroups((Player)sender))
								ChatManager.sendMsg(sender, "- &f"+s);
						} else {
							Player target = Bukkit.getServer().getPlayer(args[1]);
							if(target == null)
								ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("could_not_find_player"));
							else {
								ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("player_groups") + "&6"+target.getName()+":");
								for(String s : VipManager.permission.getPlayerGroups(target))
									ChatManager.sendMsg(sender, "- &f"+s);
							}
						} 
						
						return true;	
					} else if( args[0].equalsIgnoreCase("active")) {
						PlayerManager.checkAndAddVip((Player)sender);
						ChatManager.sendMsgPrefix(sender, VipManager.config.getLangConfig().getString("active"));
					}
				}
			} else 
				ChatManager.sendMsg(sender, command.getUsage());
			return true;
		}
		
		return true;
	}
	
}
