/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 *
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
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.serverpackets.AutoAttackStop;

/**
 * This class ...
 * @version $Revision: $ $Date: $
 * @author Luca Baldi
 */
public class AttackStanceTaskManager
{
	protected static final Logger _log = Logger.getLogger(AttackStanceTaskManager.class.getName());
	
	protected Map<L2Character, Long> _attackStanceTasks = new ConcurrentHashMap<>();
	
	private static AttackStanceTaskManager _instance;
	
	public AttackStanceTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FightModeScheduler(), 0, 1000);
	}
	
	public static AttackStanceTaskManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new AttackStanceTaskManager();
		}
		
		return _instance;
	}
	
	public void addAttackStanceTask(L2Character actor)
	{
		_attackStanceTasks.put(actor, System.currentTimeMillis());
	}
	
	public void removeAttackStanceTask(L2Character actor)
	{
		_attackStanceTasks.remove(actor);
	}
	
	public boolean getAttackStanceTask(L2Character actor)
	{
		return _attackStanceTasks.containsKey(actor);
	}
	
	private class FightModeScheduler implements Runnable
	{
		protected FightModeScheduler()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			if (_attackStanceTasks == null)
			{
				return;
			}
			
			Long current = System.currentTimeMillis();
			try
			{
				Set<L2Character> keySet;
				keySet = _attackStanceTasks.keySet();
				
				for (L2Character actor : keySet)
				{
					if (actor == null)
					{
						continue;
					}
					
					Long currentAttackStance = _attackStanceTasks.get(actor);
					if (currentAttackStance == null)
					{
						continue;
					}
					
					if ((current - currentAttackStance) > 15000)
					{
						actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
						actor.getAI().setAutoAttacking(false);
						removeAttackStanceTask(actor);
					}
				}
			}
			catch (Throwable e)
			{
				_log.warning(e.getMessage());
			}
		}
	}
}