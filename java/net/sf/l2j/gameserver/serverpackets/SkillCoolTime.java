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

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TimeStamp;

public class SkillCoolTime extends L2GameServerPacket
{
	private static final String _S__C1_SKILLCOOLTIME = "[S] C1 SkillCoolTime";
	
	private final L2PcInstance activeChar;
	
	public SkillCoolTime(L2PcInstance _activeChar)
	{
		activeChar = _activeChar;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (activeChar == null)
		{
			return;
		}
		
		writeC(193);
		writeD(activeChar.getReuseTimeStamps().size()); // list size
		for (TimeStamp ts : activeChar.getReuseTimeStamps())
		{
			writeD(ts.getSkillId());
			writeD(0x00);
			writeD(Math.round(ts.getReuseDelay() / 1000.f));
			writeD(Math.round(ts.getRemaining() / 1000.f));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__C1_SKILLCOOLTIME;
	}
	
}
