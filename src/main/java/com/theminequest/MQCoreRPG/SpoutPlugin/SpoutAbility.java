package com.theminequest.MQCoreRPG.SpoutPlugin;

import com.theminequest.MQCoreRPG.API.Abilities.Ability;

public abstract class SpoutAbility extends Ability {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3656060979552594479L;

	/**
	 * With SpoutAbility, you can now give this ability an image in the HUD.
	 * 20x20 max.
	 * 
	 * @return publically accessible URL
	 */
	public abstract String getImage();

}
