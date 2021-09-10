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
package net.sf.l2j.gameserver;

import java.util.Collection;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;

/**
 * @author Домашний
 */
public class IdleConnectionsUpdateTask implements Runnable
{
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
		for (L2PcInstance l2PcInstance : players)
		{
			if (l2PcInstance == null)
			{
				continue;
			}
			L2GameClient client = l2PcInstance.getClient();
			if (client == null)
			{
				continue;
			}
			if ((GameTimeController.getGameTicks() - client.packetsSentStartTick) > 1000)
			{
				client.sendPacket(new ActionFailed());
			}
		}
	}
	
}
