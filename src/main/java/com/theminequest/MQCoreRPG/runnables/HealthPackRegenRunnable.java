package com.theminequest.MQCoreRPG.runnables;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.events.HealthPackUseEvent;
import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class HealthPackRegenRunnable extends BukkitRunnable
{
    
    public MineQuestRPG plugin;
    
    private final ItemStack healthpack = new ItemStack(Material.GOLDEN_APPLE, 10, (short)2);
    
    private final ItemStack singular;
    
    private final Map<String, Long> lastUsedTick = new HashMap<String, Long>();
    
    private long tick = 0;
    
    public HealthPackRegenRunnable(MineQuestRPG plugin)
    {
        this.plugin = plugin;
        ItemMeta meta = this.healthpack.getItemMeta();
        meta.setDisplayName(ChatColor.RESET+""+ChatColor.DARK_RED+"Health Kit");
        meta.setLore(Arrays.asList(
            ChatColor.RESET+""+ChatColor.AQUA+"Recharges one kit every "+ChatColor.RED+"10 seconds.",
            "",
            ChatColor.RESET+""+ChatColor.BLUE+"Heals 4 hearts per kit."
        ));
        this.healthpack.setItemMeta(meta);
        this.singular = this.healthpack.clone();
        this.singular.setAmount(1);
    }
    
    @Override
    public void run()
    {
        for(MineQuestPlayer player : plugin.getPlayerManager().getPlayers())
        {
            Player p = player.getPlayer();
            long last = tick - getLastUsage(p);
            if(last == 0 || last % 10 != 0)
                continue;
            Inventory inv = p.getInventory();
            int count = 0;
            int red = -1;
            ItemStack i;
            for(int slot = 0; slot < inv.getContents().length; slot++)
            {
                i = inv.getContents()[slot];
                if(i == null || i.getType() == Material.AIR)
                    continue;
                if(i.isSimilar(healthpack))
                    count += i.getAmount();
                else if(i.getType() == Material.APPLE && i.getDurability() == 2)
                    red = slot;
                    
            }
            i = p.getItemOnCursor();
            if(i != null)
            {
                if(i.getType() == Material.AIR)
                    ;
                else if(i.isSimilar(healthpack))
                    count += i.getAmount();
                else if(i.getType() == Material.APPLE && i.getDurability() == 2)
                    red = Integer.MIN_VALUE;
            }
            if(count == 0)
            {
                if(red > -1)
                {
                    i = inv.getContents()[red];
                    i.setType(Material.GOLDEN_APPLE);
                    i.setAmount(1);
                }
                else if(red == Integer.MAX_VALUE)
                {
                    i = p.getItemOnCursor();
                    i.setType(Material.GOLDEN_APPLE);
                    i.setAmount(1);
                }
                else
                {
                    inv.addItem(healthpack);
                }
            }
            else if(count < 10)
            {
                inv.addItem(singular);
            }
        }
        tick++;
    }
    
    public long getLastUsage(Player player)
    {
        if(!this.lastUsedTick.containsKey(player.getName()))
            this.lastUsedTick.put(player.getName(), tick);
        return this.lastUsedTick.get(player.getName());
    }
    
    public void usePack(Player player)
    {
        this.lastUsedTick.put(player.getName(), tick);
    }
    
    public BukkitTask start()
    {
        plugin.getServer().getPluginManager().registerEvents(new HealthPackUseListener(this), plugin);
        return super.runTaskTimer(plugin, 100L, 20L);
    }
    
    private class HealthPackUseListener implements Listener
    {
        HealthPackRegenRunnable updater;

        public HealthPackUseListener(HealthPackRegenRunnable updater)
        {
            this.updater = updater;
        }
        
        @EventHandler
        public void onHealthPack(HealthPackUseEvent event)
        {
            this.updater.usePack(event.getPlayer());
        }
    }
    
}
