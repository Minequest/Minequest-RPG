package com.theminequest.MQCoreRPG.abilities;

import org.bukkit.ChatColor;

public enum ClassTag
{
    _NONE(""),
    _WARRIOR("&r&7[&4W&7]"),
    _MAGE("&r&7[&3M&7]"),
    _ASSASSIN("&r&7[&4A&7]"),
    _RANGER("&r&7[&9R&7]"),
    _NECROMANCER("&r&7[&5N&7]"),
    _SHAMAN("&r&7[&2S&7]");

    private final String tag;

    ClassTag(String tag)
    {
        this.tag = tag.replace('&', ChatColor.COLOR_CHAR) + ChatColor.RESET;
    }

    public String getTag()
    {
        return this.tag;
    }
    
    public static ClassTag getClassTag(Classes clazz)
    {
        switch(clazz)
        {
            case WARRIOR:
                return _WARRIOR;
            case MAGE:
                return _MAGE;
            case ASSASSIN:
                return _ASSASSIN;
            case RANGER:
                return _RANGER;
            case NECROMANCER:
                return _NECROMANCER;
            case SHAMAN:
                return _SHAMAN;
            default:
                return _NONE;
        }
    }
}
