package com.theminequest.MQCoreRPG.listeners;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.ClassTag;
import com.theminequest.MQCoreRPG.abilities.Classes;
import com.theminequest.MQCoreRPG.abilities.Skill;
import com.theminequest.MQCoreRPG.combat.CombatEntry;
import com.theminequest.MQCoreRPG.events.HealthPackUseEvent;
import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import com.theminequest.MQCoreRPG.managers.PlayerManager;
import com.theminequest.MQCoreRPG.util.EnchantmentVanillaNames;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.P;
import java.util.Iterator;
import java.util.Map;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener
{

    private final MineQuestRPG plugin;
    private final PlayerManager pm;
    Map<String, ClassTag> clazztags = new ConcurrentHashMap<String, ClassTag>();
    private final boolean chatTagRelationColored;

    public PlayerListener(MineQuestRPG plugin)
    {
        this.plugin = plugin;
        this.pm = plugin.getPlayerManager();
        this.chatTagRelationColored = Conf.chatTagRelationColored;
        Conf.chatTagRelationColored = false;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event)
    {
        pm.onPlayerJoin(event.getPlayer());
        ClassTag tag = ClassTag.getClassTag(pm.getPlayer(event.getPlayer()).getPlayerClass());
        this.clazztags.put(event.getPlayer().getName(), tag);
        if(tag == ClassTag._NONE && !event.getPlayer().isOp())
        {
            new BukkitRunnable() 
            {
                public void run()
                {
                    World w = Bukkit.getWorld("MineQuestRPG_spawn");
                    event.getPlayer().teleport(w.getSpawnLocation().add(0.5, 0, 0.5));
                }
            }.runTaskLater(plugin, 1L);
        }
        //event.getPlayer().setMaximumNoDamageTicks(0);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        pm.onPlayerQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSkill(PlayerInteractEvent event)
    {
        Action a = event.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }

        Player player = event.getPlayer();

        ItemStack i = player.getItemInHand();
        if (!Skill.isSkillBook(i))
        {
            return;
        }

        if (i.getDurability() == 2 && i.getType().name().toLowerCase().contains("apple"))
        {
            if(i.getType() != Material.GOLDEN_APPLE)
                return;
            event.setCancelled(true);
            boolean fire = false;
            if (i.getAmount() > 1)
            {
                if(i.getAmount() >= 10)
                    fire = true;
                i.setAmount(event.getItem().getAmount() - 1);
            }
            else
            {
                i.setAmount(1);
                i.setType(Material.APPLE);
            }
            double newHealth = player.getPlayer().getHealth() + 8;
            player.setHealth(Math.min(newHealth, player.getMaxHealth()));
            if(fire)
                this.plugin.getServer().getPluginManager().callEvent(new HealthPackUseEvent(player));
            event.setCancelled(true);
            return;
        }

        if (!this.plugin.isPermitted("WorldGuard", player, "no skill"))
        {
            return;
        }

        String title = i.getItemMeta().getDisplayName();
        title = ChatColor.stripColor(title).replace(' ', '_').toUpperCase();
        Skill skill;
        try
        {
            skill = Skill.valueOf(title);
        }
        catch (IllegalArgumentException ex)
        {
            skill = Skill.NONE;
        }

        MineQuestRPGPlayer msp = this.pm.getPlayer(player);
        if (msp == null)
        {
            return;
        }

        switch (skill.getSlot())
        {
            case 1:
                msp.getPlayerClass().getAbilities().slot1(msp);
                break;
            case 2:
                msp.getPlayerClass().getAbilities().slot2(msp);
                break;
            case 3:
                msp.getPlayerClass().getAbilities().slot3(msp);
                break;
            case 4:
                msp.getPlayerClass().getAbilities().slot4(msp);
                break;
        }
        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSignUse(final PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }
        if (event.getClickedBlock().getState() instanceof Sign == false)
        {
            return;
        }

        Sign sign = (Sign) event.getClickedBlock().getState();
        String header = ChatColor.stripColor(sign.getLine(0));
        if (!header.equalsIgnoreCase("[unenchant]"))
        {
            return;
        }
        Player player = event.getPlayer();

        Enchantment e = EnchantmentVanillaNames.getEnchantment(sign.getLine(1));
        if (e == null)
        {
            player.sendMessage(ChatColor.RED + "Unknown enchantment");
            return;
        }
        if (sign.getLine(2).matches("$[0-9]*\\.[0-9]{0,2}"))
        {
            player.sendMessage("Invalid price");
            return;
        }
        double price = Double.parseDouble(sign.getLine(2).substring(1));
        ItemStack hand = player.getItemInHand();
        if (hand == null || !hand.containsEnchantment(e))
        {
            player.sendMessage(ChatColor.RED + "That item does not hold the enchantment, thus the enchantment cannot be removed");
            return;
        }
        try
        {
            EconomyResponse er = MineQuestRPG.getEconomy().withdrawPlayer(player.getName(), price);
            if (!er.transactionSuccess())
            {
                player.sendMessage(ChatColor.RED + "You don't have enough money.");
                return;
            }
        }
        catch (IllegalArgumentException ex)
        {
            player.sendMessage(ChatColor.RED + "Something went wrong... inform an administrator!");
            return;
        }
        hand.removeEnchantment(e);
        player.sendMessage(ChatColor.GREEN + "The enchantment has been removed from the held item");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        Iterator<ItemStack> it = event.getDrops().iterator();
        ItemStack i;
        while (it.hasNext())
        {
            i = it.next();
            if (Skill.isSkillBook(i))
            {
                it.remove();
            }
        }

        Player player = event.getEntity();
        CombatEntry ce = this.plugin.getCombatTracker().retrieve(player);
        if (ce == null)
        {
            return;
        }
        EntityDamageEvent e = player.getLastDamageCause();

        String newMessage = null;
        switch (ce.getSkill())
        {
            case SHOCKWAVE:
                newMessage = String.format("&e%s&5 got slain by &e%s&5's &bshockwave", player.getName(), ce.getAttacker());
                break;
            case REND:
                newMessage = String.format("&e%s&5 succumbed to &e%s&5's &brend", player.getName(), ce.getAttacker());
                break;
            case BESERKER:
                break;
            case LAST_STAND:
                break;
            case CALL_LIGHTNING:
                newMessage = String.format("&e%s&5 got struck by &e%s&5's &blightning", player.getName(), ce.getAttacker());
                break;
            case ARCANE_SHIELD:
                break;
            case FROST_NOVA:
                break;
            case PYROBLAST:
                newMessage = String.format("&e%s&5 was burned to crisp by &e%s&5's &bpyroblast", player.getName(), ce.getAttacker());
                break;
            case VANISH:
                break;
            case ENVENOM:
                break;
            case KIDNEY_SHOT:
                newMessage = String.format("&e%s&5 died to &e%s&5's &bkidney shot", player.getName(), ce.getAttacker());
                break;
            case KILLING_SPREE:
                newMessage = String.format("&e%s&5 was assassinated by &e%s&5", player.getName(), ce.getAttacker());
                break;
            case EXPLOSIVE_GRENADE:
                newMessage = String.format("&e%s&5 was blown to bits by &e%s&5's &bexplosive grenade", player.getName(), ce.getAttacker());
                break;
            case SHARP_SHOOTER:
                newMessage = String.format("&e%s&5 was sniped by &e%s&5", player.getName(), ce.getAttacker());
                break;
            case ICE_TRAP:
                break;
            case MASTER_OF_THE_WILD:
                newMessage = String.format("&e%s&5 was torn apart by the wolves under &e%s&5's command", player.getName(), ce.getAttacker());
                break;
            case CURSE:
                newMessage = String.format("&e%s&5 succumbed to &e%s&5's &bcurse", player.getName(), ce.getAttacker());
                break;
            case CORRUPTION:
                newMessage = String.format("&e%s&5 died due &e%s&5's &bcorruption", player.getName(), ce.getAttacker());
                break;
            case SHADOW_BOLT:
                newMessage = String.format("&e%s&5 was vaporized by &e%s&5's &bshadow bolt", player.getName(), ce.getAttacker());
                break;
            case HEALING_STREAM_TOTEM:
                break;
            case EARTH_GRAB_TOTEM:
                break;
            case MAGMA_TOTEM:
                newMessage = String.format("&e%s&5 was charred due &e%s&5's &bmagma totem", player.getName(), ce.getAttacker());
                break;
            case RIPTIDE:
                break;
        }
        if (newMessage != null)
        {
            event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', newMessage));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event)
    {
        Player player = event.getPlayer();
        final MineQuestRPGPlayer msp = this.pm.getPlayer(player);
        if (msp == null)
        {
            return;
        }

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                msp.getPlayerClass().getAbilities().loadPassiveAbilities(msp);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onConsume(PlayerItemConsumeEvent event)
    {
        if (event.getItem() == null)
        {
            return;
        }
        final MineQuestRPGPlayer player = this.pm.getPlayer(event.getPlayer());
        if (player == null)
        {
            return;
        }
        Material type = event.getItem().getType();
        switch (type)
        {
            case MILK_BUCKET:
                if (player.getPlayerClass() == Classes.RANGER)
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                        }
                    }.runTaskLater(plugin, 1L);
                }
                break;
            case POTION:
                if (player.getPlayerClass() == Classes.RANGER)
                {
                    new BukkitRunnable()
                    {
                        @Override
                        public void run()
                        {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                        }
                    }.runTaskLater(plugin, 1L);
                }
            case GOLDEN_APPLE:
                ItemStack ga = event.getItem();
                if (ga.getDurability() == 2)
                {
                    event.setCancelled(true);
                }
        }
    }

    @EventHandler
    public void onSplash(PotionSplashEvent event)
    {
        for (LivingEntity le : event.getAffectedEntities())
        {
            if (le instanceof Player)
            {
                final MineQuestRPGPlayer player = this.pm.getPlayer((Player) le);
                if (player != null)
                {
                    if (player.getPlayerClass() == Classes.RANGER)
                    {
                        new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
                            }
                        }.runTaskLater(plugin, 1L);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event)
    {
        if (event.getCause() != TeleportCause.NETHER_PORTAL)
        {
            return;
        }
        
        if(!event.getFrom().getWorld().getName().toLowerCase().contains("MineQuestRPG"))
            return;
        
        event.setCancelled(true);
        MineQuestRPGPlayer player = this.pm.getPlayer(event.getPlayer());
        if (player == null)
        {
            return;
        }
        if (player.getPlayerClass() != Classes.NONE)
        {
            // Should teleport them to the spawn of the default world
            player.getPlayer().playSound(event.getTo(), Sound.PORTAL_TRAVEL, 1, 1);
            player.getPlayer().teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event)
    {
        Player talkingPlayer = event.getPlayer();
        String name = talkingPlayer.getName();
        ClassTag tag = this.clazztags.get(name);
        if (tag == null)
        {
            return;
        }
        String msg = event.getMessage();
        if (chatTagRelationColored)
        {
            event.setCancelled(true);
            FPlayer me = FPlayers.i.get(talkingPlayer);
            int InsertIndex = 0;
            String eventFormat = event.getFormat();
            if ((!Conf.chatTagReplaceString.isEmpty()) && (eventFormat.contains(Conf.chatTagReplaceString)))
            {
                if (eventFormat.contains("[FACTION_TITLE]"))
                {
                    eventFormat = eventFormat.replace("[FACTION_TITLE]", me.getTitle());
                }
                InsertIndex = eventFormat.indexOf(Conf.chatTagReplaceString);
                eventFormat = eventFormat.replace(Conf.chatTagReplaceString, "");
                Conf.chatTagPadAfter = false;
                Conf.chatTagPadBefore = false;
            }
            else
            {
                if ((!Conf.chatTagInsertAfterString.isEmpty()) && (eventFormat.contains(Conf.chatTagInsertAfterString)))
                {
                    InsertIndex = eventFormat.indexOf(Conf.chatTagInsertAfterString) + Conf.chatTagInsertAfterString.length();
                }
                else
                {
                    if ((!Conf.chatTagInsertBeforeString.isEmpty()) && (eventFormat.contains(Conf.chatTagInsertBeforeString)))
                    {
                        InsertIndex = eventFormat.indexOf(Conf.chatTagInsertBeforeString);
                    }
                    else
                    {
                        InsertIndex = Conf.chatTagInsertIndex;
                        if (InsertIndex > eventFormat.length())
                        {
                            return;
                        }
                    }
                }
            }
            String formatStart = tag.getTag()+" "+eventFormat.substring(0, InsertIndex) + ((Conf.chatTagPadBefore) && (!me.getChatTag().isEmpty()) ? " " : "");
            String formatEnd = ((Conf.chatTagPadAfter) && (!me.getChatTag().isEmpty()) ? " " : "") + eventFormat.substring(InsertIndex+me.getChatTag().length()+(!me.getChatTag().isEmpty() ? 1 : 0));
            String nonColoredMsgFormat = formatStart + me.getChatTag().trim() + formatEnd;
            for (Player listeningPlayer : event.getRecipients())
            {
                FPlayer you = (FPlayer) FPlayers.i.get(listeningPlayer);
                String ftag = me.getChatTag(you).trim();
                String yourFormat =  formatStart + ftag + formatEnd;
                try
                {
                    listeningPlayer.sendMessage(String.format(yourFormat, new Object[]
                    {
                        talkingPlayer.getDisplayName(), msg
                    }));
                }
                catch (UnknownFormatConversionException ex)
                {
                    Conf.chatTagInsertIndex = 0;
                    P.p.log(Level.SEVERE, "Critical error in chat message formatting!");
                    P.p.log(Level.SEVERE, "NOTE: This has been automatically fixed right now by setting chatTagInsertIndex to 0.");
                    P.p.log(Level.SEVERE, "For a more proper fix, please read this regarding chat configuration: http://massivecraft.com/plugins/factions/config#Chat_configuration");
                    return;
                }

            }

            String nonColoredMsg = ChatColor.stripColor(String.format(nonColoredMsgFormat, new Object[]
            {
                talkingPlayer.getDisplayName(), msg
            }));
            Bukkit.getLogger().log(Level.INFO, nonColoredMsg);
        }
        else
            event.setFormat(tag.getTag() +" "+ event.getFormat());
    }
    
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        if(!player.getWorld().getName().toLowerCase().contains("MineQuestRPG"))
            return;
        if((event.getMessage().startsWith("/setspawn") || event.getMessage().startsWith("/esetspawn")) && player.isOp())
        {
            Block b = player.getLocation().getBlock();
            player.getWorld().setSpawnLocation(b.getX(), b.getY(), b.getZ());
            player.sendMessage(ChatColor.GOLD+"Set the spawnpoint for the MineQuestRPG world");
            event.setCancelled(true);
            return;
        }
        if(player.isOp())
            return;
        event.setCancelled(true);
        player.sendMessage(ChatColor.DARK_RED+"You are not allowed to use commands in this area");
    }
}
