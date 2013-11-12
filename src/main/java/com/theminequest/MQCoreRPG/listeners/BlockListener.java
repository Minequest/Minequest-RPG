package com.theminequest.MQCoreRPG.listeners;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.util.EnchantmentVanillaNames;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockListener implements Listener
{
    
    private final MineQuestRPG plugin;
    
    public BlockListener(MineQuestRPG plugin)
    {
        this.plugin = plugin;
    }
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event)
    {
        Block b = event.getBlock();
        if(b.getType() == Material.FLOWER_POT)
            b = b.getRelative(BlockFace.DOWN);
        if(b.getType() == Material.GLASS)
        {
            event.setCancelled(true);
            b.getRelative(BlockFace.UP).setType(Material.AIR);
            b.setType(Material.AIR);
        }
    }
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPhysics(BlockPhysicsEvent event)
    {
        Block b = event.getBlock();
        if(b.getType() == Material.FLOWER_POT)
        {
            if(b.getRelative(BlockFace.DOWN).getType() == Material.GLASS)
                event.setCancelled(true);
        }
        else if(b.getType() == Material.GLASS)
        {
            if(b.getRelative(BlockFace.UP).getType() == Material.FLOWER_POT)
                event.setCancelled(true);
        } 
    }
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(final SignChangeEvent event)
    {
        if(!event.getLine(0).equalsIgnoreCase("[unenchant]"))
        {
            return;
        }
        Player player = event.getPlayer();
        if(!player.hasPermission("MineQuestRPG.unenchant"))
        {
            invalid(event, ChatColor.RED+"You don't have the permission to do that");
            return;
        }
        Enchantment e = EnchantmentVanillaNames.getEnchantment(event.getLine(1).toUpperCase());
        if(e == null)
        {
            invalid(event, ChatColor.RED+"Unknown enchantment");
            return;
        }
        if(event.getLine(2).matches("$[0-9]*\\.[0-9]{0,2}"))
        {
            invalid(event, ChatColor.RED+"Invalid price");
            return;
        }
        
        event.setLine(0, ChatColor.BLUE+"[Unenchant]");
        event.setLine(1, event.getLine(1).toUpperCase());
        event.setLine(2, event.getLine(2));
        event.setLine(3, "");
    }
    
    private void invalid(final SignChangeEvent event, String reason)
    {
        event.getPlayer().sendMessage(reason);
        event.setCancelled(true);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                event.getBlock().breakNaturally();
            }
        }.runTaskLater(plugin, 1L);
    }

}
