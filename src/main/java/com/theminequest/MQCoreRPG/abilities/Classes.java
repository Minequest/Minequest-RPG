package com.theminequest.MQCoreRPG.abilities;

public enum Classes
{
    NONE(new DefaultAbilities(), -1),
    WARRIOR(new WarriorAbilities(), 0),
    MAGE(new MageAbilities(), 1),
    ASSASSIN(new AssassinAbilities(), 2),
    NECROMANCER(new NecromancerAbilities(), 3),
    SHAMAN(new ShamanAbilities(), 4),
    RANGER(new RangerAbilities(), 5),
    ;
    
    private final Abilities a;
    private final int id;
    
    Classes(Abilities a, int id)
    {
        this.a = a;
        this.id = id;
    }
    
    public Abilities getAbilities()
    {
        return this.a;
    }
    
    public Class<? extends Abilities> getAbilityClass()
    {
        return this.a.getClass();
    }
    
    public int getId()
    {
        return this.id;
    }
}
