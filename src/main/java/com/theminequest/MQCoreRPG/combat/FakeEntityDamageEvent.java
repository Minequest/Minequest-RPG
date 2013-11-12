package com.theminequest.MQCoreRPG.combat;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class FakeEntityDamageEvent extends EntityDamageEvent
{
    public FakeEntityDamageEvent(Entity e, DamageCause dc, int d)
    {
        super(e, dc, d);
    }
    
    public FakeEntityDamageEvent(Entity e, DamageCause dc, double d)
    {
        super(e, dc, d);
    }
}
