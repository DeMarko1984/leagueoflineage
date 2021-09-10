/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillTeleportTo extends L2Skill
{
	
	private final int _x;
	private final int _y;
	private final int _z;
	
	public L2SkillTeleportTo(StatsSet set)
	{
		super(set);
		
		_x = set.getInteger("TeleportToX", 0);
		_y = set.getInteger("TeleportToY", 0);
		_z = set.getInteger("TeleportToZ", 0);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead() || !(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (player.isInLgeEvent())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_NOT_IN_EVENT_TO_USE_TELEPORT_SKILLS));
			return;
		}
		
		L2Summon summon = activeChar.getPet();
		
		if (summon != null)
		{
			summon.unSummon(player);
		}
		
		for (L2Effect effect : player.getAllEffects())
		{
			if (effect != null)
			{
				effect.exit();
			}
		}
		
		player.stopAbnormalEffect((short) 0x0400);
		player.setIsParalyzed(false);
		
		player.doRevive();
		player.setCurrentCp(player.getMaxCp());
		player.setCurrentHp(player.getMaxHp());
		player.setCurrentMp(player.getMaxMp());
		
		activeChar.teleToLocation(_x, _y, _z);
	}
	
}
