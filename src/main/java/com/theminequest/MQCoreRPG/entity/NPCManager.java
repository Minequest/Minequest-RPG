package com.theminequest.MQCoreRPG.entity;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.Classes;
import com.theminequest.MQCoreRPG.entity.network.EmptyNetHandler;
import com.theminequest.MQCoreRPG.entity.network.EmptyNetworkManager;
import com.theminequest.MQCoreRPG.entity.network.EmptySocket;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.minecraft.server.v1_6_R2.Connection;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.MinecraftServer;
import net.minecraft.server.v1_6_R2.NetworkManager;
import net.minecraft.server.v1_6_R2.PlayerInteractManager;
import net.minecraft.server.v1_6_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.util.Vector;

public class NPCManager
{
    
    Map<String, NPC> npcs = new HashMap<String, NPC>();

    MineQuestRPG plugin;
    
    public NPCManager(MineQuestRPG plugin)
    {
        this.plugin = plugin;
    }
    
    /**
     * Spawns an NPC with the specified name at the specified location
     * @param name - the name of the Player spawned
     * @param at - the location where the Player will be spawned
     * @return the NPC object associated with the entity
     */
    public NPC spawnNPC(String name, Location at)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        World world = ((CraftWorld) at.getWorld()).getHandle();
        EntityPlayerNPC ep = new EntityPlayerNPC(server, world, name, new PlayerInteractManager(world), at);

        Socket socket = new EmptySocket();
        NetworkManager conn = null;
        try
        {
            conn = new EmptyNetworkManager(server.getLogger(), socket, "npc mgr", new Connection()
            {
                @Override
                public boolean a()
                {
                    return false;
                }
            }, server.H().getPrivate());
            ep.playerConnection = new EmptyNetHandler(server, conn, ep);
            conn.a(ep.playerConnection);
        }
        catch (IOException e)
        {
            // swallow
        }
        // Check the EntityPlayer constructor for the new name.
        try
        {
            socket.close();
        }
        catch (IOException ex)
        {
            // swallow
        }
        NPC npc;
        try
        {
            npc = new NPC(server.server, ep);
            Field bukkitEntity = Entity.class.getDeclaredField("bukkitEntity");
            bukkitEntity.setAccessible(true);
            bukkitEntity.set(ep, npc);
        }
        catch (Exception ex)
        {
            // Something went wrong, stop immediately to prevent any further memory leaks
            return null;
        }

        world.addEntity(ep);
        ep.setLocation(at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
        this.npcs.put(name.toLowerCase(), npc);
        ep.setSpeed(5);
        return ep.getBukkitEntity();
    }
    
    public NPC getNPC(String name)
    {
        String npcName = name.toLowerCase();
        return this.npcs.get(npcName);
    }
    
    public boolean remove(String name)
    {
        String npcName = name.toLowerCase();//.replace('&', ChatColor.COLOR_CHAR);
        NPC npc = this.npcs.remove(npcName);
        if(npc != null)
            npc.remove();
        return npc != null;
    }
    
    public void load()
    {
        File npcFile = new File(this.plugin.getDataFolder(), "npc.dat");
        YamlConfiguration npcCfg = YamlConfiguration.loadConfiguration(npcFile);
        ConfigurationSection npcSection = npcCfg.getConfigurationSection("npcs");
        if(npcSection == null)
            return;
        for(String name : npcSection.getKeys(false))
        {
            ConfigurationSection npc = npcSection.getConfigurationSection(name);
            org.bukkit.World w = Bukkit.getWorld(npc.getString("world", ""));
            if(w == null)
            {
                plugin.getLogger().log(Level.WARNING, String.format("Invalid NPC! An unknown world was found for '%s'\n", name));
                continue;
            }
            Vector pos = npc.getVector("pos", null);
            if(pos == null)
            {
                plugin.getLogger().log(Level.WARNING, String.format("Invalid NPC! An unknown position was found for '%s'\n", name));
                continue;
            }
            float yaw = (float) npc.getDouble("yaw", 0D);
            float pitch = (float) npc.getDouble("pitch", 0D);
            Classes clazz;
            try
            {
                clazz = Classes.valueOf(npc.getString("class", "").toUpperCase());
            }
            catch(IllegalArgumentException ex)
            {
                clazz = Classes.NONE;
            }
            NPC n = spawnNPC(name, pos.toLocation(w, yaw, pitch));
            if(n != null)
                n.setClazz(clazz);
        }
    }
    
    public void save()
    {
        File npcFile = new File(this.plugin.getDataFolder(), "npc.dat");
        if(!npcFile.exists())
        {
            try
            {
                npcFile.getParentFile().mkdirs();
                if(!npcFile.createNewFile())
                    throw new IOException("Failed to create a new file");
            }
            catch(IOException ex)
            {
                plugin.getLogger().log(Level.WARNING, "Could not save NPCs: failed to create npc.dat");
                return;
            }
        }
        YamlConfiguration npcCfg = YamlConfiguration.loadConfiguration(npcFile);
        ConfigurationSection npcSection = npcCfg.getConfigurationSection("npcs");
        if(npcSection == null)
            npcSection = npcCfg.createSection("npcs");
        plugin.getLogger().info(this.npcs.size()+" NPCs found.");
        for(NPC npc : this.npcs.values())
        {
            ConfigurationSection npcData = npcSection.getConfigurationSection(npc.getName());
            if(npcData == null)
                npcData = npcSection.createSection(npc.getName());
            Location loc = npc.getSpawnPoint();
            npcData.set("world", loc.getWorld().getName());
            npcData.set("pos", loc.toVector());
            npcData.set("yaw", (double)loc.getYaw());
            npcData.set("pitch", (double)loc.getPitch());
            Classes clazz = npc.getClazz();
            if(clazz != null)
                npcData.set("class", clazz.name().toLowerCase());
            npc.remove();
        }
        try
        {
            npcCfg.save(npcFile);
        }
        catch(IOException ex)
        {
            plugin.getLogger().severe("Failed to save the NPC data");
        }
        this.npcs.clear();
    }

}
