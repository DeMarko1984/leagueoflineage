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
package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Домашний
 */
public class L2NoLeaveZoneTeleportationTask implements Runnable
{
	private final L2Character _character;
	
	public L2NoLeaveZoneTeleportationTask(L2Character character)
	{
		_character = character;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		if (((L2PcInstance) _character).isGM())
		{
			return;
		}
		
		if (_character.isInsideZone(L2Character.ZONE_NOLEAVE) || ((L2PcInstance) _character).isInLgeEvent())
		{
			return;
		}
		
		// move to base location
		// _character.teleToLocation(RequestRestartPoint.LOC, true);
	}
	
}
