package com.theminequest.MQCoreRPG.API.Abilities;

import java.util.List;

import com.theminequest.MineQuest.API.Quest.QuestDetails;
import com.theminequest.MineQuest.API.Quest.QuestParser;

public class AbilityHandler implements QuestParser.QHandler {

	/*
	 * (non-Javadoc)
	 * @see com.theminequest.MineQuest.Quest.QuestParser.QHandler#parseDetails(com.theminequest.MineQuest.Quest.Quest, java.util.List)
	 * "ability,ability,ability,ability"
	 */
	@Override
	public void parseDetails(QuestDetails q, List<String> line) {
		q.setProperty(Ability.DETAILS_KEY, line.get(0));
	}

}
