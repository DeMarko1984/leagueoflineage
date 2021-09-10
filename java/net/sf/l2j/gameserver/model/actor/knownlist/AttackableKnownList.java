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
package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.util.Util;

public class AttackableKnownList extends NpcKnownList
{
	// =========================================================
	// Data Field
	private HashMap<Integer, L2PlayableInstance> _knownPlayable;
	
	// =========================================================
	// Constructor
	public AttackableKnownList(L2Attackable activeChar)
	{
		super(activeChar);
	}
	
	// =========================================================
	// Method - Public
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}
		
		// Remove the L2Object from the _aggrolist of the L2Attackable
		if ((object != null) && (object instanceof L2Character))
		{
			getActiveChar().getAggroList().remove(object);
		}
		// Set the L2Attackable Intention to AI_INTENTION_IDLE
		boolean knownisEmpty = getKnownPlayersArray().length == 0;
		
		// FIXME: This is a temporary solution
		L2CharacterAI ai = getActiveChar().getAI();
		if ((ai != null) && knownisEmpty)
		{
			ai.setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		if (object instanceof L2PlayableInstance)
		{
			getKnownPlayable().remove(object.getObjectId());
		}
		
		return true;
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	@Override
	public L2Attackable getActiveChar()
	{
		return (L2Attackable) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if (getActiveChar().getAggroListRP() != null)
		{
			if (getActiveChar().getAggroListRP().get(object) != null)
			{
				return 3000;
			}
		}
		return Math.min(2200, 2 * getDistanceToWatchObject(object));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList#addKnownObject(net.sf.l2j.gameserver.model.L2Object, net.sf.l2j.gameserver.model.L2Character)
	 */
	@Override
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		if (!super.addKnownObject(object, dropper))
		{
			return false;
		}
		
		if (object instanceof L2PlayableInstance)
		{
			synchronized (_lockObject)
			{
				getKnownPlayable().put(object.getObjectId(), (L2PlayableInstance) object);
			}
		}
		
		return true;
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if ((object instanceof L2FolkInstance) || !(object instanceof L2Character))
		{
			return 0;
		}
		
		if (object instanceof L2PlayableInstance)
		{
			return 1500;
		}
		
		L2Attackable activeChar = getActiveChar();
		if (activeChar.getAggroRange() > activeChar.getFactionRange())
		{
			return activeChar.getAggroRange();
		}
		
		if (activeChar.getFactionRange() > 200)
		{
			return activeChar.getFactionRange();
		}
		
		return 200;
	}
	
	private final HashMap<Integer, L2PlayableInstance> getKnownPlayable()
	{
		if (_knownPlayable == null)
		{
			_knownPlayable = new HashMap<>();
		}
		return _knownPlayable;
	}
	
	/** Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI. */
	@Override
	public final void removeAllKnownObjects()
	{
		super.removeAllKnownObjects();
		
		synchronized (_lockObject)
		{
			getKnownPlayable().clear();
		}
	}
	
	public final L2PcInstance[] getKnownPlayableInRadius(long radius)
	{
		L2PcInstance[] _toReturn = new L2PcInstance[0];
		
		synchronized (_lockObject)
		{
			Collection<L2PlayableInstance> _objects = getKnownPlayable().values();
			List<L2Object> tmp = new ArrayList<>(_objects.size());
			L2Character activeChar = getActiveChar();
			for (L2PlayableInstance wr : _objects)
			{
				if ((wr != null))
				{
					if (Util.checkIfInRange((int) radius, activeChar, wr, true))
					{
						tmp.add(wr);
					}
				}
			}
			_toReturn = tmp.toArray(new L2PcInstance[tmp.size()]);
		}
		
		return _toReturn;
	}
	
	public final L2PcInstance[] getKnownPlayableArray()
	{
		L2PcInstance[] _toReturn = new L2PcInstance[0];
		
		synchronized (_lockObject)
		{
			Collection<L2PlayableInstance> _objects = getKnownPlayable().values();
			List<L2Object> tmp = new ArrayList<>(_objects.size());
			for (L2PlayableInstance wr : _objects)
			{
				if ((wr != null))
				{
					tmp.add(wr);
				}
			}
			_toReturn = tmp.toArray(new L2PcInstance[tmp.size()]);
		}
		
		return _toReturn;
	}
}
