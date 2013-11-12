package com.theminequest.MQCoreRPG.entity;

import com.theminequest.MQCoreRPG.entity.navigation.ControllerJump;
import com.theminequest.MQCoreRPG.entity.navigation.ControllerLook;
import com.theminequest.MQCoreRPG.entity.navigation.ControllerMove;
import com.theminequest.MQCoreRPG.entity.navigation.Navigation;
import java.util.Random;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import net.minecraft.server.v1_6_R2.MinecraftServer;
import net.minecraft.server.v1_6_R2.PlayerInteractManager;
import net.minecraft.server.v1_6_R2.World;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class EntityPlayerNPC extends EntityPlayer
{

    private static final double EPSILON = 0.005;
    
    public final Navigation nav;
    
    public final ControllerMove move;
    
    public final ControllerJump jump;
    
    public final ControllerLook look;
    
    private double speed = 1.0D;
    
    private int jumpTicks = 0;
    
    public boolean isNavigating;
    
    private long lastNavigated;
    
    private Vector min;
    
    private Vector max;
    
    protected double sx, sy, sz;
    
    private Random rand = new Random();
    
    EntityPlayerNPC(MinecraftServer server, World world, String name, PlayerInteractManager manager, Location at)
    {
        super(server, world, name, manager);
        this.aW().b(GenericAttributes.b).setValue(32);
        this.nav = new Navigation(this, this.world);
        this.move = new ControllerMove(this);
        this.look = new ControllerLook(this);
        this.jump = new ControllerJump(this);
        this.onGround = true;
        this.Y = 1F;
        this.isNavigating = false;
        this.lastNavigated = Integer.MAX_VALUE;
        sx = at.getX();
        sy = at.getY();
        sz = at.getZ();
        
        this.min = new Vector(sx - 2, 0, sz - 2);
        this.max = new Vector(sx + 2, world.getHeight(), sz + 2);
    }
    
    // update()
    @Override
    public void l_()
    {
        super.l_();
        this.move(0, -0.2D, 0);
        if(this.motX < EPSILON && this.motY < EPSILON && this.motZ < EPSILON)
        {
            motX = motY = motZ = 0;
        }
        if(isNavigating && this.lastNavigated == 0)
        {
            this.nav.f();
            this.nav.a(speed);
            this.move.c();
            this.look.a();
            this.jump.b();
            if(this.nav.g())
            {
                this.isNavigating = false;
            }
        }
        
        if(!this.isNavigating)
        {
            if(this.lastNavigated++ >= 60)
            {
                double dx = this.rand.nextDouble()*(max.getX() - min.getX());
                double dz = this.rand.nextDouble()*(max.getZ() - min.getZ());
                Vector pos = new Vector(dx, this.locY, dz).add(this.min);
                Location loc = pos.toLocation(this.world.getWorld());
                this.getBukkitEntity().moveTo(loc, speed);
                this.lastNavigated = 0;
                this.isNavigating = true;
            }
                
        }
        
        if (bd) {
            /* boolean inLiquid = G() || I();
             if (inLiquid) {
                 motY += 0.04;
             } else //(handled elsewhere)*/
            if (onGround && jumpTicks == 0) {
                bd();
                jumpTicks = 10;
            }
        } else {
            jumpTicks = 0;
        }
        be *= 0.98F;
        bf *= 0.98F;
        bg *= 0.9F;

        e(be, bf); // movement method
        float yaw = this.yaw;
        while (yaw < -180.0F) {
            yaw += 360.0F;
        }

        while (yaw >= 180.0F) {
            yaw -= 360.0F;
        }
        this.aP = yaw;
        this.aQ = yaw;
        if (jumpTicks > 0) {
            jumpTicks--;
        }
        
    }
    
    @Override
    public void i(float f)
    {
        super.i(f);
        setbf(f);
    }
    
    public void setbf(float f)
    {
        this.bf = f;
    }
    
    public void setSpeed(double speed)
    {
        this.speed = speed;
    }
    
    public double getSpeed()
    {
        return this.speed;
    }
    
    @Override
    public NPC getBukkitEntity()
    {
        return (NPC)super.getBukkitEntity();
    }
    
    public Navigation getNavigation()
    {
        return this.nav;
    }
    
    public ControllerMove getControllerMove()
    {
        return this.move;
    }
    
    public ControllerJump getControllerJump()
    {
        return this.jump;
    }
    
    @Override
    public void collide(Entity entity) 
    {
        // Do nothing :D
    }

}