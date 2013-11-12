package com.theminequest.MQCoreRPG.commands;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.Classes;
import com.theminequest.MQCoreRPG.entity.NPC;
import com.theminequest.MQCoreRPG.managers.MineQuestPlayer;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveclassCommand extends MineQuestRPGCommand
{

    public RemoveclassCommand(MineQuestRPG plugin)
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
        
        Player player = null;
        
        if(args.length < 1 && sender instanceof Player == false)
        {
            sender.sendMessage(Message.NOT_A_PLAYER.toString());
            return true;
        }
        else if(args.length >= 1)
        {
            player = Bukkit.getPlayerExact(args[0]);
            if(player == null)
            {
                List<Player> possible = Bukkit.matchPlayer(args[0]);
                if(possible.isEmpty())
                {
                    sender.sendMessage(Message.PLAYER_NOT_FOUND.toString());
                    return true;
                }
                else if(possible.size() > 1)
                {
                    sender.sendMessage(Message.MULTIPLE_PLAYERS_FOUND.toString());
                }
                player = possible.get(0);
            }
        }
        else
        {
            try
            {
                player = (Player) sender;
            }
            catch(ClassCastException ex)
            {
                // Very likely this is never thrown.
                // I catch it anyway to prevent any errors
                // that might magically occur
            }
        }
        
        if(player == null)
        {
            sender.sendMessage(Message.PLAYER_NOT_FOUND.toString());
            return true;
        }
        
        MineQuestPlayer msp = this.plugin.getPlayerManager().getPlayer(player);
        
        if(msp == null)
        {
            sender.sendMessage(Message.PLAYER_NOT_FOUND.toString());
            return true;
        }
        Classes c = msp.getPlayerClass();
        c.getAbilities().unloadPassiveAbilities(msp);
        msp.setPlayerClass(Classes.NONE);
        String name = c.name().toLowerCase();
        name = name.substring(0,1).toUpperCase()+name.substring(1);
        player.sendMessage(ChatColor.GREEN+"You are no longer a "+name);
        return true;
    }
}
