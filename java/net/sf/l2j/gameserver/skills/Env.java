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
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.skills;

import java.lang.ref.WeakReference;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;

/**
 * An Env object is just a class to pass parameters to a calculator such as L2PcInstance, L2ItemInstance, Initial value.
 */

public final class Env
{
	public WeakReference<L2Character> player;
	public WeakReference<L2Character> target;
	public WeakReference<L2ItemInstance> item;
	public L2Skill skill;
	public double value;
	
	public boolean isNoWeakPlayer()
	{
		return (player != null) && (player.get() != null);
	}
	
	public boolean isNoWeakTarget()
	{
		return (target != null) && (target.get() != null);
	}
	
	public boolean isNoWeakItem()
	{
		return (item != null) && (item.get() != null);
	}
}
