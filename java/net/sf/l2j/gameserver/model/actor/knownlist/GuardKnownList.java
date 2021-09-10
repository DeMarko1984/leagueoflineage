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

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class GuardKnownList extends AttackableKnownList
{
	// private static Logger _log = Logger.getLogger(GuardKnownList.class.getName());
	
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public GuardKnownList(L2GuardInstance activeChar)
	{
		super(activeChar);
	}
	
	// =========================================================
	// Method - Public
	@Override
	public boolean addKnownObject(L2Object object)
	{
		return addKnownObject(object, null);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.knownlist.AttackableKnownList#getDistanceToForgetObject(net.sf.l2j.gameserver.model.L2Object)
	 */
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		// TODO Auto-generated method stub
		return super.getDistanceToForgetObject(object);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.knownlist.AttackableKnownList#getDistanceToWatchObject(net.sf.l2j.gameserver.model.L2Object)
	 */
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		// TODO Auto-generated method stub
		return super.getDistanceToWatchObject(object);
	}
	
	@Override
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		if (!super.addKnownObject(object, dropper))
		{
			return false;
		}
		
		// Set home location of the L2GuardInstance (if not already done)
		L2GuardInstance activeChar = getActiveChar();
		if (activeChar.getHomeX() == 0)
		{
			activeChar.getHomeLocation();
		}
		
		L2CharacterAI ai = activeChar.getAI();
		if (object instanceof L2PcInstance)
		{
			if (ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			{
				ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}
		
		// Check if the _aggroList of the L2GuardInstance is Empty
		L2GuardInstance activeChar = getActiveChar();
		if (activeChar.noTarget())
		{
			// removeAllKnownObjects();
			
			// Set the L2GuardInstance to AI_INTENTION_IDLE
			L2CharacterAI ai = activeChar.getAI();
			if (ai != null)
			{
				ai.setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			}
		}
		
		return true;
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	@Override
	public final L2GuardInstance getActiveChar()
	{
		return (L2GuardInstance) super.getActiveChar();
	}
}
