package com.theminequest.MQCoreRPG.managers;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.Classes;
import com.theminequest.MQCoreRPG.abilities.Skill;
import com.theminequest.MQCoreRPG.combat.CombatEntry;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public class MineQuestPlayer 
{   
    
    private final Player player;
    
    private long activeEffects = 0;
    
    private long cooldown = 0;
    
    private Classes clazz = Classes.NONE;
    
    private final MineQuestRPG plugin;
    
    private final Map<Skill, Long> lastUsed = new HashMap<Skill, Long>();
    
    protected MineQuestPlayer(Player player, MineQuestRPG plugin)
    {
        this.player = player;
        this.plugin = plugin;
    }
    
    public MineQuestRPG getPlugin()
    {
        return this.plugin;
    }
    
    public Player getPlayer()
    {
        return this.player;
    }
    
    public String getName()
    {
        return this.player.getName();
    }
    
    public void setVelocity(Vector speed)
    {
        this.player.setVelocity(speed);
    }
    
    public Vector getDirection()
    {
        return this.player.getEyeLocation().getDirection();
    }
    
    public void setActiveEffect(Skill effect, boolean flag)
    {
        if(flag)
            this.activeEffects |= effect.getBitVal();
        else
            this.activeEffects &= ~effect.getBitVal();
    }
    
    public void setCoolEffect(Skill effect, boolean flag)
    {
        if(flag)
        {
            this.lastUsed.put(effect, System.currentTimeMillis());
            this.cooldown |= effect.getBitVal();
        }
        else
        {
            this.lastUsed.remove(effect);
            this.cooldown &= ~effect.getBitVal();
        }
    }
    
    public boolean isEffectActive(Skill effect)
    {
        return (this.activeEffects & effect.getBitVal()) != 0;
    }
    
    public boolean isCooling(Skill effect)
    {
        return (this.cooldown & effect.getBitVal()) != 0;
    }
    
    public int getCooling(Skill skill)
    {
        Long l = this.lastUsed.get(skill);
        if(l != null)
        {
            return Math.max(skill.getCooldown() - (int)((System.currentTimeMillis() - l.longValue())/1000), 1);
        }
        return -1;
    }
    
    public World getWorld()
    {
        return this.player.getWorld();
    }
    
    public double getDistanceSquaredTo(Player other)
    {
        if(other.getWorld() != getWorld()) return Double.POSITIVE_INFINITY;
        return this.player.getLocation().distanceSquared(other.getLocation());
    }
    
    public void setPlayerClass(Classes clazz)
    {
        this.clazz = clazz;
    }
    
    public Classes getPlayerClass()
    {
        return this.clazz;
    }
    
    public boolean isWarrior()
    {
        return this.clazz == Classes.WARRIOR;
    }
    
    public boolean isMage()
    {
        return this.clazz == Classes.MAGE;
    }
    
    public boolean isAssassin()
    {
        return this.clazz == Classes.ASSASSIN;
    }
    
    public boolean isRanger()
    {
        return this.clazz == Classes.RANGER;
    }
    
    public boolean isNecromancer()
    {
        return this.clazz == Classes.NECROMANCER;
    }
    
    public boolean isShaman()
    {
        return this.clazz == Classes.SHAMAN;
    }
    
    public void setHealth(int i)
    {
        this.player.setHealth(i <= this.player.getMaxHealth() ? i : this.player.getMaxHealth());
    }
    
    public void addPotionEffect(PotionEffect pe)
    {
        this.player.addPotionEffect(pe, true);
    }

    public boolean isBlocking()
    {
        return this.player.isBlocking();
    }
    
    public boolean isSneaking()
    {
        return this.player.isSneaking();
    }
    
    public void setCombatTracker(Player killer, Skill skill)
    {
        this.plugin.getCombatTracker().set(player, killer, skill);
    }
    
    public boolean hasCombatTracked()
    {
        return this.plugin.getCombatTracker().has(player);
    }
    
    public CombatEntry getCombatTacker()
    {
        return this.plugin.getCombatTracker().retrieve(player);
    }
    
    public void save(ConfigurationSection player)
    {
        player.set("active", this.activeEffects);
        player.set("cooldown", this.cooldown);
        player.set("class", this.clazz.name().toLowerCase());
        
        ConfigurationSection last = player.getConfigurationSection("last-used");
        if(last == null)
            last = player.createSection("last-used");
        
        if(this.clazz != Classes.NONE)
            for(Skill s : Skill.getClassSkills(clazz))
            {
                System.out.println(s);
                last.set(s.name().toLowerCase(), this.lastUsed.get(s));
            }
    }
    
    public void load(ConfigurationSection player)
    {
        this.activeEffects = player.getLong("active", 0);
        this.activeEffects = player.getLong("cooldown", 0);
        try
        {
            this.clazz = Classes.valueOf(player.getString("class", "").toUpperCase());
        }
        catch(IllegalArgumentException ex)
        {
            this.clazz = Classes.NONE;
        }
                
        ConfigurationSection last = player.getConfigurationSection("last-used");
        if(last == null)
            last = player.createSection("last-used");
        
        Skill s;
        
        for(String skillName : last.getKeys(false))
        {
            try
            {
                s = Skill.valueOf(skillName);
            }
            catch(IllegalArgumentException ex)
            {
                continue;
            }
            this.lastUsed.put(s, last.getLong(skillName, 0));
        }
    }
    
}
