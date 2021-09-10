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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.lge.LgeEvent.EventType;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author FBIagent
 */
public class LgeEventTeam
{
	/**
	 * The name of the team<br>
	 */
	private final String _name;
	/**
	 * The team spot coordinated<br>
	 */
	private final Location _coordinates;
	/**
	 * The points of the team<br>
	 */
	private short _points;
	/**
	 * Name and instance of all participated players in ConcurrentHashMap<br>
	 */
	private Map<String, WeakReference<L2PcInstance>> _participatedPlayers = new ConcurrentHashMap<>();
	/**
	 * Name of all participated players in Vector<br>
	 */
	private Vector<String> _participatedPlayerNames = new Vector<>();
	private final int _id;
	
	/**
	 * C'tor initialize the team
	 * @param name
	 * @param loc
	 * @param id
	 */
	public LgeEventTeam(String name, Location loc, int id)
	{
		_name = name;
		_coordinates = loc;
		_points = 0;
		_id = id;
	}
	
	/**
	 * Adds a player to the team
	 * @param playerInstance
	 * @return boolean
	 */
	public boolean addPlayer(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}
		
		String playerName = playerInstance.getName();
		
		synchronized (_participatedPlayers)
		{
			_participatedPlayers.put(playerName, new WeakReference<>(playerInstance));
			if (!_participatedPlayerNames.contains(playerName))
			{
				_participatedPlayerNames.add(playerName);
			}
		}
		return true;
	}
	
	/**
	 * Removes a player from the team
	 * @param playerName
	 */
	public void removePlayer(String playerName)
	{
		synchronized (_participatedPlayers)
		{
			_participatedPlayers.remove(playerName);
			_participatedPlayerNames.remove(playerName);
		}
	}
	
	/**
	 * Increases the points of the team<br>
	 */
	public void increasePoints()
	{
		_points++;
	}
	
	public void increasePoints(int count)
	{
		_points += count;
	}
	
	/**
	 * Cleanup the team and make it ready for adding players again<br>
	 */
	public void cleanMe()
	{
		synchronized (_participatedPlayers)
		{
			_participatedPlayers.clear();
			_participatedPlayerNames.clear();
		}
		
		_participatedPlayers = new ConcurrentHashMap<>();
		_participatedPlayerNames = new Vector<>();
		_points = 0;
	}
	
	/**
	 * Is given player in this team?
	 * @param playerName
	 * @return boolean
	 */
	public boolean containsPlayer(String playerName)
	{
		boolean containsPlayer;
		
		containsPlayer = _participatedPlayerNames.contains(playerName);
		
		return containsPlayer;
	}
	
	/**
	 * Returns the name of the team
	 * @return String
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Returns the coordinates of the team spot
	 * @return int[]
	 */
	public Location getCoordinates()
	{
		return _coordinates;
	}
	
	/**
	 * Returns the points of the team
	 * @return short
	 */
	public short getPoints()
	{
		return _points;
	}
	
	/**
	 * Returns name of all participated players in Vector
	 * @return Vector<String>
	 */
	public Vector<String> getParticipatedPlayerNames()
	{
		Vector<String> participatedPlayerNames = null;
		
		participatedPlayerNames = _participatedPlayerNames;
		
		return participatedPlayerNames;
	}
	
	/**
	 * Returns player count of this team
	 * @return int
	 */
	public int getParticipatedPlayerCount()
	{
		int participatedPlayerCount;
		
		participatedPlayerCount = _participatedPlayers.size();
		
		return participatedPlayerCount;
	}
	
	public int getParticipatedPlayerMidLevel()
	{
		int playersLevelsSumm = 0;
		int playersCount = 0;
		
		playersCount = _participatedPlayers.size();
		
		if (playersCount <= 0)
		{
			return 0;
		}
		
		synchronized (_participatedPlayers)
		{
			for (WeakReference<L2PcInstance> wk : _participatedPlayers.values())
			{
				if (wk == null)
				{
					continue;
				}
				if (wk.get() == null)
				{
					continue;
				}
				playersLevelsSumm += wk.get().getLevel();
			}
		}
		
		return Math.round(playersLevelsSumm / playersCount);
	}
	
	public L2Clan getParticipatedClan()
	{
		if (_participatedPlayers.size() <= 0)
		{
			return null;
		}
		
		L2Clan clan = null;
		
		synchronized (_participatedPlayers)
		{
			for (WeakReference<L2PcInstance> wk : _participatedPlayers.values())
			{
				if (wk == null)
				{
					continue;
				}
				if (wk.get() == null)
				{
					continue;
				}
				clan = wk.get().getClan();
				break;
			}
		}
		
		return clan;
	}
	
	/**
	 * @param eventType
	 * @return
	 */
	public boolean isFull(EventType eventType)
	{
		switch (eventType)
		{
			case TEN_TO_TEN:
				return getParticipatedPlayerCount() >= 10;
			case FIVE_TO_FIVE:
				return getParticipatedPlayerCount() >= 5;
		}
		return false;
	}
	
	public boolean canStartPlayers(EventType eventType)
	{
		switch (eventType)
		{
			case TEN_TO_TEN:
				return getParticipatedPlayerCount() >= (Config.TVT_EVENT_TEST_MODE == true ? 1 : 10);
			case FIVE_TO_FIVE:
			case CLAN_TO_CLAN:
				return getParticipatedPlayerCount() >= (Config.TVT_EVENT_TEST_MODE == true ? 1 : 5);
		}
		return false;
	}
	
	/**
	 * 
	 */
	public void decreasePoints()
	{
		_points = (short) Math.max(_points / 2, 0);
	}
	
	/**
	 * @param sm
	 */
	public void broadсastMessage(final SystemMessage sm)
	{
		synchronized (_participatedPlayers)
		{
			for (final WeakReference<L2PcInstance> wk : _participatedPlayers.values())
			{
				if (wk == null)
				{
					continue;
				}
				if (wk.get() == null)
				{
					continue;
				}
				
				ThreadPoolManager.getInstance().executeTask(() -> wk.get().sendPacket(sm));
			}
		}
	}
	
	/**
	 * @param playerName
	 * @return
	 */
	public L2PcInstance getParticipatedPlayer(String playerName)
	{
		L2PcInstance l2PcInstance = null;
		synchronized (_participatedPlayers)
		{
			WeakReference<L2PcInstance> weakReference = _participatedPlayers.get(playerName);
			l2PcInstance = weakReference != null ? weakReference.get() : null;
		}
		return l2PcInstance;
	}
	
	/**
	 * @return
	 */
	public int getId()
	{
		return _id;
	}
}
