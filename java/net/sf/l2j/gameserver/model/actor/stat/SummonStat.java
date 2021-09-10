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
package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class SummonStat extends PlayableStat
{
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public SummonStat(L2Summon activeChar)
	{
		super(activeChar);
	}
	
	// =========================================================
	// Method - Public
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	@Override
	public L2Summon getActiveChar()
	{
		return (L2Summon) super.getActiveChar();
	}
	
	/**
	 * @param HennaType
	 * @return
	 */
	protected double getAddModificator(String HennaType)
	{
		final L2Summon activeChar = getActiveChar();
		if (activeChar == null)
		{
			return 1;
		}
		final L2PcInstance owner = activeChar.getOwner();
		if (owner == null)
		{
			return 1;
		}
		double addModificator = 1.0;
		switch (HennaType)
		{
			case "con":
				addModificator += owner.getAddCON() / 10.0;
				break;
			case "men":
				addModificator += owner.getAddMEN() / 10.0;
				break;
			case "wit":
				addModificator += owner.getAddWIT() / 10.0;
				break;
			case "int":
				addModificator += owner.getAddINT() / 10.0;
				break;
			case "dex":
				addModificator += owner.getAddDEX() / 10.0;
				break;
			case "str":
				addModificator += owner.getAddSTR() / 10.0;
				break;
			default:
				break;
		}
		return addModificator;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.stat.CharStat#getMaxHp()
	 */
	@Override
	public int getMaxHp()
	{
		return (int) (super.getMaxHp() * getAddModificator("con"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.stat.CharStat#getMaxMp()
	 */
	@Override
	public int getMaxMp()
	{
		return (int) (super.getMaxMp() * getAddModificator("men"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.stat.CharStat#getMAtk(net.sf.l2j.gameserver.model.L2Character, net.sf.l2j.gameserver.model.L2Skill)
	 */
	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return (int) (super.getMAtk(target, skill) * getAddModificator("int"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.stat.CharStat#getMAtkSpd()
	 */
	@Override
	public int getMAtkSpd()
	{
		return (int) (super.getMAtkSpd() * getAddModificator("wit"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.stat.CharStat#getMDef(net.sf.l2j.gameserver.model.L2Character, net.sf.l2j.gameserver.model.L2Skill)
	 */
	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		return (int) (super.getMDef(target, skill) * getAddModificator("men"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.stat.CharStat#getPAtk(net.sf.l2j.gameserver.model.L2Character)
	 */
	@Override
	public int getPAtk(L2Character target)
	{
		return (int) (super.getPAtk(target) * getAddModificator("str"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.stat.CharStat#getPAtkSpd()
	 */
	@Override
	public int getPAtkSpd()
	{
		return (int) (super.getPAtkSpd() * getAddModificator("dex"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.stat.CharStat#getPDef(net.sf.l2j.gameserver.model.L2Character)
	 */
	@Override
	public int getPDef(L2Character target)
	{
		return (int) (super.getPDef(target) * getAddModificator("con"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.actor.stat.CharStat#getRunSpeed()
	 */
	@Override
	public int getRunSpeed()
	{
		return (int) (super.getRunSpeed() * getAddModificator("dex"));
	}
	
}
