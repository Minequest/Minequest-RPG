package com.theminequest.MQCoreRPG.combat;

import com.theminequest.MQCoreRPG.abilities.Skill;
import org.bukkit.entity.Player;

public class CombatEntry 
{
    
    public final String name;
    
    public final Skill skill;
    
    protected CombatEntry(Player player, Skill skill)
    {
        this.name = player.getName();
        this.skill = skill;
    }
    
    public String getAttacker()
    {
        return this.name;
    }

    public Skill getSkill()
    {
        return this.skill;
    }
    
}
