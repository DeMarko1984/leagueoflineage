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
package net.sf.l2j.inmem.chemas;

import java.io.Serializable;

/**
 * @author Домашний
 */
public class L2CharacterFriend implements Serializable
{
	private final int char_id;
	private final int friend_id;
	private final String friend_name;
	
	/**
	 * @return the char_id
	 */
	public final int getCharId()
	{
		return char_id;
	}
	
	/**
	 * @return the friend_id
	 */
	public final int getFriendId()
	{
		return friend_id;
	}
	
	/**
	 * @return the friend_name
	 */
	public final String getFriendName()
	{
		return friend_name;
	}
	
	private static final long serialVersionUID = -2544939474389326645L;
	
	public L2CharacterFriend(int _char_id, int _friend_id, String _friend_name)
	{
		char_id = _char_id;
		friend_id = _friend_id;
		friend_name = _friend_name;
	}
	
}
