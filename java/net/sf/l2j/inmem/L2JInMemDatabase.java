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
package net.sf.l2j.inmem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.inmem.chemas.L2CharacterFriend;

/**
 * @author Домашний
 */
public class L2JInMemDatabase
{
	private static L2JInMemDatabase _instance;
	private final Map<Integer, List<L2CharacterFriend>> _character_friends;
	private boolean _character_friends_changed;
	
	public static L2JInMemDatabase getInstance()
	{
		if (_instance == null)
		{
			_instance = new L2JInMemDatabase();
		}
		return _instance;
	}
	
	public L2JInMemDatabase()
	{
		// character friend block
		_character_friends = new HashMap<>();
		restoreCharacterFriend();
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> L2JInMemDatabase.getInstance().storeCharacterFriend(false), 360, 360);
		// character friend block
	}
	
	public void restoreCharacterFriend()
	{
		if (!InMemDatabaseConfigs.CHARACTER_FRIENDS.getFile().exists())
		{
			return;
		}
		
		synchronized (_character_friends)
		{
			_character_friends.clear();
			
			FileInputStream fileInputStream;
			ObjectInputStream objectInputStream = null;
			try
			{
				fileInputStream = new FileInputStream(InMemDatabaseConfigs.CHARACTER_FRIENDS.getFile());
				objectInputStream = new ObjectInputStream(fileInputStream);
				
				// read count
				int countObjects = objectInputStream.readInt();
				for (int i = 0; i < countObjects; i++)
				{
					int subCountObjects = objectInputStream.readInt();
					for (int j = 0; j < subCountObjects; j++)
					{
						L2CharacterFriend restoredObject = (L2CharacterFriend) objectInputStream.readObject();
						if (!_character_friends.containsKey(restoredObject.getCharId()))
						{
							_character_friends.put(restoredObject.getCharId(), new ArrayList<>());
						}
						_character_friends.get(restoredObject.getCharId()).add(restoredObject);
					}
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
			try
			{
				objectInputStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void storeCharacterFriend(boolean force)
	{
		if (!isCharacterFriendsChanged() && !force)
		{
			return;
		}
		
		synchronized (_character_friends)
		{
			FileOutputStream outputStream;
			ObjectOutputStream objectOutputStream = null;
			try
			{
				outputStream = new FileOutputStream(InMemDatabaseConfigs.CHARACTER_FRIENDS.getFile());
				objectOutputStream = new ObjectOutputStream(outputStream);
				// write count
				objectOutputStream.writeInt(_character_friends.size());
				// write objects
				for (List<L2CharacterFriend> storeObjects : _character_friends.values())
				{
					objectOutputStream.writeInt(storeObjects.size());
					for (L2CharacterFriend l2CharacterFriend : storeObjects)
					{
						objectOutputStream.writeObject(l2CharacterFriend);
					}
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
			
			try
			{
				objectOutputStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public boolean isCharacterFriendsChanged()
	{
		return _character_friends_changed;
	}
	
	public void setCharacterFriendsCchanged()
	{
		_character_friends_changed = true;
	}
	
	public void AddCharacterFriend(int CharObjectId, int FriendObjectId, String FriendName)
	{
		synchronized (_character_friends)
		{
			if (!_character_friends.containsKey(CharObjectId))
			{
				_character_friends.put(CharObjectId, new ArrayList<>());
			}
			_character_friends_changed = _character_friends.get(CharObjectId).add(new L2CharacterFriend(CharObjectId, FriendObjectId, FriendName));
		}
	}
	
	public boolean IsCharacterFriend(int CharObjectId, String FriendName)
	{
		boolean IsCharacterFriend = false;
		synchronized (_character_friends)
		{
			if (_character_friends.containsKey(CharObjectId))
			{
				List<L2CharacterFriend> friends = _character_friends.get(CharObjectId);
				for (L2CharacterFriend l2CharacterFriend : friends)
				{
					if (l2CharacterFriend.getFriendName().toLowerCase() == FriendName.toLowerCase())
					{
						IsCharacterFriend = true;
						break;
					}
				}
			}
		}
		return IsCharacterFriend;
	}
	
	public void RemoveCaracterFriend(int CharObjectId, String FriendName)
	{
		synchronized (_character_friends)
		{
			if (_character_friends.containsKey(CharObjectId))
			{
				List<L2CharacterFriend> friends = _character_friends.get(CharObjectId);
				for (L2CharacterFriend l2CharacterFriend : friends)
				{
					if (l2CharacterFriend.getFriendName().toLowerCase() == FriendName.toLowerCase())
					{
						_character_friends_changed = friends.remove(l2CharacterFriend);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * @param objectId
	 * @return
	 */
	public L2CharacterFriend[] getFriends(int objectId)
	{
		L2CharacterFriend[] toReturn = new L2CharacterFriend[0];
		synchronized (_character_friends)
		{
			if (_character_friends.containsKey(objectId))
			{
				List<L2CharacterFriend> fiends = _character_friends.get(objectId);
				toReturn = fiends.toArray(new L2CharacterFriend[fiends.size()]);
			}
		}
		return toReturn;
	}
	
	/**
	 * @param CharObjectId
	 * @param friendId
	 * @return
	 */
	public boolean IsCharacterFriend(int CharObjectId, int friendId)
	{
		boolean IsCharacterFriend = false;
		synchronized (_character_friends)
		{
			if (_character_friends.containsKey(CharObjectId))
			{
				List<L2CharacterFriend> friends = _character_friends.get(CharObjectId);
				for (L2CharacterFriend l2CharacterFriend : friends)
				{
					if (l2CharacterFriend.getFriendId() == friendId)
					{
						IsCharacterFriend = true;
						break;
					}
				}
			}
		}
		return IsCharacterFriend;
	}
}
