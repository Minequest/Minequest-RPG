package com.theminequest.MQCoreRPG.util;

import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockUtil
{
    
    final static BlockFace[] around = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    
    public static void cagePlayer(MineQuestPlayer msp, Material mat, byte data, boolean fill, long duration)
    {
        final Player player = msp.getPlayer();
        Block b = player.getLocation().getBlock();
        final List<Block> refreshed = new ArrayList<Block>();
        for(int i = 0; i < 3; i++)
        {
            for(BlockFace face : around)
            {
                refreshed.add(b.getRelative(face));
                change(player, b.getRelative(face), mat, data);
            }
            if(fill)
            {
                refreshed.add(b);
                change(player, b, mat, data);
            }
            b = b.getRelative(BlockFace.UP);
        }
        new BukkitRunnable()
        {
             
            public void run()
            {
                refreshPlayer(player, refreshed);
            }
        }.runTaskLater(msp.getPlugin(), duration);
    }
    
    public static void temporaryChange(final MineQuestPlayer player, final List<Block> blocks, final Material mat, final byte b, long duration)
    {
        for(Block block : blocks)
        {
            player.getPlayer().sendBlockChange(block.getLocation(), mat, b);
        }
        new BukkitRunnable()
        {
             
            public void run()
            {
                for(Block block : blocks)
                {
                    player.getPlayer().sendBlockChange(block.getLocation(), block.getType(), block.getData());
                }
            }
        }.runTaskLater(player.getPlugin(), duration);
    }
    
    private static void change(Player player, Block block, Material mat, byte b)
    {
        player.sendBlockChange(block.getLocation(), mat, b);
    }
    
    private static void refreshPlayer(Player player, List<Block> blocks)
    {
        for(Block block : blocks)
        {
            refresh(player, block);
        }
    }
    
    private static void refresh(Player player, Block block)
    {
        change(player, block, block.getType(), block.getData());
    }
    
    public static Block getHighestUsableBlock(Block target, Set<Material> blacklist, boolean teleportSafe)
    {
        Block end = target;
        if(target.getType().isTransparent())
        {
            do
            {
                if(end.getY() == 0)
                {
                    end = null;
                    break;
                }
                end = end.getRelative(BlockFace.DOWN);
                if(blacklist.contains(end.getType()))
                {
                    end = null;
                    break;
                }
            }while(!end.getType().isSolid());
            if(teleportSafe && end != null)
            {
                end = safeTp(end.getRelative(BlockFace.UP), blacklist);
            }
        }
        else 
        {
            end = safeTp(target.getRelative(BlockFace.UP), blacklist);
        }
        
        if(end == null)
        {
            end = target.getWorld().getHighestBlockAt(target.getLocation());
        }
        return end.getRelative(BlockFace.UP);
    }
    
    private static Block safeTp(Block end, Set<Material> blacklist)
    {
         boolean no = false;
            if(!end.getRelative(BlockFace.DOWN).getType().isSolid())
                no = true;
            else if(end.getType().isSolid() || end.getRelative(BlockFace.UP).getType().isSolid())
                no = true;
            else if(blacklist.contains(end.getType()) || blacklist.contains(end.getRelative(BlockFace.UP).getType()))
                no = true;
            if(no)
                return null;
            return end;
    }

}
