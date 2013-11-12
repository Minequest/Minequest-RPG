package com.theminequest.MQCoreRPG.entity.navigation;

import com.theminequest.MQCoreRPG.entity.EntityPlayerNPC;

public class ControllerJump
{

    private EntityPlayerNPC a;
    private boolean b;

    public ControllerJump(EntityPlayerNPC entityinsentient)
    {
        this.a = entityinsentient;
    }

    public void a()
    {
        this.b = true;
    }

    public void b()
    {
        this.a.f(this.b);
        this.b = false;
    }
}
