package com.theminequest.MQCoreRPG.Mob;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import com.theminequest.MineQuest.API.Managers;

public class MobManager implements Listener {

	/**
	 * Specify
	 */
	public static final int VARY_HIGH = 5;
	public static final int VARY_LOW = 5;
	
	private Map<LivingEntity,MobDetails> details;

	public MobManager() {
		Managers.log("[Mob] Starting Manager...");
		details = new LinkedHashMap<LivingEntity,MobDetails>();
	}
	
	public MobDetails getMobDetails(LivingEntity e){
		return details.get(e);
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e){
		details.put(e.getEntity(), new MobDetails(e.getEntity()));
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e){
		details.remove(e.getEntity());
	}

}
