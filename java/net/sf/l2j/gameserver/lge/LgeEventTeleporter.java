/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.lge;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class LgeEventTeleporter implements Runnable
{
	/** The instance of the player to teleport */
	private final L2PcInstance _playerInstance;
	/** Coordinates of the spot to teleport to */
	private final Location _coordinates;
	/** Admin removed this player from event */
	private final boolean _adminRemove;
	/** Link to TVT Event */
	private final LgeEvent _tvtEvent;
	
	/**
	 * Initialize the teleporter and start the delayed task
	 * @param _TvTEvent
	 * @param playerInstance
	 * @param location
	 * @param fastSchedule
	 * @param adminRemove
	 */
	public LgeEventTeleporter(LgeEvent _TvTEvent, L2PcInstance playerInstance, Location location, boolean fastSchedule, boolean adminRemove)
	{
		_playerInstance = playerInstance;
		_coordinates = location;
		_adminRemove = adminRemove;
		_tvtEvent = _TvTEvent;
		
		if (Config.TVT_EVENT_TEST_MODE)
		{
			System.out.println("LgeEventTeleporter: " + _playerInstance + ", " + _tvtEvent);
		}
		
		// in config as seconds
		boolean started = _tvtEvent.isStarted();
		long delay = (long) ((started ? Config.TVT_EVENT_RESPAWN_TELEPORT_DELAY * _tvtEvent.timeMod() : Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000);
		
		if (Config.TVT_EVENT_TEST_MODE)
		{
			System.out.println("started: " + started + ", delay: " + delay);
		}
		
		SystemMessage sm = null;
		if (_tvtEvent.isStarting())
		{
			sm = new SystemMessage(SystemMessageId.LOL_GAME_START);
			sm.addString(String.valueOf(Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY));
		}
		
		if (started)
		{
			sm = new SystemMessage(SystemMessageId.LOL_RESPAWN);
			sm.addString(String.valueOf((int) (Config.TVT_EVENT_RESPAWN_TELEPORT_DELAY * _tvtEvent.timeMod())));
		}
		
		if (_tvtEvent.isInactivating())
		{
			sm = new SystemMessage(SystemMessageId.LOL_GAME_END);
			sm.addString(String.valueOf(Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY));
		}
		
		_playerInstance.sendPacket(sm);
		sm = null;
		
		if (fastSchedule)
		{
			delay = 0;
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(this, delay);
	}
	
	/**
	 * The task method to teleport the player<br>
	 * 1. Unsummon pet if there is one 2. Remove all effects 3. Revive and full heal the player 4. Teleport the player 5. Broadcast status and user info
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		if (_playerInstance == null)
		{
			return;
		}
		
		L2Summon summon = _playerInstance.getPet();
		
		if (summon != null)
		{
			summon.unSummon(_playerInstance);
		}
		
		for (L2Effect effect : _playerInstance.getAllEffects())
		{
			if (effect != null)
			{
				effect.exit();
			}
		}
		
		_playerInstance.stopAbnormalEffect((short) 0x0400);
		_playerInstance.setIsParalyzed(false);
		
		_playerInstance.doRevive();
		_playerInstance.setCurrentCp(_playerInstance.getMaxCp());
		_playerInstance.setCurrentHp(_playerInstance.getMaxHp());
		_playerInstance.setCurrentMp(_playerInstance.getMaxMp());
		
		_playerInstance.teleToLocation(_coordinates.getX(), _coordinates.getY(), _coordinates.getZ(), true);
		
		if ((_tvtEvent.isStarted() || _tvtEvent.isStarting()) && !_adminRemove)
		{
			_playerInstance.setTeam(_tvtEvent.getParticipantTeamId(_playerInstance.getName()) + 1);
			_playerInstance.setCurrentLgeEvent(_tvtEvent);
			_playerInstance.setInstanceId(_tvtEvent.getEventInstanceId());
			if (_tvtEvent.isStarting())
			{
				_playerInstance.leaveParty();
			}
			_playerInstance.getKnownList().updateKnownObjects();
		}
		else
		{
			_playerInstance.setTeam(0);
			_playerInstance.setInstanceId(0);
			_playerInstance.setCurrentLgeEvent(null);
			_playerInstance.getKnownList().updateKnownObjects();
		}
		
		_playerInstance.broadcastStatusUpdate();
		_playerInstance.broadcastUserInfo();
	}
}
