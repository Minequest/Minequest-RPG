package com.theminequest.MQCoreRPG.abilities;

import com.theminequest.MQCoreRPG.combat.FakeEntityDamageEvent;
import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import com.theminequest.MQCoreRPG.managers.PlayerManager;
import com.theminequest.MQCoreRPG.util.BlockUtil;
import com.theminequest.MQCoreRPG.util.radius.Relative;
import com.theminequest.MQCoreRPG.util.radius.Wave;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MageAbilities extends Abilities
{
        
    private final PotionEffect block = new PotionEffect(PotionEffectType.SLOW, 60, 4);
    
    private final PotionEffect arcane = new PotionEffect(PotionEffectType.ABSORPTION, 300, 1);
    
    private final List<Vector> relParticlePos = new ArrayList<Vector>();
    
    private final EnumSet<Material> notp = EnumSet.of(
        Material.LAVA,
        Material.STATIONARY_LAVA,
        Material.FIRE,
        Material.WEB,
        Material.CACTUS
        );
    
    protected MageAbilities()
    {
        ItemStack call = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta =  call.getItemMeta();
        meta.setDisplayName("Call lightning");
        meta.setLore(Arrays.asList(String.format(active, Skill.CALL_LIGHTNING.getCooldown()), "", "Call lightning from ", "the sky, dealing ", "AoE damage to ", "nearby enemies"));
        call.setItemMeta(meta);
        ItemStack blink = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  blink.getItemMeta();
        meta.setDisplayName("Arcane Shield");
        meta.setLore(Arrays.asList(String.format(active, Skill.ARCANE_SHIELD.getCooldown()), "","Less damage", "for 15 seconds"));
        blink.setItemMeta(meta);
        ItemStack frost = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  frost.getItemMeta();
        meta.setDisplayName("Frost Nova");
        meta.setLore(Arrays.asList(String.format(active, Skill.FROST_NOVA.getCooldown()), "","Freeze players ", "in an area of ", "3 blocks around you"));
        frost.setItemMeta(meta);
        ItemStack pyro = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  pyro.getItemMeta();
        meta.setDisplayName("Pyroblast");
        meta.setLore(Arrays.asList(String.format(active, Skill.PYROBLAST.getCooldown()), "","Fires a ", "ball of fire ", "that damages and ", "sets enemies on ", "fire"));
        pyro.setItemMeta(meta);
        
        this.books[0] = parseBook(call);
        this.books[1] = parseBook(blink);
        this.books[2] = parseBook(frost);
        this.books[3] = parseBook(pyro);
        
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
    
    public void callLightning(MineQuestPlayer player)
    {
        if(!Skill.CALL_LIGHTNING.cooldown(player))
            return;
        Block target = player.getPlayer().getTargetBlock(null, 10);
        
        Block highest = BlockUtil.getHighestUsableBlock(target, new HashSet<Material>(), false);
        if(highest.getType().isSolid())
            highest = highest.getRelative(BlockFace.UP);
        Location loc = highest.getLocation().add(0.5, 0, 0.5);
        FPlayer fme = FPlayers.i.get(player.getPlayer());
        LightningStrike ls = loc.getWorld().strikeLightningEffect(loc);
        List<Entity> le = ls.getNearbyEntities(2, 2, 2);
        Vector lsVec = ls.getLocation().toVector();
        // Damage each entity
        for(Entity e : le)
        {
            if(e == null || e instanceof LivingEntity == false)
                continue;
            boolean dam = true;
            if(e instanceof Player)
            {
                if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtLeast(Relation.ALLY) || e == fme.getPlayer())
                    dam = false;
                
                if(dam)
                {
                	MineQuestPlayer msp = player.getPlugin().getPlayerManager().getPlayer((Player)e);
                    if(msp != null)
                        msp.setCombatTracker(player.getPlayer(), Skill.CALL_LIGHTNING);
                    Bukkit.getPluginManager().callEvent(new FakeEntityDamageEvent(e, EntityDamageEvent.DamageCause.CUSTOM, 1.0D));
                }
            }
            if(!dam)
                continue;
            ((LivingEntity)e).damage(4, player.getPlayer());
            Vector v = e.getLocation().toVector().subtract(lsVec).normalize().multiply(0.5);
            e.setVelocity(v);
        }
    }
    
    public void arcaneShield(final MineQuestPlayer player)
    {
        if(!Skill.ARCANE_SHIELD.cooldown(player))
            return;
        player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENDERMAN_SCREAM, 1, 2);
        player.addPotionEffect(arcane);
        new BukkitRunnable() 
        {

            public void run()
            {
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENDERMAN_SCREAM, 1, 1);
            }
        }.runTaskLater(player.getPlugin(), arcane.getDuration());
    }
    
    
    
    public void frostNova(final MineQuestPlayer player)
    {
        if(!Skill.FROST_NOVA.cooldown(player))
            return;
        final Block b = player.getPlayer().getLocation().getBlock();
        final FPlayer fme = FPlayers.i.get(player.getPlayer());
        new BukkitRunnable()
        {
            int i = 0;
            Block it;
            Set<Integer> alreadyFrozen = new HashSet<Integer>();
            public void run()
            {
                Wave th = Wave.getRadius(i);
                if(th == null)
                    cancel();
                else
                {
                    final PlayerManager pm = player.getPlugin().getPlayerManager();
                    for(Relative r : th.getRelatives())
                    {
                        it = r.getRelative(b);
                        it = it.getWorld().getHighestBlockAt(it.getLocation());
                        if(it.getType().isSolid())
                            it = it.getRelative(BlockFace.UP);
                        it.getWorld().playEffect(it.getLocation().add(player.getPlugin().BLOCK_CENTER_OFFSET), Effect.STEP_SOUND, Material.ICE.getId());
                        Entity e = it.getWorld().spawnEntity(it.getLocation().add(0.5, 0, 0.5), EntityType.SNOWBALL);
                        List<Entity> near = e.getNearbyEntities(0.5D, 0.5D, 0.5D);
                        Location loc = e.getLocation();
                        e.remove();
                        for(final Entity en : near)
                        {
                            if(en instanceof Player && en != player.getPlayer())
                            {
                                if(alreadyFrozen.contains(en.getEntityId())) 
                                    continue;
                                alreadyFrozen.add(en.getEntityId());
                                if(FPlayers.i.get((Player)en).getRelationTo(fme).isAtLeast(Relation.ALLY))
                                    continue;
                                en.teleport(loc);
                                MineQuestPlayer other = pm.getPlayer((Player) en);
                                if(other != null)
                                    BlockUtil.cagePlayer(other, Material.ICE, (byte)0, true, 40);
                            }
                        }
                    }
                    i++;
                }
            }
            
        }.runTaskTimer(player.getPlugin(), 0L, 1L);
    }
    
    // Still needs faction support, plus I need to check the damage
    public void pyroblast(final MineQuestPlayer player)
    {
        if(!Skill.PYROBLAST.cooldown(player))
            return;
        Location loc = player.getPlayer().getEyeLocation();
        final FallingBlock fb = player.getWorld().spawnFallingBlock(loc, Material.LAVA, (byte)1);
        final Vector speed = player.getDirection().normalize().multiply(2);
        fb.setVelocity(speed);
        fb.setDropItem(false);
        final Location start = fb.getLocation().clone();
        new BukkitRunnable()
        {
            int i = 0;
            FPlayer fme = FPlayers.i.get(player.getPlayer());
            public void run()
            {
                if(!fb.isDead())
                {
                    fb.getWorld().playSound(fb.getLocation(), Sound.GHAST_FIREBALL, 1, 0);
                    fb.setVelocity(speed);
                    for(Vector relpos : relParticlePos)
                        fb.getWorld().playEffect(fb.getLocation().add(relpos), Effect.MOBSPAWNER_FLAMES, 0);
                    for(Entity e : fb.getNearbyEntities(3, 3, 3))
                    {
                        if(e instanceof LivingEntity && e != fme.getPlayer())
                        {
                            boolean dam = true;
                            if(e instanceof Player && !fme.getFaction().isNone())
                                if(FPlayers.i.get((Player)e).getRelationTo(fme).isAtLeast(Relation.ALLY))
                                    dam = false;
                            if(dam)
                            {
                                if(e instanceof Player)
                                {
                                	MineQuestPlayer msp = player.getPlugin().getPlayerManager().getPlayer((Player)e);
                                    if(msp != null)
                                        msp.setCombatTracker(player.getPlayer(), Skill.PYROBLAST);
                                }
                                ((LivingEntity)e).damage(10, player.getPlayer());
                                ((LivingEntity)e).setFireTicks(200);
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
                    else if(fb.getLocation().distanceSquared(start) >= 850)
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
                else
                    cancel();
            }
        }.runTaskTimer(player.getPlugin(), 0L, 1L);
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
        if(!Skill.BLOCK.cooldown(player, 15000))
            return false;
        other.addPotionEffect(this.block);
        return true;
    }
    
    public void slot1(MineQuestPlayer player)
    {
        callLightning(player);
    }
    
    public void slot2(MineQuestPlayer player)
    {
        arcaneShield(player);
    }
    
    public void slot3(MineQuestPlayer player)
    {
        frostNova(player);
    }
    public void slot4(MineQuestPlayer player)
    {
        pyroblast(player);
    }
    
    public ItemStack[] getSkillBooks()
    {
        return this.books;
    }
    
    public List<String> getPassiveDescription()
    {
        return Arrays.asList("When attacked, the other player gets slowed");
    }
    
    public List<String> getBlockingDescription()
    {
        return Arrays.asList(String.format("%d second(s) cooldown", 15), "", "A succesful block severely slows the enemy.");
    }
}
