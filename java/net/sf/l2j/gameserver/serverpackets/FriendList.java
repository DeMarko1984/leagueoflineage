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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.inmem.L2JInMemDatabase;
import net.sf.l2j.inmem.chemas.L2CharacterFriend;

/**
 * Support for "Chat with Friends" dialog. Format: ch (hdSdh) h: Total Friend Count h: Unknown d: Player Object ID S: Friend Name d: Online/Offline h: Unknown
 * @author Tempy
 */
public class FriendList extends L2GameServerPacket
{
	// private static final Logger _log = Logger.getLogger(FriendList.class.getName());
	private static final String _S__FA_FRIENDLIST = "[S] FA FriendList";
	
	private final L2PcInstance _activeChar;
	
	public FriendList(L2PcInstance character)
	{
		_activeChar = character;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_activeChar == null)
		{
			return;
		}
		
		L2CharacterFriend[] friends = L2JInMemDatabase.getInstance().getFriends(_activeChar.getObjectId());
		
		writeC(0xfa);
		writeD(friends.length);
		
		for (L2CharacterFriend l2CharacterFriend : friends)
		{
			
			// writeH(0); // ??
			writeD(l2CharacterFriend.getFriendId());
			writeS(l2CharacterFriend.getFriendName());
			
			if (L2World.getInstance().getPlayer(l2CharacterFriend.getFriendName()) == null)
			{
				writeD(0); // offline
				writeD(0x00);
			}
			else
			{
				writeD(1); // online
				writeD(l2CharacterFriend.getFriendId());
			}
			
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FA_FRIENDLIST;
	}
}
