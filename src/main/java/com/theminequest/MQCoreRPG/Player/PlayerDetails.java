/**
 * This file, PlayerDetails.java, is part of MineQuest:
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

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;

import com.theminequest.MQCoreRPG.MQCoreRPG;
import com.theminequest.MQCoreRPG.API.Abilities.Ability;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerClassEvent;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerExperienceEvent;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerHealthEvent;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerLevelEvent;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerPowerEvent;
import com.theminequest.MQCoreRPG.Class.ClassDetails;
import com.theminequest.MineQuest.MineQuest;
import com.theminequest.MineQuest.Quest.Quest;
import com.theminequest.MineQuest.Utils.PropertiesFile;
import com.theminequest.MineQuest.Backend.GroupBackend;

/**
 * Extra details about the Player
 * 
 * @author MineQuest
 * 
 */
public class PlayerDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2315094916617378789L;

	private boolean abilitiesEnabled;
	// >_>
	public Map<Ability,Long> abilitiesCoolDown;
	// end >_>

	protected volatile boolean giveMana;

	// player properties
	private String name;
	private long health;
	private long power;
	private int level;
	private long exp;
	private String classid;

	public PlayerDetails(Player p) {
		name = p.getName();
		abilitiesEnabled = false;
		abilitiesCoolDown = Collections.synchronizedMap(new LinkedHashMap<Ability,Long>());
		classid = "default";
		level = 1;
		exp = 0;
		health = getMaxHealth();
		power = Math.round(getMaxPower()*0.75);
		giveMana = true;
		updateMinecraftView();
	}

	public Player getPlayer(){
		return Bukkit.getPlayerExact(name);
	}

	public synchronized int getLevel(){
		return level;
	}

	public synchronized void setLevel(int i) {
		level = i;
		PlayerLevelEvent event = new PlayerLevelEvent(getPlayer());
		Bukkit.getPluginManager().callEvent(event);
	}

	public synchronized void levelUp(){
		level+=1;
		PlayerLevelEvent event = new PlayerLevelEvent(getPlayer());
		Bukkit.getPluginManager().callEvent(event);
		exp = exp-(MQCoreRPG.classManager.getClassDetail(classid).getBaseExp()*(level-1));
		if (exp<0)
			exp = 0;
	}

	public synchronized long getExperience(){
		return exp;
	}

	public synchronized long getMaxExperience(){
		return MQCoreRPG.classManager.getClassDetail(classid).getBaseExp()*level;
	}

	public synchronized void modifyExperienceBy(int e){
		exp+=e;
		PlayerExperienceEvent event = new PlayerExperienceEvent(getPlayer(), e);
		Bukkit.getPluginManager().callEvent(event);
		while (exp>=getMaxExperience())
			levelUp();
	}

	public synchronized String getClassID(){
		return classid;
	}

	public synchronized void setClassID(String classid){
		this.classid = classid;
		PlayerClassEvent e = new PlayerClassEvent(getPlayer());
		Bukkit.getPluginManager().callEvent(e);
	}

	public synchronized long getPower(){
		return power;
	}

	public synchronized long getMaxPower(){
		return MQCoreRPG.classManager.getClassDetail(classid).getBasePower()*level;
	}

	public synchronized void modifyPowerBy(int m){
		long powertoadd = m;
		if (power==MQCoreRPG.classManager.getClassDetail(classid).getBasePower()*level)
			return;
		else if (m+power>(getMaxPower()))
			powertoadd = (getMaxPower())-(m+power);
		power+=powertoadd;
		PlayerPowerEvent event = new PlayerPowerEvent(getPlayer(),m);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			power-=powertoadd;
	}

	public synchronized long getHealth(){
		return health;
	}

	public synchronized long getMaxHealth(){
		return MQCoreRPG.classManager.getClassDetail(classid).getBaseHealth()*level;
	}
	
	public synchronized void setHealth(long l){
		setHealth(l,true);
	}

	public synchronized void setHealth(long l, boolean effect){
		boolean hurt = false;
		if (l<this.health)
			hurt = true;
		this.health = l;
		PlayerHealthEvent e = new PlayerHealthEvent(getPlayer(),this);
		Bukkit.getPluginManager().callEvent(e);
		if (effect && hurt)
			getPlayer().playEffect(EntityEffect.HURT);
	}

	/*
	 * A user should be able to toggle ability use on/off
	 * with a command, like /ability on/off?
	 */
	public synchronized boolean getAbilitiesEnabled(){
		return abilitiesEnabled;
	}

	public synchronized void setAbilitiesEnabled(boolean b){
		abilitiesEnabled = b;
	}

	public synchronized void updateMinecraftView(){
		if (!getPlayer().isDead()){
			getPlayer().setExp(getMinecraftLevelExp(getExperience(),getLevel()));
			getPlayer().setLevel(getLevel());
			getPlayer().setFoodLevel(getMinecraftFood(getPower()));
			getPlayer().setHealth(getMinecraftHealth(getHealth()));
		}
	}
	
	public synchronized float getMinecraftLevelExp(long exp, int level){
		return ((float)getExperience()/getMaxExperience());
	}

	public synchronized int getMinecraftFood(long power){
		double percentage = ((double)power)/getMaxPower();
		int f = (int) Math.floor((double)20*percentage);
		if (f>20)
			f=20;
		return f;
	}

	public synchronized int getMinecraftHealth(long health){
		double percentage = ((double)health)/getMaxHealth();
		int h = (int) Math.floor((double)20*percentage);
		if (h>20)
			h=20;
		return h;
	}

}
