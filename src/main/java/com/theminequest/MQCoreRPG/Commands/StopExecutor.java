package com.theminequest.MQCoreRPG.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StopExecutor implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2,
			String[] arg3) {
		for (Player p : Bukkit.getOnlinePlayers())
			p.kickPlayer("The server is shutting down!");
		Bukkit.shutdown();
		return true;
	}

}
