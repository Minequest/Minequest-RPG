package com.theminequest.MQCoreRPG.abilities;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public enum Skill
{

    NONE(0, 0, -1),
    
    // Warrior Skill
    SHOCKWAVE(1, 15000, 1),
    REND(2, 15000, 2),
    BESERKER(3, 15000, 3),
    LAST_STAND(4, 60000, 4),
    
    // Mage Skill
    CALL_LIGHTNING(5, 5000, 1),
    ARCANE_SHIELD(6, 15000, 2),
    FROST_NOVA(7, 15000, 3),
    PYROBLAST(8, 60000, 4),
    
    // Assassin Skill
    VANISH(9, 15000, 1),
    ENVENOM(10,0, 2),
    KIDNEY_SHOT(11, 20000, 3),
    KILLING_SPREE(12, 60000, 4),
    
    // Ranger Skill
    EXPLOSIVE_GRENADE(13, 5000, 1),
    SHARP_SHOOTER(14, 0, 2),
    ICE_TRAP(15, 15000, 3),
    MASTER_OF_THE_WILD(16, 60000, 4),
    
    // Necromancer Skill
    CURSE(17, 20000, 1),
    CORRUPTION(18, 30000, 2),
    SHADOW_BOLT(19, 20000, 3),
    SUMMON_UNDEAD(20, 60000, 4),
    
    // Shaman Skill
    HEALING_STREAM_TOTEM(21, 20000, 1),
    EARTH_GRAB_TOTEM(22, 20000, 2),
    MAGMA_TOTEM(23, 15000, 3),
    RIPTIDE(24, 60000, 4),
    
    // Class dependant!
    BLOCK(30, -1, -1),
    ;
    private final long bitval;
    private final long cooldown;
    private final int slot;
    private final String name;
    
    private static final String cd = "&c&e%s&c is cooling down! You must wait &e%d second(s)&c more seconds before using it again".replace('&', ChatColor.COLOR_CHAR);
    private static final String rf = "&a&e%s&a has been refreshed!".replace('&', ChatColor.COLOR_CHAR);

    Skill(int bitval, long cooldown, int slot)
    {
        this.bitval = (long) (Math.pow(2, bitval - 1));
        this.cooldown = cooldown;
        this.slot = slot;
        String n = this.name().replace('_', ' ').toLowerCase();
        int i = 0;
        while((i = n.indexOf(" ", i)) > 0)
        {
            if(i+1 > n.length() - 1)
                break;
            n = n.substring(0, i+1) + n.substring(i+1, i+2).toUpperCase()+n.substring(i+2);
            i++;
        }
        this.name = n.substring(0,1).toUpperCase()+n.substring(1);
    }

    public long getBitVal()
    {
        return this.bitval;
    }
    
    public int getSlot()
    {
        return this.slot;
    }
    
    public boolean cooldown(final MineQuestPlayer player, long cooldown)
    {
        if(player.isCooling(this)) 
        {
            if(this != BLOCK)
                player.getPlayer().sendMessage(String.format(Skill.cd, this.name, player.getCooling(this)));
            return false;
        }
        if(cooldown <= 0 || MineQuestRPG.isDev(player.getName())) return true;
        
        player.setCoolEffect(this, true);
        new BukkitRunnable()
        {  
            public void run()
            {
                player.getPlayer().sendMessage(String.format(Skill.rf, Skill.this.name));
                player.setCoolEffect(Skill.this, false);
            }
        }.runTaskLater(player.getPlugin(), cooldown/50L);
        return true;
    }
    
    public boolean cooldown(final MineQuestPlayer player)
    {
        return this.cooldown(player, this.cooldown);
    }
    
    public int getCooldown()
    {
        return (int) (this.cooldown/1000);
    }
    
    public static boolean isSkillBook(ItemStack i)
    {
        if(i != null && i.getType() == Material.ENCHANTED_BOOK)
        {
            if(i.getItemMeta() == null)
                return false;
            String title = i.getItemMeta().getDisplayName();
            if(title == null)
                return false;
            title = ChatColor.stripColor(title).replace(' ', '_').toUpperCase();
            try
            {
                Skill.valueOf(title);
                return true;
            }
            catch(IllegalArgumentException ex)
            {
                // Swallow it
            }
        }
        // Health kits bish :3. Should block dumping them.
        else if(i != null && (i.getType() == Material.APPLE || i.getType() == Material.GOLDEN_APPLE))
        {
            if(i.getDurability() == 2)
            {
                return true;
            }
        }
        return false;
    }
    
    public static Skill[] getClassSkills(Classes clazz)
    {
        int min = (int) Math.pow(2, clazz.getId()*4);
        int max = min << 3;
        Skill[] skills = new Skill[4];
        int i = 0;
        for(Skill s : values())
        {
            if(s.bitval >= min && s.bitval <= max)
                skills[i++] = s;
            if(i >= 4)
                break;
        }
        return skills;
    }
}