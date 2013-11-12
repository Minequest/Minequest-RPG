package com.theminequest.MQCoreRPG.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class HealthPackUseEvent extends PlayerEvent
{
        private static final HandlerList handlers = new HandlerList();
                
        public HealthPackUseEvent(Player player)
        {
            super(player);
        }
        
	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
    
}
