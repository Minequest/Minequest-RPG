package com.theminequest.MQCoreRPG.QEvents;

import org.bukkit.entity.Player;

import com.theminequest.MQCoreRPG.MQCoreRPG;
import com.theminequest.MQCoreRPG.Player.PlayerDetails;
import com.theminequest.MineQuest.MineQuest;
import com.theminequest.MineQuest.BukkitEvents.CompleteStatus;
import com.theminequest.MineQuest.EventsAPI.QEvent;
import com.theminequest.MineQuest.Group.Group;

public class AssignClassEvent extends QEvent {
	
	private String classname;

	public AssignClassEvent(long q, int e, String details) {
		super(q, e, details);
	}

	/*
	 * (non-Javadoc)
	 * @see com.theminequest.MineQuest.EventsAPI.QEvent#parseDetails(java.lang.String[])
	 * [0] Class Identifier (name)
	 */
	@Override
	public void parseDetails(String[] details) {
		classname = details[0];
	}

	@Override
	public boolean conditions() {
		return true;
	}

	@Override
	public CompleteStatus action() {
		Group g = MineQuest.groupManager.getGroup(MineQuest.groupManager.indexOfQuest(MineQuest.questManager.getQuest(getQuestId())));
		for (Player p : g.getPlayers()){
			PlayerDetails d = MQCoreRPG.playerManager.getPlayerDetails(p);
			d.setClassID(classname);
		}
		return CompleteStatus.SUCCESS;
	}

	@Override
	public Integer switchTask() {
		return null;
	}

}
