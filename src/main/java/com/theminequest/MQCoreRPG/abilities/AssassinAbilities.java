package com.theminequest.MQCoreRPG.abilities;

import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class AssassinAbilities extends Abilities
{
    
    private final PotionEffect vanish = new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0);
    private final PotionEffect kblind = new PotionEffect(PotionEffectType.BLINDNESS, 60, 9);
    private final PotionEffect kslow = new PotionEffect(PotionEffectType.SLOW, 60, 9);
    private final PotionEffect kweak = new PotionEffect(PotionEffectType.WEAKNESS, 60, 9);
    private final PotionEffect eslow = new PotionEffect(PotionEffectType.SLOW, 60, 0);
    
    private final PotionEffect block1 = new PotionEffect(PotionEffectType.WEAKNESS, 40, 1000);
    private final PotionEffect block2 = new PotionEffect(PotionEffectType.SLOW, 40, 1000);
    private final PotionEffect block3 = new PotionEffect(PotionEffectType.BLINDNESS, 40, 1000);
        
    protected AssassinAbilities()
    {	
        ItemStack vanish = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta =  vanish.getItemMeta();
        meta.setDisplayName("Vanish");
        meta.setLore(Arrays.asList(String.format(active, Skill.VANISH.getCooldown()), "", "Disappear ", "for 2 seconds"));
        vanish.setItemMeta(meta);
        ItemStack envenom = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  envenom.getItemMeta();
        meta.setDisplayName("Envenom");
        meta.setLore(Arrays.asList(String.format(passive), "", "Slows enemies ", "when you hit them"));
        envenom.setItemMeta(meta);
        ItemStack kidney = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  kidney.getItemMeta();
        meta.setDisplayName("Kidney Shot");
        meta.setLore(Arrays.asList(String.format(active, Skill.KIDNEY_SHOT.getCooldown()), "", "Stuns enemies ", "for 3 seconds "));
        kidney.setItemMeta(meta);
        ItemStack killingSpree = new ItemStack(Material.ENCHANTED_BOOK);
        meta =  killingSpree.getItemMeta();
        meta.setDisplayName("Killing Spree");
        meta.setLore(Arrays.asList(String.format(active, Skill.KILLING_SPREE.getCooldown()), "","Allows you ", "to attack your ", "enemies twice ", "as fast"));
        killingSpree.setItemMeta(meta);
        
        this.books[0] = parseBook(vanish);
        this.books[1] = parseBook(envenom);
        this.books[2] = parseBook(kidney);
        this.books[3] = parseBook(killingSpree);
    }
    
    public void vanish(final MineQuestPlayer player)
    {
        if(!Skill.VANISH.cooldown(player))
            return;
        player.getPlayer().addPotionEffect(vanish);
        int id = player.getPlayer().getEntityId();
        for(Player other : Bukkit.getOnlinePlayers())
            if(other != player.getPlayer())
                other.hidePlayer(player.getPlayer());
        
        new BukkitRunnable() 
        {
            public void run()
            {
                for(Player other : Bukkit.getOnlinePlayers())
                    if(other != player.getPlayer())
                        other.showPlayer(player.getPlayer());
            }
        }.runTaskLater(player.getPlugin(), 40L);
        
    }
    
    public void envenom(final MineQuestPlayer player, LivingEntity target)
    {
        target.addPotionEffect(eslow);
    }
    
    public void kidneyShot(MineQuestPlayer player)
    {
        if(player.isEffectActive(Skill.KIDNEY_SHOT) || !Skill.KIDNEY_SHOT.cooldown(player))
            return;
        player.setActiveEffect(Skill.KIDNEY_SHOT, true);
    }
    
    public void kidneyShotExecution(MineQuestPlayer player, MineQuestPlayer target)
    {
        if(!player.isEffectActive(Skill.KIDNEY_SHOT))
            return;
        player.getPlayer().sendMessage("Kidney shot!");
        //BlockUtil.cagePlayer(target, Material.BEDROCK, (byte)0, true, 60);
        Player ptar = target.getPlayer();
        ptar.addPotionEffect(kblind);
        ptar.addPotionEffect(kslow);
        ptar.addPotionEffect(kweak);
        target.setCombatTracker(player.getPlayer(), Skill.KIDNEY_SHOT);
    }
    
    public void killingSpree(final MineQuestPlayer player)
    {
        if(player.isEffectActive(Skill.KILLING_SPREE) || !Skill.KILLING_SPREE.cooldown(player))
            return;
        player.setActiveEffect(Skill.KILLING_SPREE, true);
        new BukkitRunnable()
        {
             
            public void run()
            {
                player.setActiveEffect(Skill.KILLING_SPREE, false);
            }
        }.runTaskLater(player.getPlugin(), 200L);
    }
    
    public boolean block(MineQuestPlayer player, Player other, double damage)
    {
        if(!Skill.BLOCK.cooldown(player, 15000))
            return false;
        MineQuestPlayer mpOther = player.getPlugin().getPlayerManager().getPlayer(other);
        if(mpOther != null)
        {
            Location loc = other.getLocation();
            loc = loc.subtract(other.getEyeLocation().getDirection().setY(0).normalize());
            Material feet = loc.getBlock().getType();
            Material head = loc.getBlock().getRelative(BlockFace.UP).getType();
            if(isSafe(feet) && isSafe(head) && loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid())
            {
                loc.setPitch(0);
                loc.setYaw(other.getEyeLocation().getYaw());
                player.getPlayer().teleport(loc);
                return true;
            }
        }
        other.sendMessage(ChatColor.RED+"You cannot block in this situation");
        return false;
    }
    
    private boolean isSafe(Material m)
    {
        return !m.isSolid() && m != Material.LAVA && m != Material.STATIONARY_LAVA;
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
    	vanish(player);
    }
    
    public void slot2(MineQuestPlayer player)
    {
        // Uncallable, its passive :3
    }
    
    public void slot3(MineQuestPlayer player)
    {
    	kidneyShot(player);
    }
    
    public void slot4(MineQuestPlayer player)
    {
    	killingSpree(player);
    }
    
    public ItemStack[] getSkillBooks()
    {
        return this.books;
    }
    
    public List<String> getPassiveDescription()
    {
        return Arrays.asList("Backstabbing deals more damage");
    }
    
    public List<String> getBlockingDescription()
    {
        return Arrays.asList(String.format("%d second(s) cooldown", 15), "", "A succesful block teleports you behind the enemy.");
    }
}
