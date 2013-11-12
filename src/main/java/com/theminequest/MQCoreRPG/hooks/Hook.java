package com.theminequest.MQCoreRPG.hooks;

import org.bukkit.entity.Player;

public interface Hook
{
    public boolean isPermitted(Player player, String check);
}
