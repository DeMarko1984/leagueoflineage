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
package net.sf.l2j.gameserver.model.entity;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;

/**
 * @author Домашний
 */
public class L2CubicContainer
{
	private final L2CubicInstance[] _cubicts = new L2CubicInstance[3];
	
	/**
	 * @return
	 */
	public int size()
	{
		int size = 0;
		for (L2CubicInstance _cubict : _cubicts)
		{
			if (_cubict != null)
			{
				size++;
			}
		}
		return size;
	}
	
	/**
	 * @return
	 */
	public synchronized L2CubicInstance[] values()
	{
		List<L2CubicInstance> _toReturn = new ArrayList<>();
		for (L2CubicInstance _cubict : _cubicts)
		{
			if (_cubict != null)
			{
				_toReturn.add(_cubict);
			}
		}
		return _toReturn.toArray(new L2CubicInstance[_toReturn.size()]);
	}
	
	/**
	 * 
	 */
	public synchronized void clear()
	{
		for (int i = 0; i < _cubicts.length; i++)
		{
			_cubicts[i] = null;
		}
	}
	
	/**
	 * @param npcId
	 * @param cubic
	 */
	public synchronized void put(int npcId, L2CubicInstance cubic)
	{
		// check already added
		for (L2CubicInstance _cubict : _cubicts)
		{
			if (_cubict == null)
			{
				continue;
			}
			if (_cubict.getId() == npcId)
			{
				_cubict.stopAction();
				_cubict = null;
			}
		}
		
		// add to array
		for (int i = 0; i < _cubicts.length; i++)
		{
			if (_cubicts[i] == null)
			{
				_cubicts[i] = cubic;
				break;
			}
		}
	}
	
	/**
	 * @param npcId
	 */
	public synchronized void remove(int npcId)
	{
		for (L2CubicInstance _cubict : _cubicts)
		{
			if (_cubict == null)
			{
				continue;
			}
			if (_cubict.getId() == npcId)
			{
				_cubict = null;
			}
		}
	}
	
	/**
	 * @param npcId
	 * @return
	 */
	public synchronized L2CubicInstance get(int npcId)
	{
		for (L2CubicInstance _cubict : _cubicts)
		{
			if (_cubict == null)
			{
				continue;
			}
			if (_cubict.getId() == npcId)
			{
				return _cubict;
			}
		}
		return null;
	}
	
	/**
	 * @param npcId
	 * @return
	 */
	public boolean containsKey(int npcId)
	{
		for (L2CubicInstance _cubict : _cubicts)
		{
			if (_cubict == null)
			{
				continue;
			}
			if (_cubict.getId() == npcId)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return
	 */
	public synchronized Integer[] keySet()
	{
		List<Integer> _toReturn = new ArrayList<>();
		for (L2CubicInstance _cubict : _cubicts)
		{
			if (_cubict != null)
			{
				_toReturn.add(_cubict.getId());
			}
		}
		return _toReturn.toArray(new Integer[_toReturn.size()]);
	}
	
}
