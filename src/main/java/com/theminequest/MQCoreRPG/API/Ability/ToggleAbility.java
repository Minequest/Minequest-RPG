package com.theminequest.MQCoreRPG.API.Ability;

import org.bukkit.entity.Player;

public interface ToggleAbility extends CostAbility {

	void toggleAbility(Player player);
	void detoggleAbility(Player player);
	long getDelay();
	
}
