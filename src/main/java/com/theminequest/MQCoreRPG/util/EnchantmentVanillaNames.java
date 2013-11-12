package com.theminequest.MQCoreRPG.util;

import org.bukkit.enchantments.Enchantment;

public enum EnchantmentVanillaNames
{
    SHARPNESS(Enchantment.DAMAGE_ALL, "SHARPNESS"),
    SMITE(Enchantment.DAMAGE_UNDEAD, "SMITE"),
    EFFICIENCY(Enchantment.DURABILITY, "EFFICIENCY"),
    PROJ_PROT(Enchantment.PROTECTION_PROJECTILE, "PROJECTILE_PROT"),
    BLAST_PROT(Enchantment.PROTECTION_EXPLOSIONS, "BLAST_PROT"),
    FIRE_PROT(Enchantment.PROTECTION_FIRE, "FIRE_PROT"),
    BANE(Enchantment.DAMAGE_ARTHROPODS, "BANE"),
    PUNCH(Enchantment.ARROW_KNOCKBACK, "PUNCH")
    ;
    
    private final Enchantment original;
    private final String name;

    EnchantmentVanillaNames(Enchantment original, String name)
    {
        this.original = original;
        this.name = name;
    }
    
    public static Enchantment getEnchantment(String enchant)
    {
        enchant = enchant.toUpperCase();
        for(EnchantmentVanillaNames e : values())
            if(e.name.equalsIgnoreCase(enchant))
                return e.original;
        return Enchantment.getByName(enchant);
    }
}
