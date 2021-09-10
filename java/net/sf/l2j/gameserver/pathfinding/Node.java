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
package net.sf.l2j.gameserver.pathfinding;

import net.sf.l2j.gameserver.pathfinding.geonodes.GeoPathFinding;

/**
 * @author Домашний
 */
public abstract class Node
{
	private final int _neighborsIdx;
	private Node[] _neighbors;
	private Node _parent;
	private short _cost;
	
	protected Node(final int neighborsIdx)
	{
		_neighborsIdx = neighborsIdx;
	}
	
	public final void setParent(final Node p)
	{
		_parent = p;
	}
	
	public final void setCost(final int cost)
	{
		_cost = (short) cost;
	}
	
	public final void attachNeighbors()
	{
		_neighbors = GeoPathFinding.getInstance().readNeighbors(this, _neighborsIdx);
	}
	
	public final Node[] getNeighbors()
	{
		return _neighbors;
	}
	
	public final Node getParent()
	{
		return _parent;
	}
	
	public final short getCost()
	{
		return _cost;
	}
	
	public abstract int getX();
	
	public abstract int getY();
	
	public abstract short getZ();
	
	public abstract void setZ(short z);
	
	public abstract int getNodeX();
	
	public abstract int getNodeY();
	
	@Override
	public final int hashCode()
	{
		return hash((getNodeX() << 20) + (getNodeY() << 8) + getZ());
	}
	
	@Override
	public final boolean equals(final Object obj)
	{
		if (!(obj instanceof Node))
		{
			return false;
		}
		
		final Node n = (Node) obj;
		
		return (getNodeX() == n.getNodeX()) && (getNodeY() == n.getNodeY()) && (getZ() == n.getZ());
	}
	
	public final int hash(int h)
	{
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}
}