package com.theminequest.MQCoreRPG.commands;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.Classes;
import com.theminequest.MQCoreRPG.entity.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DevmodeCommand extends MineQuestRPGCommand
{

    public DevmodeCommand(MineQuestRPG plugin)
    {
        super(plugin, "MineQuestRPG.dev", true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(!super.onCommand(sender, cmd, label, args))
            return true;
        
        boolean id = MineQuestRPG.isDev(sender.getName());
        
        if(!id)
        { 
        	MineQuestRPG.setDev(sender.getName(), true);
        }
        else
        	MineQuestRPG.setDev(sender.getName(), false);
        sender.sendMessage("Developer mode "+(!id ? ChatColor.GREEN+"enabled" : ChatColor.RED+"disabled"));
        
        return true;
    }
}
