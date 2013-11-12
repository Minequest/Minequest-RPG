package com.theminequest.MQCoreRPG.abilities;

import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DefaultAbilities extends Abilities
{

    protected DefaultAbilities()
    {
    }

    public void loadPassiveAbilities(MineQuestPlayer player)
    {
    }
    
    public void unloadPassiveAbilities(MineQuestPlayer player)
    {
    }
    
    public boolean block(MineQuestPlayer player, Player other, double damage)
    {
        return false;
    }
    
    public void slot1(MineQuestPlayer player){}
    public void slot2(MineQuestPlayer player){}
    public void slot3(MineQuestPlayer player){}
    public void slot4(MineQuestPlayer player){}
    
    public ItemStack[] getSkillBooks()
    {
        return new ItemStack[]{};
    }
    
    public List<String> getPassiveDescription()
    {
        return Arrays.asList();
    }
    
    public List<String> getBlockingDescription()
    {
        return Arrays.asList();
    }
    
}
