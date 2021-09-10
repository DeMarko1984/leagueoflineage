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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.lge.LgeManager;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2TvTEventNpcInstance extends L2NpcInstance
{
	public L2TvTEventNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance playerInstance, String command)
	{
		if (command.startsWith("multisell"))
		{
			L2Multisell.getInstance().SeparateAndSend(Integer.parseInt(command.substring(9).trim()), playerInstance, false, getCastle().getTaxRate());
		}
		else
		{
			LgeManager.getInstance().onBypass(command, playerInstance);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance playerInstance, int val)
	{
		if (playerInstance == null)
		{
			return;
		}
		
		String htmFile = "data/html/mods/";
		if (!LgeManager.getInstance().isPlayerParticipant(playerInstance.getName()))
		{
			htmFile += "TvTEventParticipation";
		}
		else
		{
			htmFile += "TvTEventRemoveParticipation";
		}
		htmFile += ".htm";
		
		String htmContent = HtmCache.getInstance().getHtm(htmFile);
		
		if (htmContent != null)
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
			npcHtmlMessage.setHtml(htmContent);
			npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
			playerInstance.sendPacket(npcHtmlMessage);
		}
		
		playerInstance.sendPacket(new ActionFailed());
	}
}
