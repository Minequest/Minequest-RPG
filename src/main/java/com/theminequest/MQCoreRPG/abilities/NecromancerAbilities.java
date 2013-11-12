package com.theminequest.MQCoreRPG.abilities;

import com.theminequest.MQCoreRPG.combat.FakeEntityDamageEvent;
import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import com.theminequest.MQCoreRPG.util.BlockUtil;
import com.theminequest.MQCoreRPG.util.radius.Relative;
import com.theminequest.MQCoreRPG.util.radius.Wave;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class NecromancerAbilities extends Abilities
{        
    private final PotionEffect curse = new PotionEffect(PotionEffectType.WITHER, 300, 0);
    private final PotionEffect shadow = new PotionEffect(PotionEffectType.WEAKNESS, 200, 1);
    private final PotionEffect fear1 = new PotionEffect(PotionEffectType.WEAKNESS, 40, 1000);
    private final PotionEffect fear2 = new PotionEffect(PotionEffectType.SLOW, 40, 1000);
    private final PotionEffect fear3 = new PotionEffect(PotionEffectType.BLINDNESS, 40, 1000);
    
    private final EnumSet<Material> corruptionExcludes = EnumSet.of(
        Material.OBSIDIAN,
        Material.WATER,
        Material.STATIONARY_WATER,
        Material.LAVA,
        Material.STATIONARY_LAVA);
    
    private final List<Vector> relParticlePos = new ArrayList<Vector>();
    
    protected NecromancerAbilities()
    {
    	ItemStack curse = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta =  curse.getItemMeta();
        meta.setDisplayName("Curse");
        meta.setLore(Arrays.asList(String.format(active, Skill.CURSE.getCooldown()),"","Curse your enemies, ", "let them wither away"));
        curse.setItemMeta(meta);
        ItemStack corrupt = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  corrupt.getItemMeta();
        meta.setDisplayName("Corruption");
        meta.setLore(Arrays.asList(String.format(active, Skill.CORRUPTION.getCooldown()), "", "Corrupt the ground ", "around you, dealing ", "AoE damage"));
        corrupt.setItemMeta(meta);
        ItemStack summon = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  summon.getItemMeta();
        meta.setDisplayName("Summon Undead");
        meta.setLore(Arrays.asList(String.format(active, Skill.SUMMON_UNDEAD.getCooldown()),"","Summons an ", "undead ally"));
        summon.setItemMeta(meta);
        ItemStack shadow = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  shadow.getItemMeta();
        meta.setDisplayName("Shadow Bolt");
        meta.setLore(Arrays.asList(String.format(active, Skill.SHADOW_BOLT.getCooldown()), "","Fires a bolt ", "of pure darkness ", "harming enemies"));
        shadow.setItemMeta(meta);
        
        this.books[0] = parseBook(curse);
        this.books[1] = parseBook(corrupt);
        this.books[2] = parseBook(shadow);
        this.books[3] = parseBook(summon);        
        double x, y, z, xz;
        
        double step = Math.PI / 10;
        double twostep = step * 2;
        
        double r = 1.5;
        
        Vector d = new Vector(0, 1, 0);
        
        for(int i = 0; i < 5; i++)
        {
            y = Math.sin(twostep*i)*r;
            xz = Math.cos(twostep*i)*r;
            for(int j = 0; j < 5*2; j++)
            {
                x = Math.cos(step*j)*xz;
                z = Math.sin(step*j)*xz;
                relParticlePos.add(new Vector(x,y,z).subtract(d));
            }
        }
        
    }
    
    public void curse(final MineQuestPlayer player)
    {
        if(!Skill.CURSE.cooldown(player))
            return;
        player.setActiveEffect(Skill.CURSE, true);
    }
    
    public void curseExecution(MineQuestPlayer player, MineQuestPlayer other)
    {
        other.setCombatTracker(player.getPlayer(), Skill.CURSE);
        other.addPotionEffect(curse);
        other.getWorld().playSound(other.getPlayer().getLocation(), Sound.BAT_DEATH, 1, 0);
        
    }
    
    public void corruption(final MineQuestPlayer player)
    {
        if(!Skill.CORRUPTION.cooldown(player))
            return;
        final Location loc = player.getPlayer().getLocation().clone();
        
        final List<Block> blocks = new ArrayList<Block>();
        Block b = player.getPlayer().getLocation().getBlock();
        for(int i = 0; i < 6; i++)
        {
            Wave r = Wave.getRadius(i);
            if(r == null)
                break;
            Relative[] rels = r.getRelatives();
            for(Relative rel : rels)
            {
                Block a = rel.getRelative(b);
                a = a.getWorld().getHighestBlockAt(a.getLocation()).getRelative(BlockFace.DOWN);
                if(!corruptionExcludes.contains(a.getType()))
                    blocks.add(a);
            }
        }
        
        List<Entity> le = player.getPlayer().getNearbyEntities(25, 25, 25);
        le.add(player.getPlayer());
        for(Entity e : le)
            if(e instanceof Player)
            {
                MineQuestPlayer msp = player.getPlugin().getPlayerManager().getPlayer((Player) e);
                if(msp != null)
                    BlockUtil.temporaryChange(msp, blocks, Material.MYCEL, (byte)0, 300);
            }
        
        new BukkitRunnable()
        {
            final FPlayer fme = FPlayers.i.get(player.getPlayer());
            int i = 0;
            
            public void run()
            {
                Bat bat = (Bat) loc.getWorld().spawnEntity(loc, EntityType.BAT);
                for(Entity e : bat.getNearbyEntities(5, 5, 5))
                    if(e instanceof LivingEntity && e != fme.getPlayer())
                    {
                        boolean dam = true;
                        if(e instanceof Player)
                            if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtLeast(Relation.ALLY))
                                dam = false;
                        if(dam)
                        {
                            if(e instanceof Player)
                            {
                                MineQuestPlayer msp = player.getPlugin().getPlayerManager().getPlayer((Player)e);
                                if(msp != null)
                                    msp.setCombatTracker(player.getPlayer(), Skill.CORRUPTION);
                            }
                            ((LivingEntity)e).damage(2);
                            Bukkit.getPluginManager().callEvent(new FakeEntityDamageEvent(e, EntityDamageEvent.DamageCause.CUSTOM, 1.0D));
                        }
                    }
                bat.remove();
                loc.getWorld().playSound(loc, Sound.SILVERFISH_WALK, 1, 0);
                if(++i >= 15)
                {
                    cancel();
                }
                
            }
        }.runTaskTimer(player.getPlugin(), 0L, 20L);
    }
    
    public void summonUndead(final MineQuestPlayer player)
    {
        if(!Skill.SUMMON_UNDEAD.cooldown(player))
            return;

        Location loc = player.getWorld().getHighestBlockAt(player.getPlayer().getTargetBlock(null, 10).getLocation()).getLocation();

        final Skeleton e = (Skeleton) player.getWorld().spawnEntity(loc, EntityType.SKELETON);
        player.getWorld().playSound(loc, Sound.SKELETON_DEATH, 1, 0);

        e.setSkeletonType(Skeleton.SkeletonType.WITHER);
        e.setMaxHealth(80);
        e.setHealth(80);
        e.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 3));
        
        e.setMetadata("owner", new FixedMetadataValue(player.getPlugin(), player.getName()));
        
        final OfflinePlayer op = Bukkit.getOfflinePlayer(player.getName());        
        new BukkitRunnable()
        {
            int i = 0;
            @Override
            public void run()
            {
                if(e.isDead())
                {
                    e.removeMetadata("owner", player.getPlugin());
                    cancel();
                    return;
                }
                
                if(!op.isOnline())
                {
                    e.removeMetadata("owner", player.getPlugin());
                    ((LivingEntity)e).damage(100);
                    cancel();
                    return;
                }
                
                if(++i >= 30)
                {
                    e.removeMetadata("owner", player.getPlugin());
                    ((LivingEntity)e).damage(100);
                    cancel();
                }
            }
        }.runTaskTimer(player.getPlugin(), 0L, 20L);
       
    }
    
    public void shadowBolt(final MineQuestPlayer player)
    {
        if(!Skill.SHADOW_BOLT.cooldown(player))
            return;
        final Location loc = player.getPlayer().getEyeLocation().clone();
        final FallingBlock fb = player.getWorld().spawnFallingBlock(loc, Material.OBSIDIAN, (byte)1);
        fb.setDropItem(false);
        final Vector speed = loc.getDirection().normalize().multiply(2);
        fb.setVelocity(speed);
        
        new BukkitRunnable()
        {
            int i = 0;
            
            FPlayer fme = FPlayers.i.get(player.getPlayer());
             
            public void run()
            {
                if(fb.isDead())
                {
                    cancel();
                    return;
                }
                fb.setVelocity(speed);
                fb.getWorld().playSound(fb.getLocation(), Sound.WITHER_SHOOT, 1, 0);
                for(Vector relpos : relParticlePos)
                    fb.getWorld().playEffect(fb.getLocation().add(relpos), Effect.SMOKE, BlockFace.SELF);
                for(Entity e : fb.getNearbyEntities(1.5, 1.5, 1.5))
                {
                    if(e instanceof LivingEntity && e != fme.getPlayer())
                    {
                        boolean dam = true;
                        if(e instanceof Player)
                            if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtLeast(Relation.ALLY))
                                dam = false;
                        if(dam)
                        {
                            if(e instanceof Player)
                            {
                                MineQuestPlayer msp = player.getPlugin().getPlayerManager().getPlayer((Player)e);
                                if(msp != null)
                                    msp.setCombatTracker(player.getPlayer(), Skill.SHADOW_BOLT);
                            }
                            ((LivingEntity)e).damage(4, player.getPlayer());
                            ((LivingEntity)e).addPotionEffect(shadow);
                            Bukkit.getPluginManager().callEvent(new FakeEntityDamageEvent(e, EntityDamageEvent.DamageCause.CUSTOM, 1.0D));
                        }
                    }
                }
                Material ahead = fb.getLocation().add(fb.getVelocity().normalize().multiply(0.1)).getBlock().getType();
                Material ahead2 = fb.getLocation().add(fb.getVelocity().normalize().multiply(1.1)).getBlock().getType();
                Material ahead3 = fb.getLocation().add(fb.getVelocity().normalize().multiply(2.1)).getBlock().getType();
                if(ahead != Material.AIR || ahead2 != Material.AIR || ahead3 != Material.AIR)
                {
                    fb.remove();
                    cancel();
                }
                else if(fb.getLocation().distanceSquared(loc) >= 850)
                {
                    fb.remove();
                    cancel();
                }
                else if(++i > 200)
                {
                    fb.remove();
                    cancel();
                }
            }
        }.runTaskTimer(player.getPlugin(), 0L, 1L);
    }
    
    public boolean block(MineQuestPlayer player, Player other, double damage)
    {
        if(!Skill.BLOCK.cooldown(player, 30000))
            return false;
        // Not 100% done
        FPlayer fme = FPlayers.i.get(player.getPlayer());
    	for(Entity e : player.getPlayer().getNearbyEntities(5, 5, 5))
            if(e instanceof Player)
            {
                if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtMost(Relation.NEUTRAL))
                {
                    MineQuestPlayer msp = player.getPlugin().getPlayerManager().getPlayer((Player)e);
                    if(msp != null)
                    {
                        msp.setCombatTracker(player.getPlayer(), Skill.BLOCK);
                        //BlockUtil.cagePlayer(msp, Material.BEDROCK, (byte)0, false, 40);
                        msp.addPotionEffect(fear1);
                        msp.addPotionEffect(fear2);
                        msp.addPotionEffect(fear3);
                    }
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
    	curse(player);
    }
    
    public void slot2(MineQuestPlayer player)
    {
    	corruption(player);
    }
    
    public void slot3(MineQuestPlayer player)
    {
        shadowBolt(player);
    }
    
    public void slot4(MineQuestPlayer player)
    {
        summonUndead(player);
    }
    
    public ItemStack[] getSkillBooks()
    {
        return this.books;
    }
    
    public List<String> getPassiveDescription()
    {
        return Arrays.asList("Undead mobs won't attack you (ex. summons)");
    }
    
    public List<String> getBlockingDescription()
    {
        return Arrays.asList(String.format("%d second(s) cooldown", 30), "", "A succesful will stun the enemy.");
    }
    
}
