package com.theminequest.MQCoreRPG;

import org.bukkit.plugin.java.JavaPlugin;

import com.theminequest.MQCoreRPG.API.Abilities.AbilityHandler;
import com.theminequest.MQCoreRPG.API.Abilities.AbilityManager;
import com.theminequest.MQCoreRPG.Class.ClassManager;
import com.theminequest.MQCoreRPG.Commands.PlayerCommandFrontend;
import com.theminequest.MQCoreRPG.Player.PlayerManager;
import com.theminequest.MQCoreRPG.QEvents.RewardExpEvent;
import com.theminequest.MineQuest.MineQuest;

public class MQCoreRPG extends JavaPlugin {
	
	public static PlayerManager playerManager = null;
	public static AbilityManager abilityManager = null;
	public static ClassManager classManager = null;
	
	@Override
	public void onEnable(){
		MineQuest.log("[RPG] Starting RPG addon...");
		playerManager = new PlayerManager();
		getServer().getPluginManager().registerEvents(playerManager, this);
		abilityManager = new AbilityManager();
		getServer().getPluginManager().registerEvents(abilityManager, this);
		classManager = new ClassManager();
		getServer().getPluginManager().registerEvents(classManager, this);
		getCommand("player").setExecutor(new PlayerCommandFrontend());
		MineQuest.eventManager.registerEvent("RewardExpEvent", RewardExpEvent.class);
		MineQuest.questManager.parser.addClassHandler("bannedabilities", AbilityHandler.class);
	}

	@Override
	public void onDisable() {
		playerManager.saveAll();
		playerManager = null;
		abilityManager = null;
		classManager = null;
	}

}
