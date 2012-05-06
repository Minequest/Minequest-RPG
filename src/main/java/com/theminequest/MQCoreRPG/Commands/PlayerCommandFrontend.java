package com.theminequest.MQCoreRPG.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.theminequest.MQCoreRPG.MQCoreRPG;
import com.theminequest.MQCoreRPG.Player.PlayerDetails;
import com.theminequest.MQCoreRPG.Player.PlayerManager;
import com.theminequest.MineQuest.MineQuest;
import com.theminequest.MineQuest.Frontend.Command.CommandFrontend;
import com.theminequest.MineQuest.Utils.ChatUtils;

public class PlayerCommandFrontend extends CommandFrontend {

	public PlayerCommandFrontend() {
		super("player");
	}

	// TODO Implement localization fully.

	public Boolean clear(Player p, String[] args){
		if (p!=null){
			if (!p.isOp())
				return false;
		}

		CommandSender c;
		if (p==null)
			c = Bukkit.getConsoleSender();
		else
			c = p;

		if (args.length!=1){
			c.sendMessage("Invalid number of arguments.");
			return false;
		}

		Player player = Bukkit.getPlayerExact(args[0]);
		if (player==null){
			c.sendMessage("No such player. :(");
			return false;
		}

		PlayerDetails details = MQCoreRPG.playerManager.getPlayerDetails(player);
		details.modifyExperienceBy((int) -(details.getExperience()));
		details.setLevel(1);
		c.sendMessage("Cleared.");
		return true;
	}

	public Boolean setlevel(Player p, String[] args){
		if (p!=null){
			if (!p.isOp())
				return false;
		}

		CommandSender c;
		if (p==null)
			c = Bukkit.getConsoleSender();
		else
			c = p;

		if (args.length!=2){
			c.sendMessage("Invalid number of arguments.");
			return false;
		}

		Player player = Bukkit.getPlayerExact(args[0]);
		if (player==null){
			c.sendMessage("No such player. :(");
			return false;
		}

		PlayerDetails details = MQCoreRPG.playerManager.getPlayerDetails(player);
		details.setLevel(Integer.parseInt(args[1]));
		c.sendMessage("Modified.");
		return true;
	}

	public Boolean setclass(Player p, String[] args){
		if (p!=null){
			if (!p.isOp())
				return false;
		}

		CommandSender c;
		if (p==null)
			c = Bukkit.getConsoleSender();
		else
			c = p;

		if (args.length!=2){
			c.sendMessage("Invalid number of arguments.");
			return false;
		}

		Player player = Bukkit.getPlayerExact(args[0]);
		if (player==null){
			c.sendMessage("No such player. :(");
			return false;
		}

		PlayerDetails details = MQCoreRPG.playerManager.getPlayerDetails(player);
		details.setClassID(args[1]);
		c.sendMessage("Modified.");
		return true;
	}

	public Boolean giveexp(Player p, String[] args){
		if (p!=null){
			if (!p.isOp())
				return false;
		}

		CommandSender c;
		if (p==null)
			c = Bukkit.getConsoleSender();
		else
			c = p;

		if (args.length!=2){
			c.sendMessage("Invalid number of arguments.");
			return false;
		}

		Player player = Bukkit.getPlayerExact(args[0]);
		if (player==null){
			c.sendMessage("No such player. :(");
			return false;
		}

		PlayerDetails details = MQCoreRPG.playerManager.getPlayerDetails(player);
		details.modifyExperienceBy((int)Double.parseDouble(args[1]));
		c.sendMessage("Modified.");
		return true;
	}

	public Boolean levelup(Player p, String[] args){
		if (p!=null){
			if (!p.isOp())
				return false;
		}

		CommandSender c;
		if (p==null)
			c = Bukkit.getConsoleSender();
		else
			c = p;

		if (args.length!=1){
			c.sendMessage("Invalid number of arguments.");
			return false;
		}

		Player player = Bukkit.getPlayerExact(args[0]);
		if (player==null){
			c.sendMessage("No such player. :(");
			return false;
		}

		PlayerDetails details = MQCoreRPG.playerManager.getPlayerDetails(player);
		details.levelUp();
		c.sendMessage("Leveled.");
		return true;
	}

	public Boolean heal(Player p, String[] args){
		if (p!=null){
			if (!p.isOp())
				return false;
		}

		CommandSender c;
		if (p==null)
			c = Bukkit.getConsoleSender();
		else
			c = p;

		if (args.length>1){
			c.sendMessage("Invalid number of arguments.");
			return false;
		}

		Player player;
		if (args.length==1)
			player = Bukkit.getPlayerExact(args[0]);
		else
			player = p;
		if (player==null){
			c.sendMessage("No such player. :(");
			return false;
		}

		PlayerDetails details = MQCoreRPG.playerManager.getPlayerDetails(player);
		details.setHealth(details.getMaxHealth());
		c.sendMessage("Healed.");
		return true;

	}

	public Boolean info(Player p, String[] args) {
		CommandSender c;
		if (p==null)
			c = Bukkit.getConsoleSender();
		else
			c = p;

		if (args.length>1){
			c.sendMessage("Invalid number of arguments.");
			return false;
		}

		Player lookup;

		if (args.length==0)
			lookup = p;
		else
			lookup = Bukkit.getPlayerExact(args[0]);

		if (lookup==null){
			c.sendMessage("No such player.");
			return false;
		}

		PlayerDetails details = MQCoreRPG.playerManager.getPlayerDetails(lookup);

		List<String> messages = new ArrayList<String>();
		messages.add(ChatUtils.formatHeader("Player Information: " + lookup.getName()));
		messages.add(ChatColor.AQUA + "Display Name: " + ChatColor.WHITE + lookup.getDisplayName());
		messages.add(ChatColor.AQUA + "On Team: " + ChatColor.WHITE + (MineQuest.groupManager.indexOf(lookup)!=-1));
		messages.add(ChatColor.AQUA + "Health: " + ChatColor.WHITE + details.getHealth() + "/" + (details.getMaxHealth()));
		messages.add(ChatColor.AQUA + "Level: " + ChatColor.WHITE + details.getLevel());
		if (p.equals(lookup) || !(c instanceof Player)){
			messages.add(ChatColor.AQUA + "Exp: " + ChatColor.WHITE + details.getExperience() + "/" + (details.getMaxExperience()));
			messages.add(ChatColor.AQUA + "Power: " + ChatColor.WHITE + details.getPower() + "/" + (details.getMaxPower()));
		}

		for (String s: messages){
			c.sendMessage(s);
		}
		return true;
	}

	@Override
	public Boolean help(Player p, String[] args) {
		List<String> messages = new ArrayList<String>();

		// CONSOLE COMMANDS
		if (p==null || p.isOp()){
			messages.add(ChatUtils.formatHeader("Op Commands"));
			messages.add(ChatUtils.formatHelp("player clear [name]","Clear a user's statistics completely."));
			messages.add(ChatUtils.formatHelp("player giveexp [name] [amt]", "Give a player EXP."));
			messages.add(ChatUtils.formatHelp("player heal [name]", "Heal a player."));
			messages.add(ChatUtils.formatHelp("player levelup [name]", "Level up a player by 1."));
			messages.add(ChatUtils.formatHelp("player setlevel [name] [lvl]", "Set the level of a player."));
			messages.add(ChatUtils.formatHelp("player setclass [name] [class]", "Set the class of a player."));
		}
		messages.add(ChatUtils.formatHeader("Player Commands"));
		messages.add(ChatUtils.formatHelp("player info", "Retrieve your information."));
		messages.add(ChatUtils.formatHelp("player info <name>", "Retrieve Player Information."));		
		if (p==null){
			CommandSender c = Bukkit.getConsoleSender();
			for (String s : messages){
				c.sendMessage(s);
			}
		} else {
			for (String s : messages){
				p.sendMessage(s);
			}
		}
		return true;
	}

	@Override
	public boolean allowConsole() {
		return true;
	}

}
