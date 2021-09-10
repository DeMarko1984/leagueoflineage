/* This program is free software; you can redistribute it and/or modify
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
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

/**
 * A no landing zone
 * @author durgus
 */
public class L2OnlyInstanceZone extends L2ZoneType
{
	
	public class ToSpawnTeleportationTask implements Runnable
	{
		private final L2Character _character;
		
		public ToSpawnTeleportationTask(L2Character character)
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
			
			if (!_character.isInsideZone(L2Character.ZONE_ONLY_INSTANCE) || (_character.getInstanceId() != 0))
			{
				return;
			}
			
			// move to base location
			// _character.teleToLocation(RequestRestartPoint.LOC, true);
		}
	}
	
	public L2OnlyInstanceZone()
	{
		super();
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_ONLY_INSTANCE, true);
			// start back teleport task
			ThreadPoolManager.getInstance().scheduleGeneral(new ToSpawnTeleportationTask(character), 1000);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_ONLY_INSTANCE, false);
		}
	}
	
	@Override
	protected void onDieInside(L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(L2Character character)
	{
	}
	
}
