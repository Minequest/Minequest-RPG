package com.theminequest.MQCoreRPG.Class;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.theminequest.MQCoreRPG.MQCoreRPG;
import com.theminequest.MQCoreRPG.Player.PlayerDetails;
import com.theminequest.MineQuest.API.Managers;

public class ClassManager implements Listener {

	public static final String loc = Managers.getActivePlugin().getDataFolder()
			+ File.separator + "classes";
	private LinkedHashMap<String, ClassDetails> classes;

	public ClassManager() {
		Managers.log("[Class] Starting Manager...");
		classes = new LinkedHashMap<String, ClassDetails>();
		initialize();
		ClassDetails def = new ClassDetails();
		def.saveDefault();
		if (!classes.containsKey("default"))
			classes.put("default", def);
	}

	public boolean hasClassDetail(String name) {
		return classes.containsKey(name);
	}

	public ClassDetails getClassDetail(String name) {
		return classes.get(name);
	}

	private void initialize() {
		File f = new File(loc);
		if (!f.exists())
			f.mkdirs();
		File[] cfiles = f.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".cspec");
			}

		});
		for (File c : cfiles) {
			try {
				ClassDetails d = new ClassDetails(c);
				classes.put(d.getName(), d);
			} catch (IOException e) {
				Managers.log(Level.SEVERE,
						"[Class] Class Specification " + c.getName()
								+ " is invalid.");
			}
		}
	}

	// FIXME
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		double amt = Math.random();
		if (amt > 0.25)
			amt = 0.25;
		PlayerDetails d = MQCoreRPG.playerManager.getPlayerDetails(e
				.getEntity());
		int rmexp = (int) Math.round(d.getExperience() * amt);
		if (MQCoreRPG.popupManager != null)
			MQCoreRPG.popupManager.triggerNotificationPopup(e.getEntity(),
					e.getDeathMessage() + "\nLost " + rmexp + " exp.");
		d.modifyExperienceBy(-1 * rmexp);
	}

	// FIXME
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent e) {
		EntityDamageEvent c = e.getEntity().getLastDamageCause();
		if (!(c instanceof EntityDamageByEntityEvent))
			return;
		EntityDamageByEntityEvent cause = (EntityDamageByEntityEvent) c;
		double amt = Math.random();
		if (!(cause.getDamager() instanceof Player))
			return;
		PlayerDetails d = MQCoreRPG.playerManager
				.getPlayerDetails((Player) cause.getDamager());
		ClassDetails cd = MQCoreRPG.classManager.getClassDetail(d.getClassID());
		int addexp = (int) Math.round(cd.getDamageFromCause(cause.getCause())
				* amt);
		if (MQCoreRPG.popupManager != null) {
			MQCoreRPG.popupManager.triggerExpPopup((Player) cause.getDamager(),
					addexp);
			MQCoreRPG.popupManager.triggerNotificationPopup(
					(Player) cause.getDamager(), "Defeated "
							+ e.getEntityType().getName() + "!\nGained "
							+ addexp + " exp.");
		}
		d.modifyExperienceBy(addexp);
	}
}
