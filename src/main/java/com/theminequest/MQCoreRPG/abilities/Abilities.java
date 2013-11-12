package com.theminequest.MQCoreRPG.abilities;

import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Abilities
{
    protected final static String active = "&bCooldown: &c%ds".replace('&', ChatColor.COLOR_CHAR);
    protected final static String passive = "Passive ability";
    
    
    protected ItemStack[] books = new ItemStack[]{
        new ItemStack(Material.AIR),
        new ItemStack(Material.AIR),
        new ItemStack(Material.AIR),
        new ItemStack(Material.AIR),
        new ItemStack(Material.AIR)};
            
    public abstract void loadPassiveAbilities(MineQuestPlayer player);
    public abstract void unloadPassiveAbilities(MineQuestPlayer player);
    
    public ItemStack[] getSkillBooks()
    {
        return this.books;
    }
    
    /**
     * 
     * @param player the player blocking
     * @param other the player attacking
     * @param damage the damage dealt by the blow
     * @return if the block was successful
     */
    public abstract boolean block(MineQuestPlayer player, Player other, double damage);
    
    public abstract void slot1(MineQuestPlayer player);
    public abstract void slot2(MineQuestPlayer player);
    public abstract void slot3(MineQuestPlayer player);
    public abstract void slot4(MineQuestPlayer player);
    
    public abstract List<String> getPassiveDescription();
    public abstract List<String> getBlockingDescription();
    
    protected ItemStack parseBook(ItemStack book)
    {
        if(book == null || book.getItemMeta() == null)
            return book;
        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName(ChatColor.RESET+""+ChatColor.YELLOW+meta.getDisplayName());
        List<String> lore = meta.getLore();
        if(lore != null)
            for(int i = 0; i < lore.size(); i++)
            {
                lore.set(i, ChatColor.RESET+""+ChatColor.BLUE+lore.get(i));
            }
        book.setItemMeta(meta);
        return book;
    }
    
}
