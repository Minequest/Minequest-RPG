package com.theminequest.MQCoreRPG.hooks;

import org.bukkit.entity.Player;

public class NullHook implements Hook
{
    public boolean isPermitted(Player player, String sub)
    {
        return true;
    }
}
