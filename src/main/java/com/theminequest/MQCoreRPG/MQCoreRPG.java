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
		configuration = new PropertiesFile(Managers.getActivePlugin()
				.getDataFolder() + File.separator + "rpg.properties");
		playerManager = new PlayerManager();
		getServer().getPluginManager().registerEvents(playerManager, this);
		abilityManager = new AbilityManager();
		getServer().getPluginManager().registerEvents(abilityManager, this);
		classManager = new ClassManager();
		getServer().getPluginManager().registerEvents(classManager, this);
		try {
			getServer().getPluginManager().registerEvents(new TitleManager(),
					this);
			popupManager = new PopupManager();
			getServer().getPluginManager().registerEvents(popupManager, this);
		} catch (ClassNotFoundException e) {
			Managers.log(Level.WARNING,
					"[Title/Popup] Unable to start managers; No SpoutPlugin found.");
		}
		getCommand("player").setExecutor(new PlayerCommandFrontend());
		Managers.getEventManager().addEvent("RewardExpEvent",
				RewardExpEvent.class);
		Managers.getEventManager().addEvent("AssignClassEvent",
				AssignClassEvent.class);
		Managers.getQuestManager().getParser()
				.addClassHandler("bannedabilities", AbilityHandler.class);
		MineQuest.commandListener.helpmenu.put("player",
				"List player commands. (RPG)");
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
