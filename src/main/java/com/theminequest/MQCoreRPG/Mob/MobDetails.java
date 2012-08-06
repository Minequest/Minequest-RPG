package com.theminequest.MQCoreRPG.Mob;

import java.util.Random;

import org.bukkit.entity.LivingEntity;

public class MobDetails {
	
	private int levelvariation;
	private final LivingEntity entity;
	
	public MobDetails(LivingEntity entity) {
		this(entity,MobManager.VARY_LOW,MobManager.VARY_HIGH);
	}
	
	public MobDetails(LivingEntity entity, int lowvar, int highvar){
		this.entity = entity;
		toggleRecalculation(lowvar,highvar);
	}
	
	public LivingEntity getLivingEntity(){
		return entity;
	}
	
	public int getLevelVariation(){
		return levelvariation;
	}
	
	public void toggleRecalculation(int lowvar, int highvar){
		levelvariation = new Random().nextInt(highvar+1+Math.abs(lowvar))-lowvar;
	}
}
