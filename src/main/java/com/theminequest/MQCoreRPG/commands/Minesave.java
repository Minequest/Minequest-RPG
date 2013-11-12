package com.theminequest.MQCoreRPG.commands;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.Classes;
import com.theminequest.MQCoreRPG.entity.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Minesave extends MineQuestRPGCommand
{

    public Minesave(MineQuestRPG plugin)
    {
        super(plugin, "MinequestRPG.dev", false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(!super.onCommand(sender, cmd, label, args))
            return true;
        
        this.plugin.saveAll();
        sender.sendMessage(ChatColor.GREEN + "Everything has been saved :3");
        
        return true;
    }
}
