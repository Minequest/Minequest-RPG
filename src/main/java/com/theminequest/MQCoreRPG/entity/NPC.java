package com.theminequest.MQCoreRPG.entity;

import com.theminequest.MQCoreRPG.abilities.ClassTag;
import com.theminequest.MQCoreRPG.abilities.Classes;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class NPC extends CraftPlayer
{
    private final static String PREFIX = "&4[&a%s&4]&r".replace('&', ChatColor.COLOR_CHAR);
    
    private final static ItemStack none = new ItemStack(Material.AIR);
    
    private final ItemStack[] contents = new ItemStack[]
    {
        none, none, none, none, none, none, none, none, none,
        none, none, none, none, none, none, none, none, none,
        none, none, none, none, none, none, none, none, none
    };
    
    Classes clazz;
    
    
    NPC(CraftServer server, EntityPlayerNPC player)
    {
        super(server, player);
        
    }
    
    private ItemStack setMeta(ItemStack i, String name, List<String> lore)
    {
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        i.setItemMeta(meta);
        return i;
    }
    
    public void setClazz(Classes clazz)
    {
        this.clazz = clazz;
        List<String> pd = clazz.getAbilities().getPassiveDescription();
        contents[2] = setMeta(new ItemStack(Material.PISTON_EXTENSION), "&r&ePassive".replace('&', ChatColor.COLOR_CHAR), pd);
        ItemStack[] books = clazz.getAbilities().getSkillBooks();
        for(int i = 0; i < books.length; i++)
        {
            contents[i+3] = books[i].clone();
        }
        pd = clazz.getAbilities().getBlockingDescription();
        contents[13] = setMeta(new ItemStack(Material.DIAMOND_SWORD), "&r&eBlock".replace('&', ChatColor.COLOR_CHAR), pd);
        contents[18] = setMeta(new ItemStack(Material.WOOL, 1, DyeColor.LIME.getWoolData()), "&aAccept".replace('&', ChatColor.COLOR_CHAR), null);
        contents[26] = setMeta(new ItemStack(Material.WOOL, 1, DyeColor.RED.getWoolData()), "&4Decline".replace('&', ChatColor.COLOR_CHAR), null);
    }
    
    public Classes getClazz()
    {
        return this.clazz;
    }
    
    public void startConversation(Plugin plugin, final Player player)
    {
        /*Conversation con = new ConversationFactory(plugin)
            .addConversationAbandonedListener(new NPCConversationAbandonedListener())
            .thatExcludesNonPlayersWithMessage(ChatColor.RED+"Sorry this conversation is only for players")
            .withFirstPrompt(new VerifyPrompt())
            .withInitialSessionData(this.sessionData)
            .withLocalEcho(false)
            .withModality(true)
            .withPrefix(new CustomConversationPrefix(String.format(PREFIX, this.getName())))
            .withTimeout(180)
            .buildConversation(player);
        player.beginConversation(con);*/
        String tag = "[Citizen]";
        ClassTag ct = ClassTag.getClassTag(this.clazz);
        if(ct != null)
            tag = ct.getTag();
        player.sendMessage(String.format("%s %s: %s", tag, this.getName(), "Hello Adventurer"));
        final Inventory i = Bukkit.createInventory(player, 27, String.format("%s Trainer", getName()));
        i.setContents(contents);
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.openInventory(i);
            }
        }.runTaskLater(plugin, 20L);
    }
    
    public void moveTo(Location loc, double speed)
    {
        getHandle().getNavigation().a(loc.getX(), loc.getY(), loc.getZ(), speed);
        getHandle().setSpeed(speed);
        getHandle().isNavigating = true;
    }
    
    public Location getSpawnPoint()
    {
        return new Location(getHandle().world.getWorld(), getHandle().sx, getHandle().sy, getHandle().sz);
    }
    
    @Override
    public EntityPlayerNPC getHandle()
    {
        return (EntityPlayerNPC)this.entity;
    }
}
