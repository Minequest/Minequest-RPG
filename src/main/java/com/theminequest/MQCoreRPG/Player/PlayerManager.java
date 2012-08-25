/**
 * This file, PlayerManager.java, is part of MineQuest:
 * A full featured and customizable quest/mission system.
 * Copyright (C) 2012 The MineQuest Party
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.theminequest.MQCoreRPG.MQCoreRPG;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerRegisterEvent;
import com.theminequest.MQCoreRPG.Class.ClassDetails;
import com.theminequest.MineQuest.API.Managers;

public class PlayerManager implements Listener {

	private Map<String, PlayerDetails> players;
	private volatile boolean shutdown;
	public static final int MAX_LEVEL = 50;

	public PlayerManager() {
		Managers.log("[Player] Starting Manager...");
		players = Collections
				.synchronizedMap(new LinkedHashMap<String, PlayerDetails>());
		shutdown = false;

		Managers.getStatisticManager().registerStatistic(PlayerDetails.class);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(
				Managers.getActivePlugin(), new Runnable() {

					@Override
					public void run() {
						saveAll();
					}

				}, 1200, 18000);

		Runnable r = new Runnable() {

			@Override
			public void run() {
				Random r = new Random();
				while (!shutdown) {
					for (PlayerDetails d : players.values()) {
						if (d.giveMana)
							d.modifyPowerBy((int) Math.round(1*d.getLevel()*(Math.random()+1)));
					}
					try {
						Thread.sleep(r.nextInt(10000) + 5000);
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

		Runnable run = new Runnable() {

			@Override
			public void run() {
				while (!shutdown) {
					synchronized (players) {
						for (PlayerDetails d : players.values()) {
							try {
								d.updateMinecraftView();
							} catch (NullPointerException e) {
								Managers.log(
										Level.WARNING,
										"[Player] Thread NPE! Can't keep up! Did the system time change, is the server overloaded, or has the player changed worlds?");
							}
						}
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}

		};

		Thread th = new Thread(run);
		th.setDaemon(true);
		th.setName("MineQuest-PlayerUpdateView");
		th.start();
	}

	public void shutdown() {
		shutdown = true;
	}

	public void saveAll() {
		synchronized (players) {
			for (PlayerDetails d : players.values()) {
				Managers.getStatisticManager().saveStatistic(d,
						PlayerDetails.class);
			}
		}
	}

	private void playerAcct(Player p) {
		if (!players.containsKey(p.getName())) {
			List<PlayerDetails> d = Managers.getStatisticManager().getAllStatistics(p.getName(), PlayerDetails.class);
			PlayerDetails obj;
			if (d.size()!=0){
				obj = d.get(0);
				obj.resetupPlayerDetails();
				players.put(p.getName(), obj);
				return;
			}
			obj = new PlayerDetails();
			obj.setupPlayerDetails(p);
			Managers.getStatisticManager().saveStatistic(obj,
					PlayerDetails.class);
			players.put(p.getName(), obj);
			PlayerRegisterEvent e = new PlayerRegisterEvent(p);
			Bukkit.getPluginManager().callEvent(e);
		}
	}

	public PlayerDetails getPlayerDetails(Player p) {
		playerAcct(p);
		return players.get(p.getName());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Managers.log("[Player] Retrieving details for player "
				+ e.getPlayer().getName());
		playerAcct(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Managers.log("[Player] Saving details for player "
				+ e.getPlayer().getName());
		Managers.getStatisticManager().saveStatistic(
				getPlayerDetails(e.getPlayer()), PlayerDetails.class);
		players.remove(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		PlayerDetails d = getPlayerDetails(p);
		d.modifyPowerBy(e.getFoodLevel() - p.getFoodLevel());
		e.setFoodLevel(d.getMinecraftFood(d.getPower()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityRegainHealthEvent(EntityRegainHealthEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		PlayerDetails d = getPlayerDetails(p);
		int amount = e.getAmount();
		long total = d.getHealth() + amount;
		switch (e.getRegainReason()) {
		case EATING:
		case MAGIC:
		case MAGIC_REGEN:
		case REGEN:
		case SATIATED:
			total = d.getHealth() + amount * d.getLevel();
			break;
		}
		if (total > d.getMaxHealth())
			total = d.getMaxHealth();
		int minecrafthealth = d.getMinecraftHealth(total);
		int minecraftcurrent = p.getHealth();
		e.setAmount(minecrafthealth - minecraftcurrent);
		d.setHealth(total);
	}

	// Damage START

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
		damageEvents(e);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamageByBlockEvent(EntityDamageByBlockEvent e) {
		damageEvents(e);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamageEvent(EntityDamageEvent e) {
		damageEvents(e);
	}

	private void damageEvents(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		Player p = (Player) e.getEntity();
		if (p.getGameMode() == GameMode.CREATIVE)
			return;
		PlayerDetails d = getPlayerDetails(p);
		if (d==null) // make way for hacky plugins!
			return;
		ClassDetails c = MQCoreRPG.classManager.getClassDetail(d.getClassID());
		int amount = c.getDamageFromCause(e.getCause()) + e.getDamage();
		long total = d.getHealth() - amount;
		if (total < 0)
			total = 0;
		int minecrafthealth = d.getMinecraftHealth(total);
		int minecraftcurrent = p.getHealth();
		e.setDamage(minecraftcurrent - minecrafthealth);
		switch (e.getCause()) {
		case FIRE:
		case CONTACT:
		case LAVA:
		case VOID:
			d.setHealth(total, false);
			break;
		default:
			d.setHealth(total);
			break;
		}
	}

	// Damage END

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerExpChangeEvent(PlayerExpChangeEvent e) {
		e.setAmount(0);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerEnchant(EnchantItemEvent e) {
		PlayerDetails p = getPlayerDetails(e.getEnchanter());
		p.modifyPowerBy((int) Math.round(-1 * (Math.random() * p.getPower())));
		e.setExpLevelCost(0);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(PlayerDeathEvent e) {
		e.setDroppedExp(0);
		PlayerDetails p = getPlayerDetails(e.getEntity());
		if (p==null) // make way for hacky plugins!
			return;
		p.setHealth(0);
		p.giveMana = false;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		PlayerDetails p = getPlayerDetails(e.getPlayer());
		p.setHealth(p.getMaxHealth());
		p.giveMana = true;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent e) {
		e.setDroppedExp(0);
	}

}
