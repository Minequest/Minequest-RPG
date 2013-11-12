package com.theminequest.MQCoreRPG.listeners;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.ClassTag;
import com.theminequest.MQCoreRPG.abilities.Classes;
import com.theminequest.MQCoreRPG.abilities.Skill;
import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import java.util.Map;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import static org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP;
import static org.bukkit.event.inventory.InventoryAction.PICKUP_ALL;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryListener implements Listener
{
    
    private final MineQuestRPG plugin;
    
    private final PlayerListener pl;
    
    public InventoryListener(MineQuestRPG plugin, PlayerListener pl)
    {
        this.plugin = plugin;
        this.pl = pl;
    }
    
    @EventHandler (priority = EventPriority.LOW)
    public void onChoose(InventoryClickEvent event)
    {
        if(event.getWhoClicked() instanceof Player == false)
            return;
        final Player p = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();
        String[] parts = title.split(" ");
        if(parts.length != 2)
            return;
        if(!parts[1].equalsIgnoreCase("trainer"))
            return;
        Classes clazz;
        try
        {
            clazz = Classes.valueOf(parts[0].toUpperCase());
        }
        catch(IllegalArgumentException ex)
        {
            return;
        }
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        MineQuestRPGPlayer player = this.plugin.getPlayerManager().getPlayer(p);
        if(player == null)
            return;
        ItemStack i = event.getCurrentItem();
        if(i == null)
            return;
        ItemMeta meta = i.getItemMeta();
        if(meta == null)
            return;
        if(meta.hasDisplayName())
        {
            if(meta.getDisplayName().toLowerCase().contains("decline") || meta.getDisplayName().toLowerCase().contains("accept"))
            {
                if(meta.getDisplayName().toLowerCase().contains("accept"))
                {
                    Classes c = player.getPlayerClass();
                    boolean giveClass = true;
                    if(c != Classes.NONE && !player.getWorld().getName().toLowerCase().contains("MineQuestRPG"))
                    {
                         EconomyResponse er = MineQuestRPG.getEconomy().withdrawPlayer(player.getName(), 5000D);
                         if(!er.transactionSuccess())
                         {
                             giveClass = false;
                         }
                    }
                    if(giveClass)
                    {
                        player.setPlayerClass(clazz);
                        p.sendMessage(ChatColor.GREEN+"Congratulations! You are now a "+parts[0]);
                        PlayerInventory inv = p.getInventory();
                        ItemStack[] contents = inv.getContents();
                        for(int j = 0; j < contents.length; j++)
                        {
                            if(Skill.isSkillBook(contents[j]))
                                inv.setItem(j, null);
                        }
                        c.getAbilities().unloadPassiveAbilities(player);
                        clazz.getAbilities().loadPassiveAbilities(player);
                        p.updateInventory();
                        this.pl.clazztags.put(p.getName(), ClassTag.getClassTag(clazz));
                    }
                    else
                        p.sendMessage(ChatColor.RED+"You do not possess sufficient funds to change your class.");
                }
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        p.closeInventory();
                    }
                }.runTask(plugin);
                
            }
        }
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        //System.out.println(event.getInventory().getSize());
        // Inb4 I might need it once more :3
        /*System.out.println("Inventory click start");
        System.out.println(event.getAction().name());
        System.out.println(event.getClick().name());
        System.out.println(event.getCurrentItem());
        System.out.println(event.getCursor());
        System.out.println(event.getRawSlot());
        System.out.println(event.getSlotType());
        System.out.println("Inventory click end");//*/
        
        if(event.getWhoClicked() instanceof Player == false) return;
        Player player = (Player) event.getWhoClicked();

        // Allow if it is a player inventory?
        if(event.getView().getType() == InventoryType.CRAFTING)
            return;
        
        ItemStack i = event.isShiftClick() ? event.getCurrentItem() : event.getCursor();
        if(!Skill.isSkillBook(i) && event.getAction() != HOTBAR_SWAP)
            return;
        
        boolean drop = false;
        switch(event.getAction())
        {
            case CLONE_STACK:
                // If I'm correct, this is a creative action
                break;
            case COLLECT_TO_CURSOR:
                // This will get everything on the cursor,
                //  does not apply to skill books as they have different ItemMeta
                break;
            case DROP_ALL_CURSOR:
                // This will drop items, and it should not
            case DROP_ALL_SLOT:
                // This will drop items, and it should not
                 event.setCancelled(true);
                 event.setResult(Event.Result.DENY);
                drop = true;
            case HOTBAR_MOVE_AND_READD:
                // This only happens when stuff gets moved towards the
                //  hotbar
                break;
            case HOTBAR_SWAP:
                // Only deny when the rawslot is located in the other inventory
                int chestSize = event.getInventory().getSize();
                int rawSlot = chestSize + 27 + event.getHotbarButton();
                ItemStack hoti = event.getView().getItem(rawSlot);
                if(event.getRawSlot() < chestSize && Skill.isSkillBook(hoti))
                {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                }
                break;
            case MOVE_TO_OTHER_INVENTORY:
                if(event.getRawSlot() >= event.getInventory().getSize())
                {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                }
                break;
            case NOTHING:
                // Did we even do anything? I guess not
                break;
            
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PICKUP_SOME:
                if(event.getCurrentItem().getType().name().toLowerCase().contains("apple"))
                {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                }
            case PICKUP_ALL:
                // Picking up
                break;
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
                if(event.getRawSlot() < event.getInventory().getSize())
                {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                }
            case SWAP_WITH_CURSOR:
                if(event.getRawSlot() < event.getInventory().getSize())
                {
                    event.setCancelled(true);
                    event.setResult(Event.Result.DENY);
                }
            case UNKNOWN:
                // How am I supposed to handle that what
                //  Bukkit cannot even properly name xD
                break;

        }//*/
        if(event.isCancelled())
        {
            String what = "skill books";
            Material type = i.getType();
            if(type == Material.GOLDEN_APPLE || type == Material.APPLE)
            {
                what = "health kits";
            }
            player.sendMessage(ChatColor.RED+"You are not allowed to "+(drop ? "drop" : "store")+" your "+what);
        }
    }

    @EventHandler
    public void onDrag(final InventoryDragEvent event)
    {
        if(event.getWhoClicked() instanceof Player == false) return;
        Player player = (Player) event.getWhoClicked();

        int size = event.getView().getTopInventory().getSize();
        
        boolean handle = false;
        Material type = event.getOldCursor().getType();
        for(Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet())
            if(Skill.isSkillBook(entry.getValue()) && (entry.getKey() < size || (type == Material.GOLDEN_APPLE || type == Material.APPLE )))
                handle = true;
        
        if(!handle)
            return;
        
        event.setCancelled(true);
        String what = "skill books";
        
        if(type == Material.GOLDEN_APPLE || type == Material.APPLE)
        {
            what = "health kits";
        }
        player.sendMessage(ChatColor.RED+"You are not allowed to store your "+what);
        
        /*new BukkitRunnable()
        {
            @Override
            public void run()
            {
                ItemStack i = event.getOldCursor();
                int placed = 0;
                for(Map.Entry<Integer, ItemStack> itemEntry : event.getNewItems().entrySet())
                {
                    if(itemEntry.getKey() >= event.getInventory().getSize())
                    {
                        placed += itemEntry.getValue().getAmount();
                        event.getView().setItem(itemEntry.getKey(), itemEntry.getValue());
                    }
                }
                i.setAmount(i.getAmount() - placed);
                if(i.getAmount() < 1)
                {
                    i.setType(Material.AIR);
                }
                event.getView().setCursor(i);
            }
        }.runTask(plugin);*/
    }
    
    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDrop(final PlayerDropItemEvent event)
    {
        ItemStack i = event.getItemDrop().getItemStack();
        if(Skill.isSkillBook(i))
        {
            event.setCancelled(true);
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    event.getPlayer().updateInventory();
                }
            }.runTask(plugin);
        }
    }
    
    @EventHandler
    public void onClose(InventoryCloseEvent event)
    {
        if(event.getPlayer() instanceof Player == false)
            return;
        Player player = (Player) event.getPlayer();
        ItemStack i = event.getView().getCursor().clone();
        
        if(Skill.isSkillBook(i))
        {
            if(player.getInventory().firstEmpty() == -1)
            {
                Inventory view = player.getInventory();
                ItemStack zero = view.getItem(0).clone();
                view.setItem(0, i);
                event.getView().setCursor(zero);
            }
            else
            {
                player.getInventory().setItem(player.getInventory().firstEmpty(), i);
                event.getView().setCursor(null);
            }
        }
    }
    
    //@EventHandler
    public void onBrew(BrewEvent event)
    {
        event.setCancelled(true);
    }
}
