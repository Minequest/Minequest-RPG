package com.theminequest.MQCoreRPG.API.Ability;

import org.bukkit.entity.Player;

public interface ActiveAbility extends CostAbility {

	void activateAbility(Player player);
	
}
