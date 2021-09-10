/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.lge;

import net.sf.l2j.gameserver.lge.LgeEvent.EventType;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author Домашний
 */
public class LgeEventRewardsTask implements Runnable
{
	
	private final L2PcInstance playerInstance;
	private final LgeEventTeam team;
	private final boolean win;
	private final EventType eventType;
	private final LgeEvent tvTEvent;
	
	/**
	 * @param _playerInstance
	 * @param _team
	 * @param _win
	 * @param _eventType
	 * @param _tvTEvent
	 */
	public LgeEventRewardsTask(L2PcInstance _playerInstance, LgeEventTeam _team, boolean _win, EventType _eventType, LgeEvent _tvTEvent)
	{
		playerInstance = _playerInstance;
		team = _team;
		win = _win;
		eventType = _eventType;
		tvTEvent = _tvTEvent;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		if (playerInstance == null)
		{
			return;
		}
		
		if (win)
		{
			playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_TEAM_WIN));
		}
		else
		{
			playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_TEAM_DEFEAT));
		}
		
		// message of winning
		short points = (short) Math.max(team.getPoints(), 1);
		int participatedPlayerCount = Math.max(team.getParticipatedPlayerCount(), 1);
		int boxItemId = 3440 + Math.max(playerInstance.getSkillLevel(239) - 2, 0);
		if (win)
		{
			// exp 100 * level * point
			playerInstance.addExpAndSp((playerInstance.getLevel() * 2000) * points, (playerInstance.getLevel() * 500) * points);
			
			// give Treasure`s Box [League Box]
			playerInstance.addItem("LOL", boxItemId, Math.max(points / participatedPlayerCount, 1), null, true);
			
			int playerPoinst = tvTEvent.getPlayerPoints(tvTEvent, playerInstance.getName());
			if (playerPoinst > 0)
			{
				playerInstance.addItem("LOL", 4037, playerPoinst * 10, null, true);
				playerInstance.incLgePoints(playerPoinst, true);
			}
			
			// clan to clan
			// give reputation
			if (eventType == EventType.CLAN_TO_CLAN)
			{
				L2Clan clan = playerInstance.getClan();
				if (clan != null)
				{
					clan.setReputationScore(clan.getReputationScore() + (points * 10), true);
					playerInstance.sendPacket(new PledgeShowInfoUpdate(clan));
					SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_ADDED_S1S_POINTS_TO_REPUTATION_SCORE);
					sm.addNumber((points * 10));
					playerInstance.sendPacket(sm);
				}
			}
		}
		else
		{
			// exp 50 * level * point
			playerInstance.addExpAndSp((playerInstance.getLevel() * 500) * points, (playerInstance.getLevel() * 250) * points);
			
			// give 3443 Treasure`s Box [League Box]
			playerInstance.addItem("LOL", boxItemId, Math.max(points / participatedPlayerCount, 1), null, true);
			
			int playerPoinst = tvTEvent.getPlayerPoints(tvTEvent, playerInstance.getName());
			if (playerPoinst > 0)
			{
				playerInstance.addItem("LOL", 4037, playerPoinst, null, true);
				playerInstance.incLgePoints(playerPoinst, true);
			}
			
			// clan to clan
			// give reputation
			if (eventType == EventType.CLAN_TO_CLAN)
			{
				L2Clan clan = playerInstance.getClan();
				if (clan != null)
				{
					clan.setReputationScore(clan.getReputationScore() + (points * 5), false);
					playerInstance.sendPacket(new PledgeShowInfoUpdate(clan));
					SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_ADDED_S1S_POINTS_TO_REPUTATION_SCORE);
					sm.addNumber((points * 5));
					playerInstance.sendPacket(sm);
				}
			}
		}
		
		// paralyze
		playerInstance.startAbnormalEffect(0x0400);
		playerInstance.setIsParalyzed(true);
		StopMove sm = new StopMove(playerInstance);
		playerInstance.sendPacket(sm);
		playerInstance.broadcastPacket(sm);
		
	}
	
}
