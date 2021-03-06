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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.serverpackets.KeyPacket;

/**
 * This class ...
 * @version $Revision: 1.5.2.8.2.8 $ $Date: 2005/04/02 10:43:04 $
 */
public final class ProtocolVersion extends L2GameClientPacket
{
	private static final String _C__00_PROTOCOLVERSION = "[C] 00 ProtocolVersion";
	static Logger _log = Logger.getLogger(ProtocolVersion.class.getName());
	
	private int _version;
	
	@Override
	protected void readImpl()
	{
		try
		{
			_version = readD();
		}
		catch (Exception e)
		{
			_version = -2;
		}
	}
	
	@Override
	protected void runImpl()
	{
		// this packet is never encrypted
		L2GameClient client = getClient();
		if (_version == -2)
		{
			if (Config.DEBUG)
			{
				_log.info("Ping received");
			}
			// this is just a ping attempt from the new C2 client
			client.closeNow();
		}
		else if ((_version < Config.MIN_PROTOCOL_REVISION) || (_version > Config.MAX_PROTOCOL_REVISION))
		{
			_log
				.info(
					"Client: " + client.toString() + " -> Protocol Revision: " + _version + " is invalid. Minimum is " + Config.MIN_PROTOCOL_REVISION + " and Maximum is "
						+ Config.MAX_PROTOCOL_REVISION + " are supported. Closing connection.");
			_log.warning("Wrong Protocol Version " + _version);
			client.closeNow();
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.fine("Client Protocol Revision is ok: " + _version);
			}
			
			client.sendPacket(new KeyPacket(client.enableCrypt()));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__00_PROTOCOLVERSION;
	}
}
