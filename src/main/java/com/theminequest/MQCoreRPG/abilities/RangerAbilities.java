package com.theminequest.MQCoreRPG.abilities;

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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

public class RangerAbilities extends Abilities
{
    
    private final PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0);
    private final PotionEffect ice = new PotionEffect(PotionEffectType.SLOW, 100, 3);
    
    private final EnumSet<Material> icetrapExcludes = EnumSet.of(
        Material.OBSIDIAN,
        Material.WATER,
        Material.STATIONARY_WATER,
        Material.LAVA,
        Material.STATIONARY_LAVA);

    protected RangerAbilities()
    {
        ItemStack grenade = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta =  grenade.getItemMeta();
        meta.setDisplayName("Explosive Grenade");
        meta.setLore(Arrays.asList(String.format(active, Skill.EXPLOSIVE_GRENADE.getCooldown()), "", "Throws a grenade", " that deals 2.5 ", "hearts of AoE", "damage"));
        grenade.setItemMeta(meta);
        ItemStack sharp = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  sharp.getItemMeta();
        meta.setDisplayName("Sharp Shooter");
        meta.setLore(Arrays.asList(String.format(passive), "", "Rangers deal ", "more damage over ", " a longer range ", "with bows"));
        sharp.setItemMeta(meta);
        ItemStack ice = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  ice.getItemMeta();
        meta.setDisplayName("Ice Trap");
        meta.setLore(Arrays.asList(String.format(active, Skill.ICE_TRAP.getCooldown()), "", "A trap that ", "slows enemies"));
        ice.setItemMeta(meta);
        ItemStack motw = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  motw.getItemMeta();
        meta.setDisplayName("Master of the Wild");
        meta.setLore(Arrays.asList(String.format(active, Skill.MASTER_OF_THE_WILD.getCooldown()), "", "Summon 10 wolves ", "for you to command ", "for 30 seconds"));
        motw.setItemMeta(meta);
        
        this.books[0] = parseBook(grenade);
        this.books[1] = parseBook(sharp);
        this.books[2] = parseBook(ice);
        this.books[3] = parseBook(motw);
    }
    
    public void loadPassiveAbilities(MineQuestPlayer player)
    {
        constantSpeed(player);
        PlayerInventory pi = player.getPlayer().getInventory();
        for(int i = 1; i < 5; i++)
        {
            pi.setItem(i, this.books[i-1]);
        }
    }

    public void unloadPassiveAbilities(MineQuestPlayer player)
    {
        player.getPlayer().removePotionEffect(PotionEffectType.SPEED);
    }

    public void constantSpeed(final MineQuestPlayer player)
    {
        player.getPlayer().addPotionEffect(speed);

    }

    public void throwGrenade(MineQuestPlayer player)
    {
        if (!Skill.EXPLOSIVE_GRENADE.cooldown(player))
        {
            return;
        }
        ThrownPotion tp = player.getPlayer().launchProjectile(ThrownPotion.class);
        Potion p = new Potion(PotionType.POISON);
        p.setSplash(true);
        tp.setItem(p.toItemStack(1));
        tp.setBounce(false);
        tp.setVelocity(player.getPlayer().getEyeLocation().getDirection().normalize().multiply(0.5));
        tp.setMetadata("grenade", new FixedMetadataValue(player.getPlugin(), "derp"));

    }
    
    public void iceTrap(final MineQuestPlayer player)
    {
        if(!Skill.ICE_TRAP.cooldown(player))
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
                if(!icetrapExcludes.contains(a.getType()))
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
                    BlockUtil.temporaryChange(msp, blocks, Material.ICE, (byte)0, 100);
            }
        
        new BukkitRunnable()
        {
            final FPlayer fme = FPlayers.i.get(player.getPlayer());
            int i = 0;
            
            public void run()
            {
                Arrow arrow = (Arrow) loc.getWorld().spawnEntity(loc, EntityType.ARROW);
                arrow.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.ICE);
                for(Entity e : arrow.getNearbyEntities(5, 5, 5))
                    if(e instanceof LivingEntity && e != fme.getPlayer())
                    {
                        boolean dam = true;
                        if(e instanceof Player)
                            if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtLeast(Relation.ALLY))
                                dam = false;
                        if(e instanceof Wolf)
                            if(((Wolf)e).getOwner() == fme.getPlayer())
                                dam = false;
                        if(dam)
                            ((LivingEntity)e).addPotionEffect(ice);
                    }
                arrow.remove();
                if(++i >= 20)
                {
                    cancel();
                }
                
            }
        }.runTaskTimer(player.getPlugin(), 0L, 5L);
    }

    public void summonWolves(final MineQuestPlayer player)
    {
        if (!Skill.MASTER_OF_THE_WILD.cooldown(player))
        {
            return;
        }
        final ArrayList<Wolf> wolves = new ArrayList<Wolf>();

        for (int i = 0; i < 10; i++)
        {
            Entity e = player.getWorld().spawnEntity(player.getPlayer().getLocation(), EntityType.WOLF);
            player.getWorld().playSound(player.getPlayer().getLocation(), Sound.WOLF_HOWL, 1, 1);
            Wolf wolf = (Wolf) e;
            wolf.setOwner(player.getPlayer());
            wolf.setAngry(true);
            wolf.setMetadata("owner", new FixedMetadataValue(player.getPlugin(), ""));
            wolves.add(wolf);
        }

        new BukkitRunnable()
        {
            final OfflinePlayer p = Bukkit.getOfflinePlayer(player.getName());

            int i = 0;

            public void run()
            {
                if (i++ >= 30 || !p.isOnline())
                {
                    for (Wolf f : wolves)
                    {
                        f.removeMetadata("owner", player.getPlugin());
                        f.damage(100);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(player.getPlugin(), 0L, 20L);

    }

    public boolean block(MineQuestPlayer player, Player other, double damage)
    {
        if (!Skill.BLOCK.cooldown(player, 15000))
        {
            return false;
        }
        player.setVelocity(player.getPlayer().getLocation().getDirection().multiply(-1.5).setY(1));
        return true;
    }

    public void slot1(MineQuestPlayer player)
    {
        throwGrenade(player);

    }

    public void slot2(MineQuestPlayer player)
    {
        //handled in PlayerListener
    }

    public void slot3(MineQuestPlayer player)
    {
        iceTrap(player);

    }

    public void slot4(MineQuestPlayer player)
    {
        summonWolves(player);
    }
    
    public ItemStack[] getSkillBooks()
    {
        return this.books;
    }
    
    public List<String> getPassiveDescription()
    {
        return Arrays.asList("You will run faster and never recieve fall damage");
    }
    
    public List<String> getBlockingDescription()
    {
        return Arrays.asList(String.format("%d second(s) cooldown", 15), "", "A succesful block knocks you back,", "giving you a chance to escape");
    }
}
