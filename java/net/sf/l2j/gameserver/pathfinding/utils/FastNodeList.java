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
package net.sf.l2j.gameserver.pathfinding.utils;

import java.util.ArrayList;

import net.sf.l2j.gameserver.pathfinding.AbstractNode;

/**
 * @author -Nemesiss-
 */
public class FastNodeList
{
	private final ArrayList<AbstractNode<?>> _list;
	
	public FastNodeList(int size)
	{
		_list = new ArrayList<>(size);
	}
	
	public void add(AbstractNode<?> n)
	{
		_list.add(n);
	}
	
	public boolean contains(AbstractNode<?> n)
	{
		return _list.contains(n);
	}
	
	public boolean containsRev(AbstractNode<?> n)
	{
		return _list.lastIndexOf(n) != -1;
	}
}
