package com.theminequest.MQCoreRPG;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.theminequest.MQCoreRPG.Ability.AbilityHandler;
import com.theminequest.MQCoreRPG.Ability.AbilityManager;
import com.theminequest.MQCoreRPG.Class.ClassManager;
import com.theminequest.MQCoreRPG.Commands.PlayerCommandFrontend;
import com.theminequest.MQCoreRPG.Commands.StopExecutor;
import com.theminequest.MQCoreRPG.Player.PlayerManager;
import com.theminequest.MQCoreRPG.QEvents.AssignClassEvent;
import com.theminequest.MQCoreRPG.QEvents.RewardExpEvent;
import com.theminequest.MQCoreRPG.SpoutPlugin.PopupManager;
import com.theminequest.MQCoreRPG.SpoutPlugin.TitleManager;
import com.theminequest.MineQuest.MineQuest;
import com.theminequest.MineQuest.API.Managers;
import com.theminequest.MineQuest.API.Utils.PropertiesFile;

public class MQCoreRPG extends JavaPlugin {
	
	public static MQCoreRPG activePlugin = null;
	public static PlayerManager playerManager = null;
	public static AbilityManager abilityManager = null;
	public static ClassManager classManager = null;
	public static PropertiesFile configuration = null;
	
	public static PopupManager popupManager = null;
	
	@Override
	public void onEnable() {
		Managers.log("[RPG] Starting RPG addon...");
		// must be first at all costs to prevent /stop from working.
		getCommand("stop").setExecutor(new StopExecutor());
		activePlugin = this;
		configuration = new PropertiesFile(Managers.getActivePlugin().getDataFolder() + File.separator + "rpg.properties");
		
		// allow configurable toggling
		if (configuration.getBoolean("enable_RPGSystem", true)) {
			playerManager = new PlayerManager();
			getServer().getPluginManager().registerEvents(playerManager, this);
			Managers.getEventManager().addEvent("RewardExpEvent", RewardExpEvent.class);
			classManager = new ClassManager();
			getServer().getPluginManager().registerEvents(classManager, this);
			Managers.getEventManager().addEvent("AssignClassEvent", AssignClassEvent.class);
		}
		
		if (configuration.getBoolean("enable_AbilitySystem", true)) {
			abilityManager = new AbilityManager();
			getServer().getPluginManager().registerEvents(abilityManager, this);
			Managers.getQuestManager().getParser().addClassHandler("bannedabilities", AbilityHandler.class);
		}
		
		if (configuration.getBoolean("enable_SpoutFeatures", true)) {
			try {
				getServer().getPluginManager().registerEvents(new TitleManager(), this);
				popupManager = new PopupManager();
				getServer().getPluginManager().registerEvents(popupManager, this);
			} catch (ClassNotFoundException e) {
				Managers.log(Level.WARNING, "[Title/Popup] Unable to start managers; No SpoutPlugin found.");
			}
		}
		
		getCommand("player").setExecutor(new PlayerCommandFrontend());
		MineQuest.commandListener.helpmenu.put("player", "(RPG) List player commands.");
	}
	
	@Override
	public void onDisable() {
		popupManager = null;
		if (playerManager != null) {
			playerManager.saveAll();
			playerManager.shutdown();
		}
		playerManager = null;
		abilityManager = null;
		classManager = null;
		configuration = null;
		activePlugin = null;
	}
	
}
