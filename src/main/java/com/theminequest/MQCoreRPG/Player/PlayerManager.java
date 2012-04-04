/**
 * This file, PlayerManager.java, is part of MineQuest:
 * A full featured and customizable quest/mission system.
 * Copyright (C) 2012 The MineQuest Team
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
package com.theminequest.MQCoreRPG.Player;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.theminequest.MQCoreRPG.MQCoreRPG;
import com.theminequest.MineQuest.MineQuest;
import com.theminequest.MineQuest.BukkitEvents.QuestAvailableEvent;
import com.theminequest.MineQuest.BukkitEvents.QuestCompleteEvent;
import com.theminequest.MineQuest.BukkitEvents.GroupInviteEvent;

public class PlayerManager implements Listener {

	private LinkedHashMap<Player,PlayerDetails> players;
	private Object playerlock;
	private volatile boolean shutdown;
	private volatile boolean chill;

	public PlayerManager(){
		MineQuest.log("[Player] Starting Manager...");
		players = new LinkedHashMap<Player,PlayerDetails>();
		playerlock = new Object();
		shutdown = false;
		chill = false;

		Bukkit.getScheduler().scheduleSyncRepeatingTask(MineQuest.activePlugin, new Runnable(){

			@Override
			public void run() {
				saveAll();
				MineQuest.log("[Player] Routine Record Save Finished.");
			}

		}, 1200, 18000);

		Runnable r = new Runnable(){

			@Override
			public void run() {
				Random r = new Random();
				while (!shutdown){
					if (!chill){
						chill = true;
						synchronized(playerlock){
							for (PlayerDetails d : players.values()){
								d.modifyManaBy(1);
							}
						}
						chill = false;
					}
					try {
						Thread.sleep(r.nextInt(2000)+4000);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}

		};

		Thread t = new Thread(r);
		t.setDaemon(true);
		t.setName("MineQuest-PlayerMana");
		t.start();
	}

	public void shutdown(){
		shutdown = true;
	}

	public void saveAll(){
		synchronized(playerlock){
			for (PlayerDetails d : players.values())
				d.save();
		}
	}

	private void playerAcct(Player p){
		synchronized(playerlock){
			if (!players.containsKey(p)){
				try {
					players.put(p,new PlayerDetails(p));
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			} else {
				players.get(p).reload();
			}
		}
	}

	public PlayerDetails getPlayerDetails(Player p){
		synchronized(playerlock){
			playerAcct(p);
			return players.get(p);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e){
		MineQuest.log("[Player] Retrieving details for player " + e.getPlayer().getName());
		synchronized(playerlock){
			playerAcct(e.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e){
		MineQuest.log("[Player] Saving details for player " + e.getPlayer().getName());
		synchronized(playerlock){
			getPlayerDetails(e.getPlayer()).save();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerKick(PlayerKickEvent e){
		MineQuest.log("[Player] Saving details for player " + e.getPlayer().getName());
		synchronized(playerlock){
			getPlayerDetails(e.getPlayer()).save();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent e){
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		PlayerDetails d = getPlayerDetails(p);
		e.setFoodLevel(d.getMinecraftMana(d.getMana()));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityRegainHealthEvent(EntityRegainHealthEvent e){
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		PlayerDetails d = getPlayerDetails(p);
		int amount = e.getAmount();
		long total = d.getHealth()+amount;
		if (total>d.getMaxHealth())
			total = d.getMaxHealth();
		int minecrafthealth = d.getMinecraftHealth(total);
		int minecraftcurrent = p.getHealth();
		e.setAmount(minecrafthealth-minecraftcurrent);
	}
	
	// Damage START
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e){
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		PlayerDetails d = getPlayerDetails(p);
		int amount = e.getDamage();
		long total = d.getHealth()-amount;
		if (total<0)
			total = 0;
		int minecrafthealth = d.getMinecraftHealth(total);
		int minecraftcurrent = p.getHealth();
		e.setDamage(minecraftcurrent-minecrafthealth);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByBlockEvent(EntityDamageByBlockEvent e){
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		PlayerDetails d = getPlayerDetails(p);
		int amount = e.getDamage();
		long total = d.getHealth()-amount;
		if (total<0)
			total = 0;
		int minecrafthealth = d.getMinecraftHealth(total);
		int minecraftcurrent = p.getHealth();
		e.setDamage(minecraftcurrent-minecrafthealth);
	}
	
	// Damage END
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerExpChangeEvent(PlayerExpChangeEvent e){
		e.setAmount(0);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent e){
		e.setDroppedExp(0);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent e){
		e.setDroppedExp(0);
	}
	
}
