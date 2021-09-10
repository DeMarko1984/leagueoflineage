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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.inmem.L2JInMemDatabase;
import net.sf.l2j.inmem.chemas.L2CharacterFriend;

/**
 * This class ...
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestFriendList extends L2GameClientPacket
{
	private static final String _C__60_REQUESTFRIENDLIST = "[C] 60 RequestFriendList";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		SystemMessage sm;
		
		L2CharacterFriend[] friends = L2JInMemDatabase.getInstance().getFriends(activeChar.getObjectId());
		
		// ======<Friend List>======
		activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_HEAD));
		
		L2PcInstance friend = null;
		
		for (L2CharacterFriend _friend : friends)
		{
			String friendName = _friend.getFriendName();
			friend = L2World.getInstance().getPlayer(friendName);
			
			if (friend == null)
			{
				// (Currently: Offline)
				sm = new SystemMessage(SystemMessageId.S1_OFFLINE);
				sm.addString(friendName);
			}
			else
			{
				// (Currently: Online)
				sm = new SystemMessage(SystemMessageId.S1_ONLINE);
				sm.addString(friendName);
			}
			
			activeChar.sendPacket(sm);
		}
		
		// =========================
		activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
		sm = null;
	}
	
	@Override
	public String getType()
	{
		return _C__60_REQUESTFRIENDLIST;
	}
}
