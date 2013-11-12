package com.theminequest.MQCoreRPG.commands;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.omg.CORBA.NO_PERMISSION;

public abstract class MineQuestRPGCommand implements CommandExecutor
{
    protected enum Message
    {
        NO_PERMISSION("&cYou don't have the permission to use this command."),
        NOT_A_PLAYER("&cYou need to be an ingame player to execute this command."),
        PLAYER_NOT_FOUND("&cPlayer not found!"),
        MULTIPLE_PLAYERS_FOUND("&cMultiple players found! Please specify the exact player.");
        
        private final String message;
        
        Message(String msg)
        {
            this.message = msg.replace('&', ChatColor.COLOR_CHAR);
        }
        
        @Override
        public String toString()
        {
            return this.message;
        }
    }
    
    private final String perm;
    
    private final boolean playerOnly;
    
    protected final MineQuestRPG plugin;
    
    protected MineQuestRPGCommand(MineQuestRPG plugin, String perm, boolean playerOnly)
    {
        this.plugin = plugin;
        this.perm = perm;
        this.playerOnly = playerOnly;
    }
    
    public boolean canUseCommand(CommandSender sender)
    {
        if(sender instanceof Player || !playerOnly)
            return sender.hasPermission(perm);
        return false;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings)
    {
        if(!canUseCommand(cs))
        {
            if(cs instanceof Player)
                cs.sendMessage(Message.NO_PERMISSION.toString());
            else
                cs.sendMessage(Message.NOT_A_PLAYER.toString());
            return false;
        }
        return true;
    }
}
