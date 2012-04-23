package com.theminequest.MQCoreRPG.SpoutPlugin;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.gui.ContainerType;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.theminequest.MQCoreRPG.MQCoreRPG;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerExperienceEvent;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerHealthEvent;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerLevelEvent;
import com.theminequest.MQCoreRPG.BukkitEvents.PlayerPowerEvent;
import com.theminequest.MQCoreRPG.Player.PlayerDetails;
import com.theminequest.MineQuest.MineQuest;

public class HUDManager implements Listener {
	
	private Map<Player,GenericContainer> container;
	private Map<Player,GenericLabel> health;
	private Map<Player,GenericLabel> power;
	private Map<Player,GenericLabel> lvlclass;
	private Map<Player,GenericLabel> exp;

	public HUDManager() throws ClassNotFoundException{
		Class.forName("org.getspout.spoutapi.Spout");
		MineQuest.log("[HUD] Starting Manager...");
		container = new LinkedHashMap<Player,GenericContainer>();
		health = new LinkedHashMap<Player,GenericLabel>();
		power = new LinkedHashMap<Player,GenericLabel>();
		lvlclass = new LinkedHashMap<Player,GenericLabel>();
		exp = new LinkedHashMap<Player,GenericLabel>();
	}
	
	@EventHandler
	public void onPlayerHealth(PlayerHealthEvent e){
		upPlayerHealthWidget(e.getPlayer());
	}
	
	private void upPlayerHealthWidget(Player p){
		PlayerDetails d = MQCoreRPG.playerManager.getPlayerDetails(p);
		GenericLabel hl = health.get(p);
		long h = d.getHealth();
		long mh = MQCoreRPG.classManager.getClassDetail(d.getClassID()).getBaseHealth()*d.getLevel();
		String ch = ChatColor.GREEN.toString();
		if ((mh*0.25)>h)
			ch = ChatColor.RED.toString();
		else if ((mh*0.50)>h)
			ch = ChatColor.GOLD.toString();
		else if ((mh*0.75)>h)
			ch = ChatColor.YELLOW.toString();
		hl.setText(ch + "Health: " + h + "/" + mh);
		hl.setAlign(WidgetAnchor.TOP_RIGHT);
		hl.setDirty(true);
	}
	
	@EventHandler
	public void onPlayerLevel(PlayerLevelEvent e){
		upPlayerLevelWidget(e.getPlayer());
	}

	private void upPlayerLevelWidget(Player p){
		PlayerDetails d = MQCoreRPG.playerManager.getPlayerDetails(p);
		GenericLabel ll = lvlclass.get(p);
		int level = d.getLevel();
		String clazz = MQCoreRPG.classManager.getClassDetail(d.getClassID()).getDisplayName();
		ll.setText(ChatColor.GREEN + "" + level + " " + ChatColor.GRAY + clazz);
		ll.setAlign(WidgetAnchor.TOP_RIGHT);
		ll.setDirty(true);
	}
	
	@EventHandler
	public void onPlayerPower(PlayerPowerEvent e){
		upPlayerPowerWidget(e.getPlayer());
	}
	
	private void upPlayerPowerWidget(Player p){
		PlayerDetails d = MQCoreRPG.playerManager.getPlayerDetails(p);
		GenericLabel pla = power.get(p);
		long pl = d.getPower();
		long mp = MQCoreRPG.classManager.getClassDetail(d.getClassID()).getBasePower()*d.getLevel();
		String cp = ChatColor.GREEN.toString();
		if ((mp*0.25)>pl)
			cp = ChatColor.RED.toString();
		else if ((mp*0.50)>pl)
			cp = ChatColor.GOLD.toString();
		else if ((mp*0.75)>pl)
			cp = ChatColor.YELLOW.toString();
		pla.setText(cp + "Power: " + p + "/" + mp);
		pla.setAlign(WidgetAnchor.TOP_RIGHT);
		pla.setDirty(true);
	}
	
	@EventHandler
	public void onPlayerExperience(PlayerExperienceEvent e){
		upPlayerExperienceWidget(e.getPlayer());
	}
	
	private void upPlayerExperienceWidget(Player p){
		PlayerDetails d = MQCoreRPG.playerManager.getPlayerDetails(p);
		GenericLabel e = exp.get(p);
		long ex = d.getExperience();
		long mex = MQCoreRPG.classManager.getClassDetail(d.getClassID()).getBaseExp()*d.getLevel();
		e.setText(ChatColor.GRAY + "Experience: " + ex + "/" + mex);
		e.setDirty(true);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		if (!((SpoutPlayer)e.getPlayer()).isSpoutCraftEnabled())
			return;
		createAndSetWidget(e.getPlayer());
		upPlayerHealthWidget(e.getPlayer());
		upPlayerPowerWidget(e.getPlayer());
		upPlayerLevelWidget(e.getPlayer());
		upPlayerExperienceWidget(e.getPlayer());
	}
	
	private void createAndSetWidget(Player player){
		// Let's contain everything in this.
		GenericContainer cont = new GenericContainer();
		cont.setAlign(WidgetAnchor.TOP_RIGHT).setAnchor(WidgetAnchor.BOTTOM_RIGHT);
		cont.setDirty(true);
		cont.setLayout(ContainerType.HORIZONTAL);
		cont.setPriority(RenderPriority.Highest);
		
		GenericLabel h = (GenericLabel) new GenericLabel().setResize(true).setFixed(true);
		health.put(player, h);
		cont.addChild(h);
		
		// How about power level?
		GenericLabel p = (GenericLabel) new GenericLabel().setResize(true).setFixed(true);
		power.put(player, p);
		cont.addChild(p);
		
		// Class and Level
		GenericLabel l = (GenericLabel) new GenericLabel().setResize(true).setFixed(true);
		lvlclass.put(player,l);
		cont.addChild(l);
		
		// and finally, EXP
		GenericLabel e = (GenericLabel) new GenericLabel().setResize(true).setFixed(true);
		exp.put(player, e);
		cont.addChild(e);

		((SpoutPlayer)player).getMainScreen().attachWidget(MQCoreRPG.activePlugin, cont);
		container.put(player, cont);
	}

}
