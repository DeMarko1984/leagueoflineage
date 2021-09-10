/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.l2j.gameserver.model.L2Object;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @param <T>
 */
public class WorldObjectSet<T extends L2Object> extends L2ObjectSet<T>
{
	private final HashMap<Integer, T> _objectMap;
	private final Object _lockObject = new Object();
	
	public WorldObjectSet()
	{
		_objectMap = new HashMap<>();
	}
	
	@Override
	public int size()
	{
		int size;
		synchronized (_lockObject)
		{
			size = _objectMap.size();
		}
		return size;
	}
	
	@Override
	public boolean isEmpty()
	{
		boolean empty;
		synchronized (_lockObject)
		{
			empty = _objectMap.isEmpty();
		}
		return empty;
	}
	
	@Override
	public void clear()
	{
		synchronized (_lockObject)
		{
			_objectMap.clear();
		}
	}
	
	@Override
	public void put(T obj)
	{
		if (obj == null)
		{
			return;
		}
		synchronized (_lockObject)
		{
			_objectMap.put(obj.getObjectId(), obj);
		}
	}
	
	@Override
	public void remove(T obj)
	{
		if (obj == null)
		{
			return;
		}
		synchronized (_lockObject)
		{
			_objectMap.remove(obj.getObjectId());
		}
	}
	
	@Override
	public boolean contains(T obj)
	{
		if (obj == null)
		{
			return false;
		}
		boolean b;
		synchronized (_lockObject)
		{
			b = _objectMap.get(obj.getObjectId()) != null;
		}
		return b;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		List<T> _n = new ArrayList<>();
		
		synchronized (_lockObject)
		{
			Collection<T> iterator = _objectMap.values();
			for (T t : iterator)
			{
				if (t != null)
				{
					_n.add(t);
				}
			}
		}
		
		return _n.iterator();
	}
}
