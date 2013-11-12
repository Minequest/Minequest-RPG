package com.theminequest.MQCoreRPG;

import com.theminequest.MQCoreRPG.combat.CombatTracker;
import com.theminequest.MQCoreRPG.commands.MineQuestRPGCommand;
import com.theminequest.MQCoreRPG.entity.NPCManager;
import com.theminequest.MQCoreRPG.hooks.Hook;
import com.theminequest.MQCoreRPG.hooks.NullHook;
import com.theminequest.MQCoreRPG.listeners.BlockListener;
import com.theminequest.MQCoreRPG.listeners.EntityListener;
import com.theminequest.MQCoreRPG.listeners.InventoryListener;
import com.theminequest.MQCoreRPG.listeners.PlayerListener;
import com.theminequest.MQCoreRPG.managers.PlayerManager;
import com.theminequest.MQCoreRPG.runnables.HealthPackRegenRunnable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class MineQuestRPG extends JavaPlugin
{

    private static MineQuestRPG instance;
    private static Economy eco;
    private PlayerManager pm;
    private NPCManager nm;
    public int MAX_DAMAGE_TICKS = 20;
    public Vector BLOCK_CENTER_OFFSET = new Vector(.5, .5, .5);
    private static Set<String> devs = new HashSet<String>();
    private final String hookClassPath = "com.theminequest.MQCoreRPG.hooks.%sHook";
    private final Map<String, Hook> hooks = new HashMap<String, Hook>();
    private CombatTracker tracker;
    private BukkitTask healthkitTask;
    private final String cmdPath = "com.theminequest.MQCoreRPG.commands.%sCommand";
    
    @Override
    public void onEnable()
    {
        WorldCreator wc = new WorldCreator("spawn");
        World w = wc.createWorld();
        w.setSpawnFlags(false, true);
        w.setPVP(false);
        this.pm = new PlayerManager(this);
        this.nm = new NPCManager(this);
        this.nm.load();
        PluginManager pm = Bukkit.getPluginManager();
        if (!setupEconomy())
        {
            getLogger().log(Level.SEVERE, "Failed to hook into Vault for the economic aspects");
            pm.disablePlugin(this);
            return;
        }
        instance = this;
        pm.registerEvents(new EntityListener(this), this);
        PlayerListener pl = new PlayerListener(this);
        pm.registerEvents(pl, this);
        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new InventoryListener(this, pl), this);
        this.tracker = new CombatTracker();
        String[] hookNames = new String[]
        {
            "WorldGuard"
        };
        for (String hook : hookNames)
        {
            Plugin p = pm.getPlugin(hook);
            Hook h;
            try
            {
                Class c = Class.forName(String.format(this.hookClassPath, hook));
                Constructor ctor = c.getDeclaredConstructor(new Class[]
                {
            		MineQuestRPG.class, Plugin.class
                });
                h = (Hook) ctor.newInstance(this, p);
            }
            catch (IllegalStateException ex)
            {
                h = new NullHook();
                getLogger().severe(ex.getMessage());
            }
            catch (Exception ex)
            {
                h = new NullHook();
                getLogger().log(Level.SEVERE, "Failed to find or instantiate the hook for %s", hook);
            }
            this.hooks.put(hook, h);
        }

        this.healthkitTask = new HealthPackRegenRunnable(this).start();

        Set<String> commands = this.getDescription().getCommands().keySet();
        String t;
        for (String command : commands)
        {
            PluginCommand pc = Bukkit.getPluginCommand(command);
            if (pc == null)
            {
                continue;
            }
            t = command.substring(0, 1).toUpperCase() + command.substring(1);
            try
            {
                Class c = Class.forName(String.format(this.cmdPath, t));
                MineQuestRPGCommand cmd = (MineQuestRPGCommand) c.getDeclaredConstructor(MineQuestRPG.class).newInstance(this);
                if (cmd != null)
                {
                    pc.setExecutor(cmd);
                }
            }
            catch (Exception ex)
            {
                getLogger().log(Level.WARNING, "Failed to initialize command: {0}", command);
            }
        }
    }

    @Override
    public void onDisable()
    {
        this.nm.save();
        this.pm.forceSave();
        if (this.healthkitTask != null && this.healthkitTask.getTaskId() >= 0)
        {
            this.healthkitTask.cancel();
        }
    }

    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
    }

    public PlayerManager getPlayerManager()
    {
        return this.pm;
    }

    public NPCManager getNPCManager()
    {
        return this.nm;
    }

    public static MineQuestRPG getInstance()
    {
        return instance;
    }

    public static Economy getEconomy() throws IllegalStateException
    {
        if (eco == null)
        {
            throw new IllegalStateException("Economy not found. How did you get here!");
        }
        return eco;
    }

    public CombatTracker getCombatTracker()
    {
        return this.tracker;
    }

    public boolean isPermitted(String hookName, Player player, String sub)
    {
        Hook h = this.hooks.get(hookName);
        if (h == null)
        {
            return true;
        }
        return h.isPermitted(player, sub);
    }

    public static void setDev(String name, boolean flag)
    {
        if (flag)
        {
            devs.add(name);
        }
        else
        {
            if (isDev(name))
            {
                devs.remove(name);
            }
        }
    }

    public static boolean isDev(String name)
    {
        return devs.contains(name);
    }

    public void saveAll()
    {
        this.nm.save();
        this.nm.load();
        this.pm.forceSave();
    }
}
