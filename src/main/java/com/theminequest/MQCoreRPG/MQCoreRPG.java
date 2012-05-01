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
import com.theminequest.MQCoreRPG.QEvents.AssignClassEvent;
import com.theminequest.MQCoreRPG.QEvents.RewardExpEvent;
import com.theminequest.MQCoreRPG.SpoutPlugin.PopupManager;
import com.theminequest.MQCoreRPG.SpoutPlugin.TitleManager;
import com.theminequest.MineQuest.MineQuest;
import com.theminequest.MineQuest.Utils.PropertiesFile;

public class MQCoreRPG extends JavaPlugin {
	
	public static MQCoreRPG activePlugin = null;
	public static PlayerManager playerManager = null;
	public static AbilityManager abilityManager = null;
	public static ClassManager classManager = null;
	public static PropertiesFile configuration = null;
	
	public static PopupManager popupManager = null;
	
	@Override
	public void onEnable(){
		MineQuest.log("[RPG] Starting RPG addon...");
		// must be first at all costs to prevent /stop from working.
		getCommand("stop").setExecutor(new StopExecutor());
		activePlugin = this;
		configuration = new PropertiesFile(MineQuest.activePlugin.getDataFolder()+File.separator+"rpg.properties");
		playerManager = new PlayerManager();
		getServer().getPluginManager().registerEvents(playerManager, this);
		abilityManager = new AbilityManager();
		getServer().getPluginManager().registerEvents(abilityManager, this);
		classManager = new ClassManager();
		getServer().getPluginManager().registerEvents(classManager, this);
		try {
			getServer().getPluginManager().registerEvents(new TitleManager(), this);
			popupManager = new PopupManager();
			getServer().getPluginManager().registerEvents(popupManager, this);
		} catch (ClassNotFoundException e) {
			MineQuest.log(Level.WARNING, "[Title/Popup] Unable to start managers; No SpoutPlugin found.");
		}
		getCommand("player").setExecutor(new PlayerCommandFrontend());
		MineQuest.eventManager.registerEvent("RewardExpEvent", RewardExpEvent.class);
		MineQuest.eventManager.registerEvent("AssignClassEvent", AssignClassEvent.class);
		MineQuest.questManager.parser.addClassHandler("bannedabilities", AbilityHandler.class);
		MineQuest.commandListener.helpmenu.put("player", "Access player functions");
	}

	@Override
	public void onDisable() {
		popupManager = null;
		playerManager.saveAll();
		playerManager.shutdown();
		playerManager = null;
		abilityManager = null;
		classManager = null;
		configuration = null;
		activePlugin = null;
	}

}
