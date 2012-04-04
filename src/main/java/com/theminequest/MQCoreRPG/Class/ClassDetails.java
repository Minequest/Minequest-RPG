package com.theminequest.MQCoreRPG.Class;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class ClassDetails {
	
	public static final int BASE_HEALTH = 100;
	public static final int BASE_EXP = 100;
	public static final int BASE_MANA = 100;
	
	private String name;
	private String displayname;
	private List<String> abilities;
	private int basemanaby;
	private int baseexpby;
	private int basehealthby;
	
	protected ClassDetails(){
		name = "default";
		displayname = "Regular Person";
		abilities = new ArrayList<String>();
		basemanaby = 0;
		baseexpby = 0;
		basehealthby = 0;
	}
	
	protected ClassDetails(File f) throws IOException{
		YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
		name = f.getName().substring(0, f.getName().indexOf(".cspec"));
		displayname = y.getString("displayname", name);
		abilities = y.getStringList("abilities");
		basemanaby = y.getInt("manadifference", 0);
		baseexpby = y.getInt("expdifference", 0);
		basehealthby = y.getInt("basehealthby",0);
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
	
	public int getBaseMana(){
		return BASE_MANA + basemanaby;
	}
	
	public int getBaseHealth(){
		return BASE_HEALTH + basehealthby;
	}

}
