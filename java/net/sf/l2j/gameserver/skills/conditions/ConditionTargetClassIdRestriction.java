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
package net.sf.l2j.gameserver.skills.conditions;

import java.util.ArrayList;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionTargetClassIdRestriction extends Condition
{
	
	private final ArrayList<Integer> _classIds;
	
	public ConditionTargetClassIdRestriction(ArrayList<Integer> classId)
	{
		_classIds = classId;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!env.isNoWeakTarget())
		{
			return true;
		}
		if (!(env.target.get() instanceof L2PcInstance))
		{
			return true;
		}
		return (!_classIds.contains(((L2PcInstance) env.target.get()).getClassId().getId()));
	}
}
