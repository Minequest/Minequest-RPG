package com.theminequest.MQCoreRPG.managers;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.Classes;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerManager 
{
    
    private final MineQuestRPG plugin;
    
    private final Map<String, MineQuestPlayer> players = new HashMap<String, MineQuestPlayer>();
    
    private final YamlConfiguration playerCfg;
    
    public PlayerManager(MineQuestRPG plugin)
    {
        this.plugin = plugin;
        File playerDat = new File(plugin.getDataFolder(), "players.dat");
        if(!playerDat.exists())
        {
            try
            {
                playerDat.getParentFile().mkdirs();
                if(!playerDat.createNewFile())
                {
                    throw new IOException("Failed to create players.dat");
                }
            }
            catch(IOException ex)
            {
                plugin.getLogger().severe("Failed to create players.dat");
            }
        }
        this.playerCfg = YamlConfiguration.loadConfiguration(playerDat);
        if(!this.playerCfg.isConfigurationSection("players"))
            this.playerCfg.createSection("players");
        
        new BukkitRunnable()
        {
            final PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0);
            final PotionEffect regen = new PotionEffect(PotionEffectType.REGENERATION, 80, 0);
            
            int shamantick = 0;
            
            @Override
            public void run()
            {
                for(MineQuestPlayer msp : PlayerManager.this.players.values())
                {
                    if(msp.isRanger() && !msp.getPlayer().hasPotionEffect(PotionEffectType.SPEED))
                        msp.addPotionEffect(speed);
                    if(msp.isShaman() && shamantick == 5)
                    {
                        msp.addPotionEffect(regen);
                    }
                }
                shamantick++;
                shamantick %= 6;
            }
        }.runTaskTimer(plugin, 600L, 20L);
    }
    
    public void onPlayerJoin(Player player)
    {
        MineQuestPlayer msp = new MineQuestPlayer(player, plugin);
        String section = String.format("players.%s", player.getName());
        if(this.playerCfg.isConfigurationSection(section))
            msp.load(this.playerCfg.getConfigurationSection(section));
        this.players.put(player.getName(), msp);
    }
    
    /**
     * Note: make sure to cleanup all references to MineQuestPlayers on PlayerQuitEvent
     * @param player - the Player
     * @return the MineQuestPlayer object of the player
     */
    public MineQuestPlayer getPlayer(Player player)
    {
        return this.players.get(player.getName());
    }
    
    /**
     * Note: make sure to cleanup all references to MineQuestPlayers on PlayerQuitEvent
     * @return the Collection of all MineQuestPlayer objects
     */
    public Collection<MineQuestPlayer> getPlayers()
    {
        return this.players.values();
    }
    
    public void onPlayerQuit(Player player)
    {
        MineQuestPlayer msp = this.players.remove(player.getName());
        savePlayer(msp);
    }
    
    public void savePlayer(Player player)
    {
        savePlayer(this.players.get(player.getName()));
    }
    
    public void savePlayer(MineQuestPlayer player)
    {
        if(player != null)
        {
            String secPath = String.format("players.%s", player.getName());
            ConfigurationSection section = this.playerCfg.createSection(secPath);
            if(section == null)
                section = this.playerCfg.createSection(secPath);
            player.save(section);
        }
    }
    
    public void forceSave()
    {
        for(Player player : Bukkit.getOnlinePlayers())
            savePlayer(player);
        save();
    }
    
    public void save()
    {
        File playerFile = new File(plugin.getDataFolder(), "players.dat");
        try
        {
            this.playerCfg.save(playerFile);
        }
        catch(IOException ex)
        {
            this.plugin.getLogger().severe("Failed to save players.dat");
        }
    }

}
