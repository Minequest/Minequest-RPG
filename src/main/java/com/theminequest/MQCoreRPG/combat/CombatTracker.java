package com.theminequest.MQCoreRPG.combat;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.Skill;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class CombatTracker 
{
        
    Map<String,CombatEntry> track = new HashMap<String, CombatEntry>();
    
    public CombatTracker()
    {
    }
    
    public void set(Player attacked, Player attacker, Skill skill)
    {
        this.track.put(attacked.getName(), new CombatEntry(attacker, skill));
    }
    
    public boolean has(Player died)
    {
        return this.track.containsKey(died.getName());
    }
    
    public CombatEntry retrieve(Player died)
    {
        return this.track.remove(died.getName());
    }

}
