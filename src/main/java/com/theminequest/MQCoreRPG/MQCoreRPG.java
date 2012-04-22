package com.theminequest.MQCoreRPG;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.theminequest.MQCoreRPG.API.Abilities.AbilityHandler;
import com.theminequest.MQCoreRPG.API.Abilities.AbilityManager;
import com.theminequest.MQCoreRPG.Class.ClassManager;
import com.theminequest.MQCoreRPG.Commands.PlayerCommandFrontend;
import com.theminequest.MQCoreRPG.Commands.StopExecutor;
import com.theminequest.MQCoreRPG.Player.PlayerManager;
import com.theminequest.MQCoreRPG.QEvents.RewardExpEvent;
import com.theminequest.MQCoreRPG.SpoutPlugin.TitleManager;
import com.theminequest.MineQuest.MineQuest;
import com.theminequest.MineQuest.Utils.PropertiesFile;

public class MQCoreRPG extends JavaPlugin {
	
	public static PlayerManager playerManager = null;
	public static AbilityManager abilityManager = null;
	public static ClassManager classManager = null;
	public static PropertiesFile configuration = null;
	
	@Override
	public void onEnable(){
		MineQuest.log("[RPG] Starting RPG addon...");
		configuration = new PropertiesFile(MineQuest.activePlugin.getDataFolder()+File.separator+"rpg.properties");
		playerManager = new PlayerManager();
		getServer().getPluginManager().registerEvents(playerManager, this);
		abilityManager = new AbilityManager();
		getServer().getPluginManager().registerEvents(abilityManager, this);
		classManager = new ClassManager();
		getServer().getPluginManager().registerEvents(classManager, this);
		try {
			getServer().getPluginManager().registerEvents(new TitleManager(), this);
		} catch (ClassNotFoundException e) {
			MineQuest.log(Level.WARNING, "[Title] Unable to start manager; No SpoutPlugin found.");
		}
		getCommand("player").setExecutor(new PlayerCommandFrontend());
		getCommand("stop").setExecutor(new StopExecutor());
		MineQuest.eventManager.registerEvent("RewardExpEvent", RewardExpEvent.class);
		MineQuest.questManager.parser.addClassHandler("bannedabilities", AbilityHandler.class);
	}

	@Override
	public void onDisable() {
		playerManager.saveAll();
		playerManager = null;
		abilityManager = null;
		classManager = null;
		configuration = null;
	}

}
