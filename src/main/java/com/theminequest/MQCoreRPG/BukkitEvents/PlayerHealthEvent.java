package com.theminequest.MQCoreRPG.BukkitEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.theminequest.MQCoreRPG.Player.PlayerDetails;

public class PlayerHealthEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private Player player;
	private PlayerDetails details;

	public PlayerHealthEvent(Player p, PlayerDetails d) {
		player = p;
	}

	public Player getPlayer() {
		return player;
	}

	public PlayerDetails getDetails() {
		return details;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
