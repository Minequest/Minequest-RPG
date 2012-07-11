package com.theminequest.MQCoreRPG.API.Ability;

import org.bukkit.entity.Player;

public interface PassiveAbility extends GenericAbility {
	
	void activateAbility(Player player);
	long getDelay();
	
}
