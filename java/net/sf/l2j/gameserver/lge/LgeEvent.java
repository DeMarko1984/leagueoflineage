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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2LgeTowerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2LgeTowerInstance.L2LgeTowerHealTask;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

/**
 * @author FBIagent
 */
public class LgeEvent
{
	enum EventState
	{
		INACTIVE,
		INACTIVATING,
		PARTICIPATING,
		STARTING,
		STARTED,
		REWARDING
	}
	
	public enum EventType
	{
		FIVE_TO_FIVE,
		TEN_TO_TEN,
		CLAN_TO_CLAN
	}
	
	private final LgeEventTeam[] _teams = new LgeEventTeam[2]; // event only allow max 2 teams
	private EventState _state = EventState.INACTIVE;
	private int _EventInstanceId;
	private long _eventStartTime = 0;
	private final List<L2Spawn> _inEventSpawns;
	private final List<L2NpcInstance> _inEventNpcs;
	private EventType _eventType;
	private final Map<String, Integer> _playersPoints;
	private final Map<String, Location> _playersBackLocations;
	
	private final static Location[] TVT_EVENT_TEAM_COORDINATES = new Location[]
	{
		new Location(17698, 108917, -6476),
		new Location(113311, 14735, 10077),
		new Location(212296, -113665, -1636),
		new Location(112003, -77347, 58),
		new Location(148497, 46720, -3413),
		new Location(17718, 112154, -6584),
		new Location(115201, 16614, 10077),
		new Location(213896, -115436, -1639),
		new Location(113629, -75679, 58),
		new Location(150475, 46720, -3413)
	};
	
	/**
	 * @param eventType
	 */
	public LgeEvent(EventType eventType)
	{
		int rnd = Rnd.get(0, 4);
		Location loc1 = TVT_EVENT_TEAM_COORDINATES[rnd];
		Location loc2 = TVT_EVENT_TEAM_COORDINATES[rnd + 5];
		_teams[0] = new LgeEventTeam(Config.TVT_EVENT_TEAM_1_NAME, loc1, 1);
		_teams[1] = new LgeEventTeam(Config.TVT_EVENT_TEAM_2_NAME, loc2, 2);
		_playersPoints = new ConcurrentHashMap<>();
		_inEventSpawns = new ArrayList<>();
		_inEventNpcs = new ArrayList<>();
		_playersBackLocations = new ConcurrentHashMap<>();
		setEventType(eventType);
		setEventInstanceId(Rnd.nextInt());
		setState(EventState.PARTICIPATING);
	}
	
	public int getTeamsMidLevel()
	{
		if ((_teams[0].getParticipatedPlayerCount() == 0) && (_teams[1].getParticipatedPlayerCount() == 0))
		{
			return 0;
		}
		int midLevelTeam1 = _teams[0].getParticipatedPlayerMidLevel();
		int midLevelTeam2 = _teams[1].getParticipatedPlayerMidLevel();
		if ((midLevelTeam1 > 0) && (midLevelTeam2 > 0))
		{
			return Math.round((midLevelTeam1 + midLevelTeam2) / 2);
		}
		else if (midLevelTeam1 > 0)
		{
			return midLevelTeam1;
		}
		else if (midLevelTeam2 > 0)
		{
			return midLevelTeam2;
		}
		return 0;
	}
	
	public boolean canParticipatPlayer(L2PcInstance player)
	{
		// check for full teams
		if (_teams[0].isFull(getEventType()) && _teams[1].isFull(getEventType()))
		{
			return false;
		}
		
		if (getEventType() == EventType.CLAN_TO_CLAN)
		{
			L2Clan teamclan0 = _teams[0].getParticipatedClan();
			if (teamclan0 == null)
			{
				return true;
			}
			L2Clan teamclan1 = _teams[1].getParticipatedClan();
			if (teamclan1 == null)
			{
				return true;
			}
			return (teamclan0 == player.getClan()) || (teamclan1 == player.getClan());
		}
		
		int midLevel = getTeamsMidLevel();
		if (midLevel == 0)
		{
			return true;
		}
		return ((midLevel + 5) >= player.getLevel()) && ((midLevel - 5) <= player.getLevel());
	}
	
	/**
	 * Starts the TvTEvent fight<br>
	 * 1. Set state EventState.STARTING<br>
	 * 2. Close doors specified in configs<br>
	 * 3. Abort if not enought participants(return false)<br>
	 * 4. Set state EventState.STARTED<br>
	 * 5. Teleport all participants to team spot<br>
	 * <br>
	 * @return boolean<br>
	 */
	public boolean startFight()
	{
		if (!isParticipating())
		{
			return false;
		}
		
		setState(EventState.STARTING);
		
		// not enought participants
		if (!_teams[0].canStartPlayers(this.getEventType()) || !_teams[1].canStartPlayers(getEventType()))
		{
			setState(EventState.INACTIVE);
			_teams[0].cleanMe();
			_teams[1].cleanMe();
			// unSpawnNpc();
			return false;
		}
		
		closeDoors();
		
		// teleport all participants to there team spot
		for (LgeEventTeam team : _teams)
		{
			for (String playerName : team.getParticipatedPlayerNames())
			{
				L2PcInstance playerInstance = team.getParticipatedPlayer(playerName);
				
				if (playerInstance == null)
				{
					continue;
				}
				
				// save current locations
				this.setBackLocation(playerInstance.getName(), new Location(playerInstance.getX(), playerInstance.getY(), playerInstance.getZ()));
				
				// implements Runnable and starts itself in constructor
				new LgeEventTeleporter(this, playerInstance, team.getCoordinates(), false, false);
			}
		}
		
		_eventStartTime = System.currentTimeMillis();
		
		return true;
	}
	
	public double timeMod()
	{
		if (_eventStartTime <= 0)
		{
			return 0;
		}
		int runningTime = Config.TVT_EVENT_TEST_MODE == true ? 3 : Config.TVT_EVENT_RUNNING_TIME;
		return 1 + ((System.currentTimeMillis() - _eventStartTime) / (runningTime * (1000 * 60)));
	}
	
	public boolean timeEnd()
	{
		int runningTime = Config.TVT_EVENT_TEST_MODE == true ? 3 : Config.TVT_EVENT_RUNNING_TIME;
		return (_eventStartTime + (runningTime * (1000 * 60))) <= System.currentTimeMillis();
	}
	
	/**
	 * Calculates the TvTEvent reward<br>
	 * 1. If both teams are at a tie(points equals), send it as system message to all participants, if one of the teams have 0 participants left online abort rewarding<br>
	 * 2. Wait till teams are not at a tie anymore<br>
	 * 3. Set state EvcentState.REWARDING<br>
	 * 4. Reward team with more points<br>
	 * 5. Show win html to wining team participants<br>
	 * <br>
	 */
	public void calculateRewards()
	{
		if (!isStarted())
		{
			return;
		}
		
		setState(EventState.REWARDING); // after state REWARDING is set, nobody can point anymore
		
		if ((_teams[0].getPoints() == 0) && (_teams[1].getPoints() == 0))
		{
			return;
		}
		
		// random one point if middle point by teams
		if (_teams[0].getPoints() == _teams[1].getPoints())
		{
			if (Rnd.nextBoolean())
			{
				_teams[0].increasePoints();
			}
			else
			{
				_teams[1].increasePoints();
			}
		}
		
		byte teamId = (byte) (_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1); // which team wins?
		LgeEventTeam team = _teams[teamId];
		
		L2PcInstance playerInstance;
		int timeToNext = 0;
		for (String playerName : team.getParticipatedPlayerNames())
		{
			playerInstance = team.getParticipatedPlayer(playerName);
			ThreadPoolManager.getInstance().scheduleGeneral(new LgeEventRewardsTask(playerInstance, team, true, getEventType(), this), timeToNext);
			timeToNext += 500;
		}
		
		// lose team
		LgeEventTeam team2 = _teams[teamId == 0 ? 1 : 0];
		for (String playerName : team2.getParticipatedPlayerNames())
		{
			playerInstance = team2.getParticipatedPlayer(playerName);
			ThreadPoolManager.getInstance().scheduleGeneral(new LgeEventRewardsTask(playerInstance, team2, false, getEventType(), this), timeToNext);
			timeToNext += 500;
		}
	}
	
	/**
	 * Stops the TvTEvent fight<br>
	 * 1. Set state EventState.INACTIVATING<br>
	 * 2. Remove tvt npc from world<br>
	 * 3. Open doors specified in configs<br>
	 * 4. Teleport all participants back to participation npc location<br>
	 * 5. Teams cleaning<br>
	 * 6. Set state EventState.INACTIVE<br>
	 */
	public void stopFight()
	{
		setState(EventState.INACTIVATING);
		openDoors();
		removeInEventSpawns();
		
		for (LgeEventTeam team : _teams)
		{
			for (String playerName : team.getParticipatedPlayerNames())
			{
				L2PcInstance playerInstance = team.getParticipatedPlayer(playerName);
				
				if (playerInstance == null)
				{
					continue;
				}
				
				Location loc = this.getBackLocation(playerInstance.getName());
				if (loc == null)
				{
					loc = new Location(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0], Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1], Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
				}
				new LgeEventTeleporter(this, playerInstance, loc, false, false);
			}
		}
		
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		setState(EventState.INACTIVE);
	}
	
	/**
	 * Adds a player to a TvTEvent team<br>
	 * 1. Calculate the id of the team in which the player should be added<br>
	 * 2. Add the player to the calculated team
	 * @param playerInstance
	 * @return boolean
	 */
	public boolean addParticipant(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}
		
		if (getEventType() == EventType.CLAN_TO_CLAN)
		{
			L2Clan teamclan0 = _teams[0].getParticipatedClan();
			if (teamclan0 == null)
			{
				return _teams[0].addPlayer(playerInstance);
			}
			L2Clan teamclan1 = _teams[1].getParticipatedClan();
			if (teamclan1 == null)
			{
				return _teams[1].addPlayer(playerInstance);
			}
			if (teamclan0 == playerInstance.getClan())
			{
				return _teams[0].addPlayer(playerInstance);
			}
			if (teamclan1 == playerInstance.getClan())
			{
				return _teams[1].addPlayer(playerInstance);
			}
			return false;
		}
		
		byte teamId = 0;
		
		if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount())
		{
			teamId = (byte) (Rnd.get(2));
		}
		else
		{
			teamId = (byte) (_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);
		}
		
		return _teams[teamId].addPlayer(playerInstance);
	}
	
	/**
	 * Removes a TvTEvent player from it's team<br>
	 * 1. Get team id of the player<br>
	 * 2. Remove player from it's team
	 * @param playerName
	 * @return boolean
	 */
	public boolean removeParticipant(String playerName)
	{
		byte teamId = getParticipantTeamId(playerName);
		
		if (teamId == -1)
		{
			return false;
		}
		
		_teams[teamId].removePlayer(playerName);
		return true;
	}
	
	/**
	 * Close doors specified in configs
	 */
	private void closeDoors()
	{
		// for (int doorId : Config.TVT_EVENT_DOOR_IDS)
		// {
		// L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
		
		// if (doorInstance != null)
		// {
		// doorInstance.closeMe();
		// }
		// }
	}
	
	private void spawnControlTowers()
	{
		L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(70103);
		try
		{
			L2Spawn ct1 = new L2Spawn(template1);
			ct1.setLocx(_teams[0].getCoordinates().getX());
			ct1.setLocy(_teams[0].getCoordinates().getY());
			ct1.setLocz(_teams[0].getCoordinates().getZ());
			ct1.setAmount(1);
			ct1.setHeading(0);
			ct1.setRespawnDelay(60);
			SpawnTable.getInstance().addNewSpawn(ct1, false);
			ct1.init();
			_inEventSpawns.add(ct1);
			L2LgeTowerInstance npc1 = (L2LgeTowerInstance) ct1.getLastSpawn();
			npc1.setInstanceId(getEventInstanceId());
			npc1.setTeam(_teams[0]);
			npc1.setEvent(this);
			npc1.setTitle(_teams[0].getName());
			_inEventNpcs.add(npc1);
			
			new L2LgeTowerHealTask(npc1, this, _teams[0]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		L2NpcTemplate template2 = NpcTable.getInstance().getTemplate(70102);
		try
		{
			L2Spawn ct2 = new L2Spawn(template2);
			ct2.setLocx(_teams[1].getCoordinates().getX());
			ct2.setLocy(_teams[1].getCoordinates().getY());
			ct2.setLocz(_teams[1].getCoordinates().getZ());
			ct2.setAmount(1);
			ct2.setHeading(0);
			ct2.setRespawnDelay(60);
			SpawnTable.getInstance().addNewSpawn(ct2, false);
			ct2.init();
			_inEventSpawns.add(ct2);
			L2LgeTowerInstance npc2 = (L2LgeTowerInstance) ct2.getLastSpawn();
			npc2.setInstanceId(getEventInstanceId());
			npc2.setTeam(_teams[1]);
			npc2.setEvent(this);
			npc2.setTitle(_teams[1].getName());
			_inEventNpcs.add(npc2);
			
			new L2LgeTowerHealTask(npc2, this, _teams[1]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void removeInEventSpawns()
	{
		for (L2Spawn spawn : _inEventSpawns)
		{
			if (spawn == null)
			{
				continue;
			}
			spawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(spawn, false);
		}
		for (L2NpcInstance l2NpcInstance : _inEventNpcs)
		{
			if (l2NpcInstance == null)
			{
				continue;
			}
			l2NpcInstance.deleteMe();
		}
	}
	
	/**
	 * Open doors specified in configs
	 */
	private void openDoors()
	{
		for (int doorId : Config.TVT_EVENT_DOOR_IDS)
		{
			L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
			
			if (doorInstance != null)
			{
				doorInstance.openMe();
			}
		}
	}
	
	/**
	 * Called when a player logs in
	 * @param playerInstance
	 * @return
	 */
	public boolean onLogin(L2PcInstance playerInstance)
	{
		if ((playerInstance == null) || (!isStarting() && !isStarted()))
		{
			return false;
		}
		
		byte teamId = getParticipantTeamId(playerInstance.getName());
		
		if (teamId == -1)
		{
			return false;
		}
		
		_teams[teamId].addPlayer(playerInstance);
		new LgeEventTeleporter(this, playerInstance, _teams[teamId].getCoordinates(), true, false);
		return true;
	}
	
	/**
	 * Called when a player logs out
	 * @param playerInstance
	 */
	public void onLogout(L2PcInstance playerInstance)
	{
		if ((playerInstance == null) || (!isStarting() && !isStarted()))
		{
			return;
		}
		
		removeParticipant(playerInstance.getName());
	}
	
	/**
	 * Called on every potion use
	 * @param playerName
	 * @return boolean
	 */
	public boolean onPotionUse(String playerName)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerName) && !Config.TVT_EVENT_POTIONS_ALLOWED)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Called on every escape use(thanks to nbd)
	 * @param playerName
	 * @return boolean
	 */
	public boolean onEscapeUse(String playerName)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerName))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Called on every summon item use
	 * @param playerName
	 * @return boolean
	 */
	public boolean onItemSummon(String playerName)
	{
		if (!isStarted())
		{
			return true;
		}
		
		if (isPlayerParticipant(playerName) && !Config.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Is called when a player is killed
	 * @param killerCharacter
	 * @param killedPlayerInstance
	 */
	public void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if (Config.TVT_EVENT_TEST_MODE)
		{
			System.out.println("onKill: " + killerCharacter + ", " + killedPlayerInstance);
		}
		
		if ((killerCharacter == null) || (killedPlayerInstance == null)
			|| (!(killerCharacter instanceof L2PcInstance) && !(killerCharacter instanceof L2PetInstance) && !(killerCharacter instanceof L2SummonInstance)))
		{
			return;
		}
		
		if (Config.TVT_EVENT_TEST_MODE)
		{
			System.out.println("onKill: " + killerCharacter + ", " + killedPlayerInstance);
		}
		
		L2PcInstance killerPlayerInstance = null;
		
		if ((killerCharacter instanceof L2PetInstance) || (killerCharacter instanceof L2SummonInstance))
		{
			killerPlayerInstance = ((L2Summon) killerCharacter).getOwner();
			
			if (killerPlayerInstance == null)
			{
				return;
			}
		}
		else
		{
			killerPlayerInstance = (L2PcInstance) killerCharacter;
		}
		
		if (Config.TVT_EVENT_TEST_MODE)
		{
			System.out.println("onKill: " + killerCharacter + ", " + killedPlayerInstance);
		}
		
		byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getName());
		if (killedTeamId != -1)
		{
			if (Config.TVT_EVENT_TEST_MODE)
			{
				System.out.println("killedTeamId: " + killedTeamId);
			}
			new LgeEventTeleporter(this, killedPlayerInstance, _teams[killedTeamId].getCoordinates(), false, false);
		}
		
		String name = killerPlayerInstance.getName();
		byte killerTeamId = getParticipantTeamId(name);
		if (Config.TVT_EVENT_TEST_MODE)
		{
			System.out.println("killerTeamId: " + killerTeamId);
		}
		if ((killerTeamId != -1) && (killedTeamId != -1) && (killerTeamId != killedTeamId))
		{
			_teams[killerTeamId].increasePoints();
			
			// add fixed point to player instance
			increasePlayerPoints(name, 1);
		}
	}
	
	private void increasePlayerPoints(String playerName, int count)
	{
		synchronized (_playersPoints)
		{
			int currentPoints = 0;
			if (_playersPoints.containsKey(playerName))
			{
				currentPoints = _playersPoints.get(playerName);
			}
			currentPoints += count;
			_playersPoints.put(playerName, currentPoints);
		}
	}
	
	public void incPoints(L2PcInstance killerPlayerInstance, int count)
	{
		if ((killerPlayerInstance == null) || !isStarted())
		{
			return;
		}
		
		String playerName = killerPlayerInstance.getName();
		byte killerTeamId = getParticipantTeamId(playerName);
		
		if ((killerTeamId != -1))
		{
			_teams[killerTeamId].increasePoints(count);
			increasePlayerPoints(playerName, count);
		}
	}
	
	/**
	 * Sets the TvTEvent state
	 * @param state
	 */
	private void setState(EventState state)
	{
		synchronized (_state)
		{
			_state = state;
		}
	}
	
	/**
	 * Is TvTEvent inactive?
	 * @return boolean
	 */
	public boolean isInactive()
	{
		boolean b;
		synchronized (_state)
		{
			b = _state == EventState.INACTIVE;
		}
		return b;
	}
	
	/**
	 * Is TvTEvent in inactivating?
	 * @return boolean
	 */
	public boolean isInactivating()
	{
		boolean b;
		synchronized (_state)
		{
			b = _state == EventState.INACTIVATING;
		}
		return b;
	}
	
	/**
	 * Is TvTEvent in participation?
	 * @return boolean
	 */
	public boolean isParticipating()
	{
		boolean b;
		synchronized (_state)
		{
			b = _state == EventState.PARTICIPATING;
		}
		return b;
	}
	
	/**
	 * Is TvTEvent starting?
	 * @return boolean
	 */
	public boolean isStarting()
	{
		boolean b;
		synchronized (_state)
		{
			b = _state == EventState.STARTING;
		}
		return b;
	}
	
	/**
	 * Is TvTEvent started?
	 * @return boolean
	 */
	public boolean isStarted()
	{
		boolean b;
		synchronized (_state)
		{
			b = _state == EventState.STARTED;
		}
		return b;
	}
	
	/**
	 * Is TvTEvent rewarding?
	 * @return boolean
	 */
	public boolean isRewarding()
	{
		boolean b;
		synchronized (_state)
		{
			b = _state == EventState.REWARDING;
		}
		return b;
	}
	
	/**
	 * Returns the team id of a player, if player is not participant it returns -1
	 * @param playerName
	 * @return byte
	 */
	public byte getParticipantTeamId(String playerName)
	{
		return (byte) (_teams[0].containsPlayer(playerName) ? 0 : (_teams[1].containsPlayer(playerName) ? 1 : -1));
	}
	
	/**
	 * Returns the team coordinates in which the player is in, if player is not in a team return null
	 * @param playerName
	 * @return int[]
	 */
	public Location getParticipantTeamCoordinates(String playerName)
	{
		return _teams[0].containsPlayer(playerName) ? _teams[0].getCoordinates() : (_teams[1].containsPlayer(playerName) ? _teams[1].getCoordinates() : null);
	}
	
	/**
	 * Is given player participant of the event?
	 * @param playerName
	 * @return boolean
	 */
	public boolean isPlayerParticipant(String playerName)
	{
		return _teams[0].containsPlayer(playerName) || _teams[1].containsPlayer(playerName);
	}
	
	/**
	 * Returns participated player count<br>
	 * <br>
	 * @return int<br>
	 */
	public int getParticipatedPlayersCount()
	{
		return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
	}
	
	/**
	 * Returns teams names<br>
	 * <br>
	 * @return String[]<br>
	 */
	public String[] getTeamNames()
	{
		return new String[]
		{
			_teams[0].getName(),
			_teams[1].getName()
		};
	}
	
	/**
	 * Returns player count of both teams<br>
	 * <br>
	 * @return int[]<br>
	 */
	public int[] getTeamsPlayerCounts()
	{
		return new int[]
		{
			_teams[0].getParticipatedPlayerCount(),
			_teams[1].getParticipatedPlayerCount()
		};
	}
	
	/**
	 * Returns points count of both teams
	 * @return int[]
	 */
	public int[] getTeamsPoints()
	{
		return new int[]
		{
			_teams[0].getPoints(),
			_teams[1].getPoints()
		};
	}
	
	/**
	 * @return the _EventInstanceId
	 */
	public int getEventInstanceId()
	{
		return _EventInstanceId;
	}
	
	/**
	 * @param _EventInstanceId the _EventInstanceId to set
	 */
	public void setEventInstanceId(int _EventInstanceId)
	{
		this._EventInstanceId = _EventInstanceId;
	}
	
	/**
	 * @return
	 */
	public boolean canStartFight()
	{
		if (!isParticipating())
		{
			return false;
		}
		if (!_teams[0].canStartPlayers(this.getEventType()) || !_teams[1].canStartPlayers(this.getEventType()))
		{
			return false;
		}
		return true;
	}
	
	/**
	 * @return the _eventType
	 */
	public EventType getEventType()
	{
		return _eventType;
	}
	
	/**
	 * @param _eventType the _eventType to set
	 */
	public void setEventType(EventType _eventType)
	{
		this._eventType = _eventType;
	}
	
	/**
	 * 
	 */
	public void nowStart()
	{
		// not enought participants
		if (!_teams[0].canStartPlayers(this.getEventType()) || !_teams[1].canStartPlayers(getEventType()))
		{
			stopFight();
			return;
		}
		
		setState(EventState.STARTED);
		
		// spawn towers after
		spawnControlTowers();
	}
	
	/**
	 * @param tvTEvent
	 * @param name
	 * @return
	 */
	public int getPlayerPoints(LgeEvent tvTEvent, String name)
	{
		int playersPoints = 0;
		synchronized (_playersPoints)
		{
			playersPoints = _playersPoints.containsKey(name) ? _playersPoints.get(name) : 0;
		}
		return playersPoints;
	}
	
	/**
	 * @return
	 */
	public String getState()
	{
		return _state.toString();
	}
	
	/**
	 * @param sm
	 */
	public void broadсastMessage(SystemMessage sm)
	{
		_teams[0].broadсastMessage(sm);
		_teams[1].broadсastMessage(sm);
	}
	
	public void setBackLocation(String playerName, Location loc)
	{
		synchronized (_playersBackLocations)
		{
			_playersBackLocations.put(playerName, loc);
		}
	}
	
	public Location getBackLocation(String playerName)
	{
		Location location = null;
		synchronized (_playersBackLocations)
		{
			location = _playersBackLocations.get(playerName);
		}
		return location;
	}
}
