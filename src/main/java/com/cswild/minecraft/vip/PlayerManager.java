package com.cswild.minecraft.vip;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.cswild.minecraft.utility.ChatManager;

public class PlayerManager {
	public static HashMap<UUID, String> vips = new HashMap<UUID, String>(); 
	
	public static boolean checkAndAddVip(Player player) {
		String group = VipManager.mysql.getPlayerGroup(player);
	
		if(group!="") {
			vips.put(player.getUniqueId(), group);
			if(!VipManager.permission.playerAddGroup(null, player, group))
				return false;
			int money = VipManager.mysql.getPlayerMoneyToAdd(player);
			String commands = VipManager.mysql.getPlayerCommand(player);
			if(money > 0) {
				VipManager.economy.depositPlayer((OfflinePlayer)player, (double)money);
				VipManager.mysql.giveMoney(player);
			}
			
			if(commands != "") {
				String commmandsArray[] = commands.replaceAll("%player%", player.getName()).split("\\r?\\n");
				for(String cmd : commmandsArray) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
				}
				VipManager.mysql.executeCommands(player);
			}
		}
		return true;
	}
	
	public static boolean removeVipOnDisconnect(Player player) {
		if(vips.containsKey(player.getUniqueId())){
			String group = vips.remove(player.getUniqueId());
			return VipManager.permission.playerRemoveGroup(null, player, group);
		}
		return true;
	}
	
	public static void removeAllVip() {
		if(vips == null) return;
		vips.forEach(new BiConsumer<UUID, String>() {
			public void accept(UUID uuid, String group) {
				OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
				VipManager.permission.playerRemoveGroup(null, p, group);
			}
		});
			
		vips.clear();
	}
	
	public static void checkAndAddAllVip() {
		for(Player player : Bukkit.getOnlinePlayers()) 
			if(!PlayerManager.checkAndAddVip(player))
				ChatManager.sendMsgPrefix(player, VipManager.config.getLangConfig().getString("could_not_add_to_group"));
	}
}
