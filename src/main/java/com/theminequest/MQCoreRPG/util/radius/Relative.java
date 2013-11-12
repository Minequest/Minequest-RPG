package com.theminequest.MQCoreRPG.util.radius;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Relative 
{
    
    private final int dx;
    private final int dz;
    
    public Relative(int dx, int dz)
    {
        this.dx = dx;
        this.dz = dz;
    }
    
    public int getDeltaX()
    {
        return this.dx;
    }
    
    public int getDeltaZ()
    {
        return this.dz;
    }
    
    public Block getRelative(Block a)
    {
        return a.getRelative(BlockFace.EAST, getDeltaX())
                .getRelative(BlockFace.SOUTH, getDeltaZ());
    }

}
