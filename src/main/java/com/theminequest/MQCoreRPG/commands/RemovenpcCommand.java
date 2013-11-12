package com.theminequest.MQCoreRPG.commands;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.Classes;
import com.theminequest.MQCoreRPG.entity.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemovenpcCommand extends MineQuestRPGCommand
{

    public RemovenpcCommand(MineQuestRPG plugin)
    {
        super(plugin, "MineQuestRPG.spawn", false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!super.onCommand(sender, cmd, label, args))
        {
            return true;
        }

        if (args.length < 2)
        {
            sender.sendMessage(ChatColor.RED + "Please specify the name of the NPC to be removed.");
            return true;
        }
        if (this.plugin.getNPCManager().remove(args[1]))
        {
            sender.sendMessage(ChatColor.GREEN + "NPC removed.");
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "NPC not found.");
        }

        return true;
    }
}
