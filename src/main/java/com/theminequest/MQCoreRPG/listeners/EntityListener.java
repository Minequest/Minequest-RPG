package com.theminequest.MQCoreRPG.listeners;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.Abilities;
import com.theminequest.MQCoreRPG.abilities.Skill;
import com.theminequest.MQCoreRPG.abilities.AssassinAbilities;
import com.theminequest.MQCoreRPG.abilities.Classes;
import com.theminequest.MQCoreRPG.abilities.NecromancerAbilities;
import com.theminequest.MQCoreRPG.combat.FakeEntityDamageEvent;
import com.theminequest.MQCoreRPG.entity.NPC;
import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import com.theminequest.MQCoreRPG.managers.PlayerManager;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import java.util.EnumSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import static org.bukkit.entity.EntityType.*;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

public class EntityListener implements Listener
{
    
    private final MineQuestRPGRealms plugin;
    private final PlayerManager pm;
    private final PotionEffect magedam = new PotionEffect(PotionEffectType.SLOW, 40, 0);
    private final PotionEffect warriorweak = new PotionEffect(PotionEffectType.WEAKNESS, 100, 0);
        
    private final EnumSet<Material> weapons = EnumSet.of(Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.BOW);
    
    public EntityListener(MineQuestRPGRealms plugin)
    {
        this.plugin = plugin;
        this.pm = plugin.getPlayerManager();
    }
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDamageRecieve(final EntityDamageEvent event)
    {
        if(event instanceof FakeEntityDamageEvent)
            return;
        if(event.getEntity() instanceof Player == false)
            return;
        
        if(event.getEntity() instanceof NPC)
        {
            event.setCancelled(true);
            return;
        }
        
        final MineQuestPlayer player = pm.getPlayer((Player) event.getEntity());
        if(player == null)
            return;
        
        boolean killingSpree = false;
        
        if(event instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent even = (EntityDamageByEntityEvent) event;
            if(even.getDamager() instanceof Player == false)
                return;
            MineQuestPlayer attacker = pm.getPlayer((Player) even.getDamager());
            if(attacker == null)
                return;
            
            ItemStack hand = attacker.getPlayer().getItemInHand();
            Material type = hand != null ? hand.getType() : Material.AIR;
            // Maybe rather a EnumSet<Material> with the allowed materials
            if(!this.weapons.contains(type))
            {
                final short dur[] = new short[4];
                final ItemStack[] armor = player.getPlayer().getInventory().getArmorContents();
                for(int i = 0; i < armor.length; i++)
                {
                    if(armor[i] == null)
                    {
                        dur[i] = -1;
                        continue;
                    }
                    if(armor[i].getType().isBlock() || armor[i].getType().getMaxDurability() < 1)
                    {
                        dur[i] = -1;
                        continue;
                    }
                    dur[i] = armor[i].getDurability();
                }
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        for(int i = 0; i < armor.length; i++)
                        {
                            if(dur[i] < 0)
                                continue;
                            if(armor[i] == null)
                                continue;
                            if(armor[i].getType().isBlock() || armor[i].getType().getMaxDurability() < 1)
                                continue;
                            armor[i].setDurability(dur[i]);
                        }
                    }
                }.runTaskLater(plugin, 1L);
            }
            
            killingSpree = attacker.isEffectActive(Skill.KILLING_SPREE);
            if(killingSpree)
            {
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        ((Player)event.getEntity()).setNoDamageTicks(11);
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
        
        Abilities a = player.getPlayerClass().getAbilities();
        if(event.getCause() == DamageCause.FALL)
            if((player.isWarrior() && player.isEffectActive(Skill.SHOCKWAVE)) || player.isRanger())
            {
                event.setCancelled(true);
                return;
            }
        
        if(event instanceof EntityDamageByEntityEvent)
        {
            EntityDamageByEntityEvent even = (EntityDamageByEntityEvent) event;
            if(even.getDamager() instanceof Player == false)
                return;
            MineQuestPlayer attacker = pm.getPlayer((Player) even.getDamager());
            if(attacker == null)
                return;
            
            if(even.getCause() != DamageCause.CUSTOM)
                if(player.isBlocking())
                {
                    if(a.block(player, attacker.getPlayer(), even.getDamage()))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            
            if(killingSpree)
                player.setCombatTracker(attacker.getPlayer(), Skill.KILLING_SPREE);
            
            if(player.isMage())
            {
                ((Player)even.getDamager()).addPotionEffect(this.magedam,true);
            }
            
            if(attacker.isWarrior())
            {
                player.addPotionEffect(warriorweak);
            }
        }
    }
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDamageDeal(final EntityDamageByEntityEvent event)
    {
        if(event.getEntity() instanceof Player == false) return;
        boolean ranged = false;
        if(event.getDamager() instanceof Player == false)
        {
            if(event.getDamager() instanceof Projectile && ((Projectile)event.getDamager()).getShooter() instanceof Player)
            {
                ranged = true;
            }
            else
                return;
        }
        
        final MineQuestPlayer player;
        final MineQuestPlayer msp;
        
        if(ranged)
        {
            Vector start = null;
            for(MetadataValue mv : event.getDamager().getMetadata("rangeStart"))
            {
                if(mv.getOwningPlugin() == this.plugin)
                {
                    start = (Vector) mv.value();
                    break;
                }
            }
                        
            if(start != null)
            {
                Arrow damager = (Arrow) event.getDamager();
        	Vector end = damager.getLocation().toVector();
        	
        	double max = 5.0;
        	
        	double multiplier = 1 + (start.distance(end)*.07); 
                if(event.getEntity() instanceof Player && multiplier >= max)
                {
                    msp = this.pm.getPlayer((Player) event.getEntity());
                    if(msp != null)
                        msp.setCombatTracker((Player) damager.getShooter(), Skill.SHARP_SHOOTER);
                }
        	event.setDamage(event.getDamage()*Math.min(multiplier, max)); 
            }
            
        }
        else
        {
            player = this.pm.getPlayer((Player) event.getDamager());
            msp = this.pm.getPlayer((Player) event.getEntity());
            if(msp == null)
                return;
            Abilities a = player.getPlayerClass().getAbilities();
            if(player.isWarrior() && player.isEffectActive(Skill.REND))
            {
                player.setActiveEffect(Skill.REND, false);
                new BukkitRunnable()
                {
                    private int CURRENT_TICK = 0;
                    private final int MAX_TICKS = 5;
                    private final LivingEntity target = (LivingEntity) event.getEntity();
                    private final Player inflictor = (Player) event.getDamager();
                    
                    public void run()
                    {
                        if(this.CURRENT_TICK++ < this.MAX_TICKS)
                        {
                            if(target instanceof Player && msp != null)
                                msp.setCombatTracker(inflictor, Skill.REND);
                            target.damage(1.0D);
                            Bukkit.getPluginManager().callEvent(new FakeEntityDamageEvent(target, DamageCause.CUSTOM, 1.0D));
                        }
                        else
                            cancel();
                    }
                }.runTaskTimer(plugin, 20L, 20L);
            }
            else if(player.isAssassin())
            {
                AssassinAbilities aa = (AssassinAbilities) a;
                if(player.isEffectActive(Skill.KIDNEY_SHOT))
                {
                    aa.kidneyShotExecution(player, msp);
                    player.setActiveEffect(Skill.KIDNEY_SHOT, false);
                }
                aa.envenom(player, msp.getPlayer());
                // Backstab
                double viewangle = msp.getDirection().angle(player.getDirection());
                viewangle = (viewangle/(Math.PI*2))*360;
                Vector tpos = msp.getPlayer().getLocation().toVector();
                Vector ppos = player.getPlayer().getLocation().toVector();
                double posangle = Math.abs(ppos.subtract(tpos).normalize().angle(msp.getDirection()));
                posangle = (posangle/(Math.PI*2))*360;
                if(Math.abs(viewangle) < 30 && posangle > 90 && posangle < 270)
                {
                    player.getPlayer().sendMessage("backstab");
                    event.setDamage(event.getDamage()+1);
                }
            }
            else if(player.isNecromancer())
            {
                NecromancerAbilities na = (NecromancerAbilities) a;
                if(player.isEffectActive(Skill.CURSE))
                {
                    na.curseExecution(player, msp);
                    player.setActiveEffect(Skill.CURSE, false);
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamageLate(EntityDamageEvent event)
    {
        if(event.getEntity() instanceof Player && event.getEntity() instanceof NPC == false)
        {
            final MineQuestPlayer player = this.pm.getPlayer((Player) event.getEntity());
            if(player == null)
            {
                System.out.println("Weird player: "+event.getEntity());
                return;
            }
            if(player.hasCombatTracked())
            {
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        player.getCombatTacker();
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
    }
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteractEntity(PlayerInteractEntityEvent event)
    {
        if(event.getPlayer().isConversing())
            return;
        Entity e = event.getRightClicked();
        if(e instanceof com.theminequest.MQCoreRPG.entity.NPC)
        {
            com.theminequest.MQCoreRPG.entity.NPC npc = (com.theminequest.MQCoreRPG.entity.NPC) e;
            npc.startConversation(plugin, event.getPlayer());
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event)
    {
        if(event.getEntity() instanceof FallingBlock)
        {
            FallingBlock fb = (FallingBlock) event.getEntity();
            if((fb.getBlockId() == Material.LAVA.getId() || fb.getBlockId() == Material.OBSIDIAN.getId()) && fb.getBlockData() == 1)
            {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onCommandWolves(EntityTargetLivingEntityEvent event){
    	if(!(event.getTarget() instanceof Player))
            return;
    	if(!(event.getEntity() instanceof Wolf)) 
            return;
    	
    	final MineQuestPlayer player = this.pm.getPlayer((Player)event.getTarget());
        if(player == null)
            return;
    	Wolf wolf = (Wolf) event.getEntity();
    	if(wolf.getOwner() == null || wolf.getOwner() instanceof Player == false) return;
    	
    	final MineQuestPlayer owner = this.pm.getPlayer((Player) wolf.getOwner());
        if(owner == null)
            return;
        
    	FPlayer fme = FPlayers.i.get(player.getPlayer());
    	if(owner.isRanger())
    	{
    		if(!FPlayers.i.get(owner.getPlayer()).getRelationTo(fme).isAtMost(Relation.NEUTRAL))
                {
                    event.setCancelled(true);
                }
    		
    	}
    	
    }
    
    EnumSet<EntityType> undead = EnumSet.of(SKELETON, ZOMBIE, PIG_ZOMBIE);
    
    @EventHandler
    public void onUndeadTarget(EntityTargetLivingEntityEvent event){
    	if(!(event.getTarget() instanceof Player)) return;
        if(event.getTarget() instanceof NPC)
        {
            event.setCancelled(true);
            return;
        }
    	if(!undead.contains(event.getEntityType())) return;
        
    	final MineQuestPlayer player = this.pm.getPlayer((Player)event.getTarget());
        
        if(player == null)
        {
            System.out.println(event.getTarget());
            return;
        }
        
        if(player.isNecromancer() && event.getEntityType() != EntityType.SKELETON)
        {
            event.setCancelled(true);
        }
        
        if(!event.getEntity().hasMetadata("owner"))
        {
            if(player.isNecromancer())
                event.setCancelled(true);
            return;
        }
        
        String fid = null;
        for(MetadataValue v : event.getEntity().getMetadata("owner"))
        {
            if(v.getOwningPlugin() == this.plugin)
            {
                fid = v.asString();
                break;
            }
        }
        if(fid == null)
        {
            if(player.isNecromancer())
            {
                event.setCancelled(true);
            }
            return;
        }
        
        FPlayer f = FPlayers.i.get(fid);
        if(f.getFaction().isNone() && event.getTarget() == f.getPlayer())
        {
            event.setCancelled(true);
            return;
        }
        
        if(FPlayers.i.get((Player)event.getTarget()).getRelationTo(f).isAtLeast(Relation.ALLY))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPetAttack(EntityDamageByEntityEvent event)
    {
        if(event.getEntity() instanceof Player == false)
            return;
        if(!event.getDamager().hasMetadata("owner"))
            return;
        Player p = (Player) event.getEntity();
        MineQuestPlayer player = this.pm.getPlayer(p);
        if(player == null)
            return;
        if(event.getDamager() instanceof Wolf)
        {
            Wolf w = (Wolf) event.getDamager();
            if(w.getOwner() != null)
            {
                Player o = (Player) w.getOwner();
                player.setCombatTracker(o, Skill.MASTER_OF_THE_WILD);
            }
        }
        else if(event.getDamager() instanceof Skeleton)
        {
            Skeleton s = (Skeleton) event.getDamager();
            if(s.getSkeletonType() == Skeleton.SkeletonType.NORMAL)
                return;
            String owner = "";
            if(s.hasMetadata("owner"))
                for(MetadataValue mv : s.getMetadata("owner"))
                    if(mv.getOwningPlugin() == this.plugin)
                    {
                        owner = mv.asString();
                        break;
                    }
            if(owner.isEmpty())
                return;
            Player o = Bukkit.getPlayerExact(owner);
            if(o == null)
                return;
            player.setCombatTracker(o, Skill.SUMMON_UNDEAD);
        }
    }
    
    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event)
    {
        if(event.getEntity() instanceof Arrow == false)
            return;
        if(event.getEntity().getShooter() instanceof Player == false)
            return;
        
        MineQuestPlayer player = this.pm.getPlayer((Player) event.getEntity().getShooter());
        if(player == null)
            return;
        
        if(!player.isRanger())
            return;
        
        event.getEntity().setMetadata("rangeStart", new FixedMetadataValue(this.plugin, event.getEntity().getLocation().toVector()));
        
    }
    
    @EventHandler
    public void projectileHit(final ProjectileHitEvent event){
    	if(event.getEntity() instanceof ThrownPotion)
    	{
    		Projectile e = event.getEntity();
    		if(e.hasMetadata("grenade"))
    		{
                    boolean grenade = false;
                    for(MetadataValue v : e.getMetadata("grenade"))
                    {
                        if(v.getOwningPlugin() == this.plugin)
                        {
                            grenade = true;
                            break;
                        }
                    }
                    if(!grenade)
                        return;
                    
                    FPlayer fme = FPlayers.i.get((Player)event.getEntity().getShooter());
                    Location loc = e.getLocation();
                    // I think this will suffice as damage ;)
                    e.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 0F, false, false);
                    for(Entity ent : e.getNearbyEntities(5, 5, 5))
                    {
                            if(ent instanceof LivingEntity && ent != e.getShooter())
                            {
                                boolean dam = true;
                                if(ent instanceof Player)
                                {
                                    Player p = (Player)ent;
                                    if(p == e.getShooter() || FPlayers.i.get(p).getRelationTo(fme).isAtLeast(Relation.ALLY))
                                        dam = false;
                                    if(dam)
                                    {
                                        
                                        MineQuestPlayer msp = this.pm.getPlayer(p);
                                        if(msp != null)
                                            msp.setCombatTracker((Player)event.getEntity().getShooter(), Skill.EXPLOSIVE_GRENADE);
                                        Bukkit.getPluginManager().callEvent(new FakeEntityDamageEvent(p, DamageCause.CUSTOM, 1.0D));
                                    }
                                }
                                else if(ent instanceof Wolf)
                                {
                                    if(((Wolf)ent).getOwner() == e.getShooter() && ent.hasMetadata("owner"))
                                        dam = false;
                                }
                                if(dam)
                                {
                                     ((LivingEntity)ent).damage(5, e.getShooter());
                                     ((LivingEntity)ent).addPotionEffect(PotionEffectType.CONFUSION.createEffect(40, 3));
                                }
                            }
                    }
                    event.getEntity().removeMetadata("grenade", plugin);
    		}
    	}
        else if(event.getEntity() instanceof Arrow)
        {
            if(event.getEntity().hasMetadata("rangeStart"))
            {
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        event.getEntity().removeMetadata("rangeStart", plugin);
                    }
                }.runTaskLater(this.plugin, 1L);
            }
        }
    }
    
    @EventHandler
    public void potionSplash(PotionSplashEvent event){
    	if(event.getEntity().hasMetadata("grenade"))
    	{
            event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPortalEnter(EntityPortalEnterEvent event)
    {
        if(event.getEntity() instanceof Player == false)
            return;
        
        if(!event.getLocation().getWorld().getName().toLowerCase().contains("MineQuestRPG"))
            return;
        
        MineQuestPlayer msp = this.pm.getPlayer((Player)event.getEntity());
        
        if(msp.getPlayerClass() == Classes.NONE)
            msp.getPlayer().sendMessage(ChatColor.RED+"Please choose a class before you can enter this world");
    }
}
