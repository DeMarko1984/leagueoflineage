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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.lge.LgeEvent.EventType;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;

/**
 * @author FBIagent
 */
public class LgeManager implements Runnable
{
	/**
	 * The one and only instance of this class<br>
	 */
	private static LgeManager _instance = null;
	private static ArrayList<LgeEvent> _tvtEvents;
	private final Object _lockObject = new Object();
	private static Logger _log = Logger.getLogger(LgeManager.class.getName());
	
	/**
	 * New instance only by getInstance()<br>
	 */
	private LgeManager()
	{
		_tvtEvents = new ArrayList<>();
		
		if (Config.TVT_EVENT_ENABLED)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 60000, 60000);
			System.out.println("Legauge of Lineage II Started.");
		}
		else
		{
			System.out.println("Legauge of Lineage II Disabled.");
		}
	}
	
	/**
	 * Initialize new/Returns the one and only instance<br>
	 * <br>
	 * @return TvTManager<br>
	 */
	public static LgeManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new LgeManager();
		}
		
		return _instance;
	}
	
	/**
	 * The task method to handle cycles of the event
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		synchronized (_lockObject)
		{
			// clear old tvt events
			cleanTvTEvents();
			
			// before start, all stop fights
			stopFight();
			
			// can be start fight
			startFight();
			
			// can be give rewards and stop fight
			timeEnd();
		}
	}
	
	private void timeEnd()
	{
		for (LgeEvent tvTEvent : _tvtEvents)
		{
			if (!tvTEvent.isStarted())
			{
				continue;
			}
			if (tvTEvent.timeEnd())
			{
				tvTEvent.calculateRewards();
				tvTEvent.stopFight();
			}
		}
	}
	
	private void stopFight()
	{
		for (LgeEvent tvTEvent : _tvtEvents)
		{
			if (!tvTEvent.isRewarding())
			{
				continue;
			}
			tvTEvent.stopFight();
		}
	}
	
	private void startFight()
	{
		boolean hasEventStarted = false;
		
		for (LgeEvent tvTEvent : _tvtEvents)
		{
			if (!tvTEvent.isParticipating())
			{
				continue;
			}
			if (tvTEvent.canStartFight())
			{
				if (tvTEvent.startFight())
				{
					hasEventStarted = true;
				}
			}
		}
		
		if (hasEventStarted)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				synchronized (_lockObject)
				{
					for (LgeEvent tvTEvent : _tvtEvents)
					{
						if (tvTEvent.isStarting())
						{
							tvTEvent.nowStart();
						}
					}
				}
			}, (Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY * 1000) - 500);
		}
	}
	
	private void cleanTvTEvents()
	{
		List<LgeEvent> _toremove = new ArrayList<>();
		for (LgeEvent tvTEvent : _tvtEvents)
		{
			if (!tvTEvent.isInactive())
			{
				continue;
			}
			_toremove.add(tvTEvent);
		}
		for (LgeEvent tvTEvent : _toremove)
		{
			_tvtEvents.remove(tvTEvent);
		}
	}
	
	public LgeEvent[] getCurrentEvents()
	{
		LgeEvent[] _toReturn = new LgeEvent[0];
		synchronized (_lockObject)
		{
			_toReturn = _tvtEvents.toArray(new LgeEvent[_tvtEvents.size()]);
		}
		return _toReturn;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public boolean isPlayerParticipant(String name)
	{
		boolean toReturn = false;
		synchronized (_lockObject)
		{
			for (LgeEvent tvTEvent : _tvtEvents)
			{
				if (tvTEvent.isInactivating() || tvTEvent.isInactive())
				{
					continue;
				}
				if (tvTEvent.isPlayerParticipant(name))
				{
					toReturn = true;
					break;
				}
			}
		}
		return toReturn;
	}
	
	/**
	 * Called on every bypass by npc of type L2TvTEventNpc<br>
	 * Needs synchronization cause of the max player check
	 * @param command
	 * @param playerInstance
	 */
	public void onBypass(String command, L2PcInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return;
		}
		
		if (command.equals("tvt_event_p_ten_to_ten") || command.equals("tvt_event_p_five_to_five") || command.equals("tvt_event_p_clan_to_clan"))
		{
			if (playerInstance.isCursedWeaponEquiped())
			{
				// npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>Cursed weapon owners are not allowed to participate.</body></html>");
				playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_CURSED_WEAPON));
			}
			else if (playerInstance.getKarma() > 0)
			{
				// npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>Chaotic players are not allowed to participate.</body></html>");
				playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_KARMA));
			}
			else
			{
				EventType type = null;
				
				if (command.equals("tvt_event_p_ten_to_ten"))
				{
					type = EventType.TEN_TO_TEN;
				}
				if (command.equals("tvt_event_p_five_to_five"))
				{
					type = EventType.FIVE_TO_FIVE;
				}
				if (command.equals("tvt_event_p_clan_to_clan"))
				{
					type = EventType.CLAN_TO_CLAN;
				}
				
				if ((type == EventType.CLAN_TO_CLAN))
				{
					if ((playerInstance.getClan() == null))
					{
						playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_ONLY_CLAN_MEMBER));
						playerInstance.sendActionFailed();
						return;
					}
					
					L2PcInstance[] clanMembers = playerInstance.getClan().getOnlineMembers(playerInstance.getName());
					
					if (clanMembers.length < (Config.TVT_EVENT_TEST_MODE == true ? 0 : 5))
					{
						playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_CLAN_TO_CLAN_MANY_MEMBERS));
						playerInstance.sendActionFailed();
						return;
					}
					
					if (addParticipant(playerInstance, type))
					{
						// npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>You are on the registration list now.</body></html>");
						if ((playerInstance.getLevel() >= 76) && playerInstance.decLgePoints(5, true))
						{
							SystemMessage sm = new SystemMessage(SystemMessageId.USE_LGE_POINTS);
							sm.addString(String.valueOf(5));
							playerInstance.sendPacket(sm);
						}
						playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_PARCIPIANT_SUCCESS));
					}
					else
					{
						playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_PARCIPIANT_RETRY));
					}
				}
				else if (addParticipant(playerInstance, type))
				{
					if ((playerInstance.getLevel() >= 76) && playerInstance.decLgePoints(5, true))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.USE_LGE_POINTS);
						sm.addString(String.valueOf(5));
						playerInstance.sendPacket(sm);
					}
					playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_PARCIPIANT_SUCCESS));
				}
				else
				{
					playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_PARCIPIANT_RETRY));
				}
			}
		}
		else if (command.equals("tvt_event_remove_participation"))
		{
			removeParticipant(playerInstance.getName());
			playerInstance.sendPacket(new SystemMessage(SystemMessageId.LOL_PARCIPIANT_REMOVE));
		}
		playerInstance.sendActionFailed();
	}
	
	/**
	 * @param name
	 */
	private void removeParticipant(String name)
	{
		synchronized (_lockObject)
		{
			for (LgeEvent tvTEvent : _tvtEvents)
			{
				if (tvTEvent.isInactivating() || tvTEvent.isInactive())
				{
					continue;
				}
				if (!tvTEvent.isPlayerParticipant(name))
				{
					continue;
				}
				if (tvTEvent.removeParticipant(name))
				{
					break;
				}
			}
		}
	}
	
	/**
	 * @param playerInstance
	 * @param eventType
	 * @return
	 */
	private boolean addParticipant(L2PcInstance playerInstance, EventType eventType)
	{
		if ((playerInstance.getLevel() >= 76) && (playerInstance.getLgePoints() < 5))
		{
			playerInstance.sendSystemMessage(SystemMessageId.NO_LGE_POINTS);
			return false;
		}
		
		boolean toReturn = false;
		boolean canParticipant = true;
		synchronized (_lockObject)
		{
			if (_tvtEvents.isEmpty())
			{
				_tvtEvents.add(new LgeEvent(eventType));
			}
			// check events by type
			boolean needToCreateEvent = true;
			for (LgeEvent tvTEvent : _tvtEvents)
			{
				if ((tvTEvent.getEventType() == eventType) && tvTEvent.isParticipating())
				{
					needToCreateEvent = false;
				}
			}
			if (needToCreateEvent)
			{
				_tvtEvents.add(new LgeEvent(eventType));
			}
			
			// fist check for already pacipiated
			for (LgeEvent tvTEvent : _tvtEvents)
			{
				if (tvTEvent.isPlayerParticipant(playerInstance.getName()))
				{
					canParticipant = false;
					break;
				}
			}
			if (canParticipant)
			{
				// check to parcipiate
				for (LgeEvent tvTEvent : _tvtEvents)
				{
					if (tvTEvent.getEventType() != eventType)
					{
						continue;
					}
					if (!tvTEvent.isParticipating())
					{
						continue;
					}
					if (!tvTEvent.canParticipatPlayer(playerInstance))
					{
						continue;
					}
					if (tvTEvent.addParticipant(playerInstance))
					{
						toReturn = true;
						break;
					}
				}
				
				// create new event and participant to
				if (!toReturn)
				{
					LgeEvent e = new LgeEvent(eventType);
					toReturn = e.canParticipatPlayer(playerInstance) && e.addParticipant(playerInstance);
					if (toReturn)
					{
						_tvtEvents.add(e);
					}
				}
			}
		}
		return toReturn;
	}
	
	/**
	 * @param killer
	 * @param l2PcInstance
	 */
	public void onKill(L2Character killer, L2PcInstance l2PcInstance)
	{
		synchronized (_lockObject)
		{
			if (Config.TVT_EVENT_TEST_MODE)
			{
				System.out.println("onKill: " + killer + ", " + l2PcInstance);
			}
			for (LgeEvent tvTEvent : _tvtEvents)
			{
				if (Config.TVT_EVENT_TEST_MODE)
				{
					System.out.println("tvTEvent: " + tvTEvent + ", " + tvTEvent.getState());
				}
				if (!tvTEvent.isStarted())
				{
					continue;
				}
				if (Config.TVT_EVENT_TEST_MODE)
				{
					System.out.println("tvTEvent.isPlayerParticipant(" + l2PcInstance.getName() + ")");
				}
				if (tvTEvent.isPlayerParticipant(l2PcInstance.getName()))
				{
					if (Config.TVT_EVENT_TEST_MODE)
					{
						System.out.println("tvTEvent.onKill(" + killer + ", " + l2PcInstance + ")");
					}
					tvTEvent.onKill(killer, l2PcInstance);
				}
			}
		}
	}
	
	/**
	 * @param player
	 */
	public void onLogout(L2PcInstance player)
	{
		synchronized (_lockObject)
		{
			for (LgeEvent tvTEvent : _tvtEvents)
			{
				if (tvTEvent.isInactivating() || tvTEvent.isInactive())
				{
					continue;
				}
				if (tvTEvent.isPlayerParticipant(player.getName()))
				{
					tvTEvent.onLogout(player);
				}
			}
		}
	}
	
	/**
	 * @param player
	 * @return
	 */
	public boolean onLogin(L2PcInstance player)
	{
		boolean toReturn = false;
		synchronized (_lockObject)
		{
			for (LgeEvent tvTEvent : _tvtEvents)
			{
				if (tvTEvent.isInactivating() || tvTEvent.isInactive())
				{
					continue;
				}
				if (tvTEvent.isPlayerParticipant(player.getName()))
				{
					toReturn = tvTEvent.onLogin(player);
				}
			}
		}
		return toReturn;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public boolean onEscapeUse(String name)
	{
		boolean toReturn = false;
		synchronized (_lockObject)
		{
			for (LgeEvent tvTEvent : _tvtEvents)
			{
				if (tvTEvent.isInactivating() || tvTEvent.isInactive())
				{
					continue;
				}
				if (tvTEvent.isPlayerParticipant(name))
				{
					toReturn = tvTEvent.onEscapeUse(name);
				}
			}
		}
		return toReturn;
	}
	
	public void incPoints(L2PcInstance killerPlayer)
	{
		synchronized (_lockObject)
		{
			for (LgeEvent tvTEvent : _tvtEvents)
			{
				if (tvTEvent.isInactivating() || tvTEvent.isInactive())
				{
					continue;
				}
				if (tvTEvent.isPlayerParticipant(killerPlayer.getName()))
				{
					tvTEvent.incPoints(killerPlayer, Rnd.get(1, 5));
				}
			}
		}
	}
	
	public SystemMessage[] generateSystemMessagesTopPlayers()
	{
		List<SystemMessage> _toReturn = new ArrayList<>();
		
		java.sql.Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con
				.prepareStatement(
					"SELECT characters.char_name AS char_name, character_lge_points.points AS points FROM character_lge_points INNER JOIN characters ON character_lge_points.char_obj_id = characters.obj_Id WHERE character_lge_points.points > 0 ORDER BY character_lge_points.points DESC LIMIT 5;");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.LOL_MASTER_STATUS_INFO);
				sm.addString(rset.getString("char_name"));
				sm.addString(String.valueOf(rset.getInt("points")));
				_toReturn.add(sm);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore lge points top: " + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		
		return _toReturn.toArray(new SystemMessage[_toReturn.size()]);
	}
	
	public String[] getTopPlayers()
	{
		List<String> _toReturn = new ArrayList<>();
		
		java.sql.Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con
				.prepareStatement(
					"SELECT characters.char_name AS char_name, character_lge_points.points AS points FROM character_lge_points INNER JOIN characters ON character_lge_points.char_obj_id = characters.obj_Id WHERE character_lge_points.points > 0 ORDER BY character_lge_points.points DESC LIMIT 5;");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_toReturn.add(rset.getString("char_name"));
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore lge points top: " + e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}
		
		return _toReturn.toArray(new String[_toReturn.size()]);
	}
}
