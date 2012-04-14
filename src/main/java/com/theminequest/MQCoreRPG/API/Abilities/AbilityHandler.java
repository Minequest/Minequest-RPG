package com.theminequest.MQCoreRPG.API.Abilities;

import java.util.List;

import com.theminequest.MineQuest.Quest.Quest;
import com.theminequest.MineQuest.Quest.QuestParser;

public class AbilityHandler implements QuestParser.QHandler {

	/*
	 * (non-Javadoc)
	 * @see com.theminequest.MineQuest.Quest.QuestParser.QHandler#parseDetails(com.theminequest.MineQuest.Quest.Quest, java.util.List)
	 * "ability,ability,ability,ability"
	 */
	@Override
	public void parseDetails(Quest q, List<String> line) {
		q.database.put("bannedabilities", line.get(0));
	}

}
