package com.theminequest.MQCoreRPG.Class;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.theminequest.MQCoreRPG.MQCoreRPG;
import com.theminequest.MineQuest.Utils.PropertiesFile;

public class ClassDetails {
	
	public static final int BASE_HEALTH = 100;
	public static final int BASE_EXP = 100;
	public static final int BASE_POWER = 100;
	
	private String name;
	private String displayname;
	private List<String> abilities;
	private int basepowerby;
	private int baseexpby;
	private int basehealthby;
	private Map<String,Integer> experience;
	private Map<String,Double> damagecost;
	
	protected ClassDetails(){
		name = "default";
		displayname = "Regular Person";
		abilities = new ArrayList<String>();
		basepowerby = 0;
		baseexpby = 0;
		basehealthby = 0;
		experience = new LinkedHashMap<String,Integer>();
		damagecost = new LinkedHashMap<String,Double>();
		for (EntityType e : EntityType.values()){
			experience.put(e.getName(), MQCoreRPG.configuration.getInt("EXP_def_drop_"+e.getName(), 10));
		}
		for (DamageCause d : DamageCause.values()){
			damagecost.put(d.name(), MQCoreRPG.configuration.getDouble("DMG_def_"+d.name(),10));
		}
	}
	
	protected ClassDetails(File f) throws IOException{
		this();
		PropertiesFile p = new PropertiesFile(f.getAbsolutePath());
		name = f.getName().substring(0, f.getName().indexOf(".cspec"));
		displayname = p.getString("displayname", name);
		String s = p.getString("abilities","");
		if (!s.equalsIgnoreCase("")){
			String[] sp = s.split(",");
			for (String l : sp)
				abilities.add(l);
		}
		basepowerby = p.getInt("powerdiff",0);
		baseexpby = p.getInt("expdiff",0);
		basehealthby = p.getInt("healthdiff",0);
		for (EntityType e : EntityType.values()){
			if (p.containsKey("EXP_drop_" + e.getName())){
				experience.remove(e.getName());
				experience.put(e.getName(), p.getInt("EXP_drop_"+e.getName()));
			}
		}
		for (DamageCause d : DamageCause.values()){
			if (p.containsKey("DMG_"+d.name())){
				damagecost.remove(d.name());
				damagecost.put(d.name(), p.getDouble("DMG_"+d.name()));
			}
		}
	}
	
	public boolean hasAbility(String name){
		return abilities.contains(name);
	}
	
	public String getName(){
		return name;
	}
	
	public String getDisplayName(){
		return displayname;
	}
	
	public List<String> getAbilities(){
		return abilities;
	}
	
	public int getBaseExp(){
		return BASE_EXP + baseexpby;
	}
	
	public int getBasePower(){
		return BASE_POWER + basepowerby;
	}
	
	public int getBaseHealth(){
		return BASE_HEALTH + basehealthby;
	}
	
	public int getExperienceFromEntityDeath(EntityType e){
		return experience.get(e.getName());
	}
	
	public double getDamageFromCause(DamageCause d){
		return damagecost.get(d.name());
	}

}
