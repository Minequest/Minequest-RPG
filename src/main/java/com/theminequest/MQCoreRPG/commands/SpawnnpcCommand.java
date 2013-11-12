package com.theminequest.MQCoreRPG.commands;

import com.theminequest.MQCoreRPG.MineQuestRPG;
import com.theminequest.MQCoreRPG.abilities.Classes;
import com.theminequest.MQCoreRPG.entity.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnnpcCommand extends MineQuestRPGCommand
{

    public SpawnnpcCommand(MineQuestRPG plugin)
    {
        super(plugin, "MineQuestRPG.spawn", true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if(!super.onCommand(sender, cmd, label, args))
            return true;
        
        if(args[0].equalsIgnoreCase("warrior"))
        {
            NPC npc = this.plugin.getNPCManager().spawnNPC("Warrior", ((Player)sender).getLocation());
            npc.setClazz(Classes.WARRIOR);
        }
        else if(args[0].equalsIgnoreCase("mage"))
        {
            NPC npc = this.plugin.getNPCManager().spawnNPC("Mage", ((Player)sender).getLocation());
            npc.setClazz(Classes.MAGE);
        }
        else if(args[0].equalsIgnoreCase("assassin"))
        {
            NPC npc = this.plugin.getNPCManager().spawnNPC("Assassin", ((Player)sender).getLocation());
            npc.setClazz(Classes.ASSASSIN);
        }
        else if(args[0].equalsIgnoreCase("ranger"))
        {
            NPC npc = this.plugin.getNPCManager().spawnNPC("Ranger", ((Player)sender).getLocation());
            npc.setClazz(Classes.RANGER);
        }
        else if(args[0].equalsIgnoreCase("necromancer"))
        {
            NPC npc = this.plugin.getNPCManager().spawnNPC("Necromancer", ((Player)sender).getLocation());
            npc.setClazz(Classes.NECROMANCER);
        }
        else if(args[0].equalsIgnoreCase("shaman"))
        {
            NPC npc = this.plugin.getNPCManager().spawnNPC("Shaman", ((Player)sender).getLocation());
            npc.setClazz(Classes.SHAMAN);
        }
        
        return true;
    }
}
