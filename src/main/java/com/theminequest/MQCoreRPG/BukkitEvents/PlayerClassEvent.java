package com.theminequest.MQCoreRPG.BukkitEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.theminequest.MQCoreRPG.MQCoreRPG;
import com.theminequest.MQCoreRPG.Player.PlayerDetails;

public class PlayerClassEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	private Player player;
	
	public PlayerClassEvent(Player p){
		player = p;
	}
	
	public PlayerDetails getDetails(){
		return MQCoreRPG.playerManager.getPlayerDetails(player);
	}
	
	public Player getPlayer(){
		return player;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
