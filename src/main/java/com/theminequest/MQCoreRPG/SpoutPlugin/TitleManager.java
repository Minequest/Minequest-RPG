package com.theminequest.MQCoreRPG.SpoutPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.getspout.spoutapi.Spout;

import com.theminequest.MQCoreRPG.MQCoreRPG;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerLevelEvent;
import com.theminequest.MQCoreRPG.Player.PlayerDetails;
import com.theminequest.MineQuest.API.Managers;

public class TitleManager implements Listener {
	
	public TitleManager() throws ClassNotFoundException {
		Class.forName("org.getspout.spoutapi.Spout");
		Managers.log("[Title] Starting Manager...");
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(final PlayerJoinEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(MQCoreRPG.activePlugin, new Runnable() {
			
			@Override
			public void run() {
				PlayerDetails d = MQCoreRPG.playerManager.getPlayerDetails(e.getPlayer());
				int lvl = d.getLevel();
				String clazz = MQCoreRPG.classManager.getClassDetail(d.getClassID()).getDisplayName();
				Spout.getServer().setTitle(e.getPlayer(), ChatColor.GRAY + "[" + ChatColor.GREEN + lvl + ChatColor.GRAY + "] " + clazz + "\n" + e.getPlayer().getDisplayName());
			}
			
		});
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onLevelUp(PlayerLevelEvent e) {
		PlayerDetails d = MQCoreRPG.playerManager.getPlayerDetails(e.getPlayer());
		int lvl = d.getLevel();
		String clazz = MQCoreRPG.classManager.getClassDetail(d.getClassID()).getDisplayName();
		Spout.getServer().setTitle(e.getPlayer(), ChatColor.GRAY + "[" + ChatColor.GREEN + lvl + ChatColor.GRAY + "] " + clazz + "\n" + e.getPlayer().getDisplayName());
	}
	
}
