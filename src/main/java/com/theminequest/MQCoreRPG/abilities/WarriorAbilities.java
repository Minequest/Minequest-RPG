package com.theminequest.MQCoreRPG.abilities;

import com.theminequest.MQCoreRPG.combat.FakeEntityDamageEvent;
import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import com.theminequest.MQCoreRPG.util.BlockUtil;
import com.theminequest.MQCoreRPG.util.radius.Relative;
import com.theminequest.MQCoreRPG.util.radius.Wave;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WarriorAbilities extends Abilities
{    
    private final PotionEffect beserker = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 0);
    
    private final PotionEffect lastStand = new PotionEffect(PotionEffectType.ABSORPTION, 300, 3);
    
    private final Random rnd = new Random();
    
    protected WarriorAbilities()
    {
        ItemStack leap = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta =  leap.getItemMeta();
        meta.setDisplayName("Shockwave");
        meta.setLore(Arrays.asList(String.format(active, Skill.SHOCKWAVE.getCooldown()), "", "Create a shockwave ", "at your feet.", "Deals damage to ", "nearby enemies."));
        leap.setItemMeta(meta);
        ItemStack rend = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  rend.getItemMeta();
        meta.setDisplayName("Rend");
        meta.setLore(Arrays.asList(String.format(active, Skill.REND.getCooldown()), "", "Your next attack ", "makes your enemy ", "bleed for 5 seconds"));
        rend.setItemMeta(meta);
        ItemStack beserker = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  beserker.getItemMeta();
        meta.setDisplayName("Beserker");
        meta.setLore(Arrays.asList(String.format(active, Skill.BESERKER.getCooldown()), "", "Grants you a ", "higher attack for ", " 5 seconds"));
        beserker.setItemMeta(meta);
        ItemStack lastStand = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  lastStand.getItemMeta();
        meta.setDisplayName("Last Stand");
        meta.setLore(Arrays.asList(String.format(active, Skill.LAST_STAND.getCooldown()), "", "Heals yourself ", "completely and ", "protects you ", "from damage"));
        lastStand.setItemMeta(meta);
        
        this.books[0] = parseBook(leap);
        this.books[1] = parseBook(rend);
        this.books[2] = parseBook(beserker);
        this.books[3] = parseBook(lastStand);
    }
    
    public void shockwave(final MineQuestPlayer player)
    {
        if(!Skill.SHOCKWAVE.cooldown(player))
        {
            return;
        }
        leapDamage(player);
        /*Vector dir = player.getDirection();
        dir.normalize().multiply(2);
        if(dir.getY() < 1) 
            dir.setY(1);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.setActiveEffect(Skill.SHOCKWAVE, true);
            }
        }.runTaskLater(player.getPlugin(), 5L);
        player.setVelocity(dir);*/
    }
    
    public void leapDamage(final MineQuestPlayer player)
    {
        FPlayer fme = FPlayers.i.get(player.getPlayer());
        for(Entity e : player.getPlayer().getNearbyEntities(5, 5, 5))
        {
            if(e instanceof LivingEntity == false)
                return;
            boolean dam = true;
            if(e instanceof Player)
            {
                if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtLeast(Relation.ALLY))
                    dam = false;
                if(dam)
                {
                    MineQuestPlayer msp = player.getPlugin().getPlayerManager().getPlayer((Player)e);
                    if(msp != null)
                        msp.setCombatTracker(player.getPlayer(), Skill.SHOCKWAVE);
                }
            }
            ((LivingEntity)e).damage(4D, player.getPlayer());
            Bukkit.getPluginManager().callEvent(new FakeEntityDamageEvent(e, DamageCause.CUSTOM, 1.0D));
        }
        player.getWorld().playSound(player.getPlayer().getLocation(), Sound.IRONGOLEM_HIT, 1F, 0F);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                 player.setActiveEffect(Skill.SHOCKWAVE, false);
            }
        }.runTaskLater(player.getPlugin(), 10L);
        Location loc = player.getPlayer().getLocation();
        Block b = loc.getBlock();
        Vector bd = loc.clone().subtract(b.getLocation()).toVector();
        Block a;
        for(Relative r : Wave.getRadius(5).getRelatives())
        {
            a = r.getRelative(b);
            a.getWorld().playEffect(BlockUtil.getHighestUsableBlock(a, new HashSet<Material>(), false).getLocation().add(bd), Effect.STEP_SOUND, Material.STONE);
        }
       
    }
    
    public void rend(MineQuestPlayer player)
    {
        if(Skill.REND.cooldown(player))
            player.setActiveEffect(Skill.REND, true);
    }
    
    public void beserker(final MineQuestPlayer player)
    {
        if(Skill.BESERKER.cooldown(player))
        {
            player.getWorld().playSound(player.getPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1, rnd.nextInt(2));
            player.addPotionEffect(this.beserker);
        }
    }
    
    public void lastStand(final MineQuestPlayer player)
    {
        if(Skill.LAST_STAND.cooldown(player))
        {
            player.setHealth(Integer.MAX_VALUE);
            player.addPotionEffect(this.lastStand);
        }
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
    
    public boolean block(MineQuestPlayer player, Player other, double damage)
    {
        if(!Skill.BLOCK.cooldown(player, 20000))
            return false;
        player.getPlayer().sendMessage(ChatColor.GREEN+"Countered!");
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player.getPlayer(), other, DamageCause.CUSTOM, damage);
        Bukkit.getPluginManager().callEvent(event);
        if(!event.isCancelled())
        {
            if(other instanceof Player)
            {
                MineQuestPlayer msp = player.getPlugin().getPlayerManager().getPlayer((Player)other);
                if(msp != null)
                    msp.setCombatTracker(player.getPlayer(), Skill.BLOCK);
            }
            other.damage(damage, player.getPlayer());
        }
        return true;
    }
    
    public void slot1(MineQuestPlayer player)
    {
        shockwave(player);
    }
    
    public void slot2(MineQuestPlayer player)
    {
        rend(player);
    }
    
    public void slot3(MineQuestPlayer player)
    {
        beserker(player);
    }
    
    public void slot4(MineQuestPlayer player)
    {
        lastStand(player);
    }
    
    public ItemStack[] getSkillBooks()
    {
        return this.books;
    }
    
    public List<String> getPassiveDescription()
    {
        return Arrays.asList("Attacking an enemy will shortly weaken them.");
    }
    
    public List<String> getBlockingDescription()
    {
        return Arrays.asList(String.format("%d second(s) cooldown", 10), "", "A succesful block will counter the enemy.");
    }

}
