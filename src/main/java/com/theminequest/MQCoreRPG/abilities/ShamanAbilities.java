package com.theminequest.MQCoreRPG.abilities;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.FlowerPot;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ShamanAbilities extends Abilities
{
    
    private final PotionEffect healing = new PotionEffect(PotionEffectType.REGENERATION, 20, 0);
    private final PotionEffect earth = new PotionEffect(PotionEffectType.SLOW, 80, 2);
    private final PotionEffect riptide = new PotionEffect(PotionEffectType.ABSORPTION, 1200, 1);
    
    protected ShamanAbilities()
    {
        ItemStack health = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta =  health.getItemMeta();
        meta.setDisplayName("Healing Stream Totem");
        meta.setLore(Arrays.asList(String.format(active, Skill.HEALING_STREAM_TOTEM.getCooldown()), "", "Places a totem ", "where you aim ", "that regenerates", " the player with", " the lowest health"));
        health.setItemMeta(meta);
        ItemStack earth = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  earth.getItemMeta();
        meta.setDisplayName("Earth Grab Totem");
        meta.setLore(Arrays.asList(String.format(active, Skill.EARTH_GRAB_TOTEM.getCooldown()), "", "Places a totem ", "where you aim ", "that slows ", "enemies nearby"));
        earth.setItemMeta(meta);
        ItemStack magma = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  magma.getItemMeta();
        meta.setDisplayName("Magma Totem");
        meta.setLore(Arrays.asList(String.format(active, Skill.MAGMA_TOTEM.getCooldown()), "", "Places a totem ", "where you aim ", "that sets ", "nearby enemies ", "on fire"));
        magma.setItemMeta(meta);
        ItemStack riptide = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  riptide.getItemMeta();
        meta.setDisplayName("Riptide");
        meta.setLore(Arrays.asList(String.format(active, Skill.RIPTIDE.getCooldown()), "", "Heals all ", "allies in ", "20 blocks radius ", "and applies ", "60 seconds of ", "absorbsion"));
        riptide.setItemMeta(meta);
        
        this.books[0] = parseBook(health);
        this.books[1] = parseBook(earth);
        this.books[2] = parseBook(magma);
        this.books[3] = parseBook(riptide);
    }
    
    public void healingStream(final MineQuestPlayer player)
    {
        final Block b = player.getPlayer().getTargetBlock(null, 10).getRelative(BlockFace.UP);
        if(!canPlaceTotem(player, b))
            return;
        if(!Skill.HEALING_STREAM_TOTEM.cooldown(player))
            return;
        placeTotem(player, b, TotemType.HEALING);
        
        new BukkitRunnable()
        {
            FPlayer fp;
            FPlayer fme = FPlayers.i.get(player.getPlayer());
            boolean nofac = fme.getFaction().isNone();
            
            int run = 0;
                                     
            public void run()
            {
                if(b.getType() != Material.GLASS || b.getRelative(BlockFace.UP).getType() != Material.FLOWER_POT)
                {
                    cancel();
                    return;
                }
                
                List<LivingEntity> heals = new ArrayList<LivingEntity>(); 
                
                if(fme.getFaction().isNone())
                {
                    heals.add(fme.getPlayer());
                }
                ExperienceOrb orb = (ExperienceOrb) b.getWorld().spawnEntity(b.getLocation().add(new Vector(0.5, 0.5, 0.5)), EntityType.EXPERIENCE_ORB);
                List<Entity> entities = orb.getNearbyEntities(10, 10, 10);
                orb.remove();
                for(Entity e : entities)
                {
                    if(e instanceof LivingEntity)
                    {
                        if(e instanceof Player)
                            if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtLeast(Relation.ALLY))
                            {
                                heals.add((LivingEntity)e);
                            }
                        if(e instanceof Wolf)
                            if(((Wolf)e).getOwner() == fme.getPlayer())
                                heals.add((LivingEntity)e);
                    }
                }
                if(!heals.isEmpty())
                {
                    for(LivingEntity le : heals)
                    {
                        le.addPotionEffect(healing);
                        double health = le.getHealth();
                        health += 2;
                        le.setHealth(Math.min(health, le.getMaxHealth()));
                    }
                    heals.clear();
                }
                if(++run >= 10)
                {
                    b.getRelative(BlockFace.UP).setType(Material.AIR);
                    b.setType(Material.AIR);
                    cancel();
                }
            }
        }.runTaskTimer(player.getPlugin(), 0L, 20L);
    }
    
    public void earthGrab(final MineQuestPlayer player)
    {
        final Block b = player.getPlayer().getTargetBlock(null, 10).getRelative(BlockFace.UP);
        if(!canPlaceTotem(player, b))
            return;
        if(!Skill.EARTH_GRAB_TOTEM.cooldown(player))
            return;
        placeTotem(player, b, TotemType.EARTH);
        new BukkitRunnable()
        {
            FPlayer fp;
            FPlayer fme = FPlayers.i.get(player.getPlayer());
            
            int run = 0;
            
            public void run()
            {
                if(b.getType() != Material.GLASS || b.getRelative(BlockFace.UP).getType() != Material.FLOWER_POT)
                {
                    cancel();
                    return;
                }
                ExperienceOrb orb = (ExperienceOrb) b.getWorld().spawnEntity(b.getLocation().add(new Vector(0.5, 0.5, 0.5)), EntityType.EXPERIENCE_ORB);
                List<Entity> entities = orb.getNearbyEntities(10, 10, 10);
                orb.remove();
                for(Entity e : entities)
                    if(e instanceof Player && e != fme.getPlayer())
                    {
                        if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtMost(Relation.NEUTRAL))
                        {
                            ((Player) e).addPotionEffect(earth);
                        }
                    }
                if(++run >= 40)
                {
                    b.getRelative(BlockFace.UP).setType(Material.AIR);
                    b.setType(Material.AIR);
                    cancel();
                }
            }
        }.runTaskTimer(player.getPlugin(), 0L, 5L);
    }
    
    public void magmaTotem(final MineQuestPlayer player)
    {
        final Block b = player.getPlayer().getTargetBlock(null, 10).getRelative(BlockFace.UP);
        if(!canPlaceTotem(player, b))
            return;
        if(!Skill.MAGMA_TOTEM.cooldown(player))
            return;
        placeTotem(player, b, TotemType.MAGMA);
        
        new BukkitRunnable()
        {
            FPlayer fp;
            FPlayer fme = FPlayers.i.get(player.getPlayer());
                       
            int run = 0;
            
             
            public void run()
            {
                if(b.getType() != Material.GLASS || b.getRelative(BlockFace.UP).getType() != Material.FLOWER_POT)
                {
                    cancel();
                    return;
                }
                ExperienceOrb orb = (ExperienceOrb) b.getWorld().spawnEntity(b.getLocation().add(new Vector(0.5, 0.5, 0.5)), EntityType.EXPERIENCE_ORB);
                orb.getWorld().playSound(orb.getLocation(), Sound.LAVA, 1, 1);
                List<Entity> entities = orb.getNearbyEntities(10, 10, 10);
                orb.remove();
                for(Entity e : entities)
                {
                    boolean dam = true;
                    if(e instanceof Player)
                        if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtLeast(Relation.ALLY) || e == fme.getPlayer())
                            dam = false;
                    if(dam)
                    {
                        if(e instanceof Player)
                        {
                            MineQuestPlayer msp = player.getPlugin().getPlayerManager().getPlayer((Player)e);
                            if(msp != null)
                                msp.setCombatTracker(player.getPlayer(), Skill.MAGMA_TOTEM);
                        }
                        e.setFireTicks(20);
                    }
                }
                if(++run >= 10)
                {
                    b.getRelative(BlockFace.UP).setType(Material.AIR);
                    b.setType(Material.AIR);
                    cancel();
                }
            }
        }.runTaskTimer(player.getPlugin(), 0L, 20L);
    }
    
    public void riptide(final MineQuestPlayer player)
    {
        //Block b = player.getPlayer().getTargetBlock(null, 10);
        //if(!canPlaceTotem(player, b))
        //    return;
        if(!Skill.RIPTIDE.cooldown(player))
            return;
        //placeTotem(b, TotemType.RIPTIDE);
        FPlayer fme = FPlayers.i.get(player.getPlayer());
        
        player.setHealth(Integer.MAX_VALUE);
        player.addPotionEffect(riptide);
        if(!fme.getFaction().isNone())
            for(Entity e : player.getPlayer().getNearbyEntities(10, 10, 10))
                if(e instanceof Player)
                {
                    if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtLeast(Relation.ALLY))
                    {
                        ((Player)e).setHealth(((Player)e).getMaxHealth());
                        ((Player)e).addPotionEffect(riptide);
                    }
                }
    }
    
    public boolean block(MineQuestPlayer player, Player other, double damage)
    {
        if(!Skill.BLOCK.cooldown(player, 15000))
            return false;
        FPlayer fme = FPlayers.i.get(player.getPlayer());
        Vector vme = player.getPlayer().getLocation().toVector();
        Vector v;
    	for(Entity e : player.getPlayer().getNearbyEntities(5, 5, 5))
            if(e instanceof Player)
            {
                if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtMost(Relation.NEUTRAL))
                {
                    v = e.getLocation().toVector().subtract(vme).normalize();
                    e.setVelocity(v);
                }
            }
        return true;     
    }
    
    public void loadPassiveAbilities(MineQuestPlayer player)
    {
        PlayerInventory pi = player.getPlayer().getInventory();
        for(int i = 1; i < 5; i++)
        {
            pi.setItem(i, this.books[i-1]);
        }
    }
    
    public void unloadPassiveAbilities(MineQuestPlayer player)
    {
    }
    
    public void slot1(MineQuestPlayer player)
    {
    	healingStream(player);
    }
    
    public void slot2(MineQuestPlayer player)
    {
    	earthGrab(player);
    }
    
    public void slot3(MineQuestPlayer player)
    {
    	magmaTotem(player);
    }
    
    public void slot4(MineQuestPlayer player)
    {
        riptide(player);
    }
    
    private boolean canPlaceTotem(MineQuestPlayer player, Block b)
    {
        if(b.getType() != Material.AIR || b.getRelative(BlockFace.DOWN).getType() == Material.AIR)
        {
            player.getPlayer().sendMessage(ChatColor.RED+"Invalid target!");
            return false;
        }
        if(b.getRelative(BlockFace.UP).getType() != Material.AIR)
        {
            player.getPlayer().sendMessage(ChatColor.RED+"Not enough space for the totem!");
            return false;
        }
        return true;
    }
    
    private void placeTotem(MineQuestPlayer player, final Block b, final TotemType type)
    {
        b.setType(Material.GLASS);
        final Block a = b.getRelative(BlockFace.UP);
        a.setType(Material.FLOWER_POT);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                BlockState bs = a.getState();
                FlowerPot fp = new FlowerPot(Material.FLOWER_POT);
                fp.setContents(type.data);
                bs.setData(fp);
                bs.update(true, false);
            }
        }.runTaskLater(player.getPlugin(), 1L);
    }
    
    public ItemStack[] getSkillBooks()
    {
        return this.books;
    }
    
    public List<String> getPassiveDescription()
    {
        return Arrays.asList("Shamans recieve a slight regeneration over time.");
    }
    
    public List<String> getBlockingDescription()
    {
        return Arrays.asList(String.format("%d second(s) cooldown", 30), "", "A succesful block will knockback surrounding enemies.");
    }
    
    private enum TotemType
    {
        HEALING(Material.RED_ROSE),
        EARTH(Material.BROWN_MUSHROOM),
        MAGMA(Material.YELLOW_FLOWER),
        //RIPTIDE(Material.RED_ROSE);
        ;
        private final MaterialData data;
        
        TotemType(Material mat)
        {
            data = new MaterialData(mat);
        }
    }
    
}
