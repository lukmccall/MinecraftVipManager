package com.cswild.minecraft.vip;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.cswild.minecraft.utility.ChatManager;

//import org.bukkit.event.player.PlayerMoveEvent;

public class EventManager implements Listener{
	
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		if(!PlayerManager.checkAndAddVip(event.getPlayer()))
			ChatManager.sendMsgPrefix(event.getPlayer(), VipManager.config.getLangConfig().getString("could_not_add_to_group"));
	}
	
	@EventHandler
	public void OnPlayerDisconnect(PlayerQuitEvent event) {
		PlayerManager.removeVipOnDisconnect(event.getPlayer());
	}
}
