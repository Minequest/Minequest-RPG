package com.theminequest.MQCoreRPG.hooks;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardHook implements Hook
{
    private final MineQuestRPG plugin;
    private final WorldGuardPlugin wg;
        
    private String noSkillMsg;
    
    public WorldGuardHook(MineQuestRPG plugin, Plugin wg)
    {
        this.plugin = plugin;
        if(wg == null || wg instanceof WorldGuardPlugin == false)
        {
            throw new IllegalStateException("WorldGuard plugin not found");
        }
        this.wg = (WorldGuardPlugin) wg;
        if(!this.wg.isEnabled())
        {
            throw new IllegalStateException("WorldGuard is not enabled");
        }
        this.noSkillMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("worldguard.no-skill.message", "&cYou cannot use abilities in this region."));
    }
    
    public boolean isPermitted(Player player, String sub)
    {
        ApplicableRegionSet regions = this.wg.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());
        switch(Sub.getSub(sub))
        {
            case NO_SKILL:
                if(regions.allows(DefaultFlag.INVINCIBILITY) || !regions.allows(DefaultFlag.PVP))
                {
                    player.sendMessage(this.noSkillMsg);
                    return false;
                }
                return true;
            default:
                return true;
        }
    }
    
    private enum Sub
    {
        NONE,
        NO_SKILL;
        
        private static Sub getSub(String sub)
        {
            sub = sub.toUpperCase().replace(' ', '_');
            try
            {
                return valueOf(sub);
            }
            catch(IllegalArgumentException ex)
            {
                return NONE;
            }
        }
    }
    
}
