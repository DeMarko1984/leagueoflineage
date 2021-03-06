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

import java.lang.ref.WeakReference;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;

public class CharStat
{
	// =========================================================
	// Data Field
	private final WeakReference<L2Character> _activeChar;
	private volatile long _exp = 0;
	private volatile int _sp = 0;
	private volatile byte _level = 1;
	
	// =========================================================
	// Constructor
	public CharStat(L2Character activeChar)
	{
		_activeChar = new WeakReference<>(activeChar);
	}
	
	// =========================================================
	// Method - Public
	/**
	 * Calculate the new value of the state with modifiers that will be applied on the targeted L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...) : <BR>
	 * <BR>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <B>_order</B>. Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order. The result of the calculation is stored in
	 * the value property of an Env class instance.<BR>
	 * <BR>
	 * @param stat The stat to calculate the new value with modifiers
	 * @param init The initial value of the stat before applying modifiers
	 * @param target The L2Charcater whose properties will be used in the calculation (ex : CON, INT...)
	 * @param skill The L2Skill whose properties will be used in the calculation (ex : Level...)
	 * @return
	 */
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return init;
		}
		
		int id = stat.ordinal();
		
		Calculator c = _activeChar.get().getCalculators()[id];
		
		// If no Func object found, no modifier is applied
		if ((c == null) || (c.size() == 0))
		{
			return init;
		}
		
		// Create and init an Env object to pass parameters to the Calculator
		Env env = new Env();
		env.player = _activeChar;
		env.target = new WeakReference<>(target);
		env.skill = skill;
		env.value = init;
		
		// Launch the calculation
		c.calc(env);
		// avoid some troubles with negative stats (some stats should never be
		// negative)
		if ((env.value <= 0) && ((stat == Stats.MAX_HP) || (stat == Stats.MAX_MP) || (stat == Stats.MAX_CP) || (stat == Stats.MAGIC_DEFENCE) || (stat == Stats.POWER_DEFENCE)
			|| (stat == Stats.POWER_ATTACK) || (stat == Stats.MAGIC_ATTACK) || (stat == Stats.POWER_ATTACK_SPEED) || (stat == Stats.MAGIC_ATTACK_SPEED) || (stat == Stats.SHIELD_DEFENCE)
			|| (stat == Stats.STAT_CON) || (stat == Stats.STAT_DEX) || (stat == Stats.STAT_INT) || (stat == Stats.STAT_MEN) || (stat == Stats.STAT_STR) || (stat == Stats.STAT_WIT)))
		{
			env.value = 1;
		}
		
		return env.value;
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	/**
	 * Return the Accuracy (base+modifier) of the L2Character in function of the Weapon Expertise Penalty.
	 * @return
	 */
	public int getAccuracy()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 0;
		}
		
		return (int) (calcStat(Stats.ACCURACY_COMBAT, 0, null, null) / _activeChar.get().getWeaponExpertisePenalty());
	}
	
	public L2Character getActiveChar()
	{
		return _activeChar.get();
	}
	
	/**
	 * Return the Attack Speed multiplier (base+modifier) of the L2Character to get proper animations.
	 * @return
	 */
	public final float getAttackSpeedMultiplier()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (float) (((1.1) * getPAtkSpd()) / _activeChar.get().getTemplate().basePAtkSpd);
	}
	
	/**
	 * Return the CON of the L2Character (base+modifier).
	 * @return
	 */
	public int getCON()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_CON, _activeChar.get().getTemplate().baseCON, null, null);
	}
	
	/**
	 * Return the Critical Damage rate (base+modifier) of the L2Character.
	 * @param target
	 * @param init
	 * @return
	 */
	public final double getCriticalDmg(L2Character target, double init)
	{
		return calcStat(Stats.CRITICAL_DAMAGE, init, target, null);
	}
	
	/**
	 * Return the Critical Hit rate (base+modifier) of the L2Character.
	 * @param target
	 * @param skill
	 * @return
	 */
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		int criticalHit = (int) calcStat(Stats.CRITICAL_RATE, _activeChar.get().getTemplate().baseCritRate, target, skill);
		
		// Set a cap of Critical Hit at 500
		if (criticalHit > 500)
		{
			criticalHit = 500;
		}
		
		return criticalHit;
	}
	
	/**
	 * Return the DEX of the L2Character (base+modifier).
	 * @return
	 */
	public int getDEX()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_DEX, _activeChar.get().getTemplate().baseDEX, null, null);
	}
	
	/**
	 * Return the Attack Evasion rate (base+modifier) of the L2Character.
	 * @param target
	 * @return
	 */
	public int getEvasionRate(L2Character target)
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) (calcStat(Stats.EVASION_RATE, 0, target, null) / _activeChar.get().getArmourExpertisePenalty());
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public void setExp(long value)
	{
		_exp = value;
	}
	
	/**
	 * Return the INT of the L2Character (base+modifier).
	 * @return
	 */
	public int getINT()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_INT, _activeChar.get().getTemplate().baseINT, null, null);
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public void setLevel(byte value)
	{
		_level = value;
	}
	
	/**
	 * Return the Magical Attack range (base+modifier) of the L2Character.
	 * @param skill
	 * @return
	 */
	public final int getMagicalAttackRange(L2Skill skill)
	{
		if (skill != null)
		{
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		}
		
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return _activeChar.get().getTemplate().baseAtkRange;
	}
	
	public final int getMaxCp()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_CP, _activeChar.get().getTemplate().baseCpMax, null, null);
	}
	
	public int getMaxHp()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_HP, _activeChar.get().getTemplate().baseHpMax, null, null);
	}
	
	public int getMaxMp()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_MP, _activeChar.get().getTemplate().baseMpMax, null, null);
	}
	
	/**
	 * Return the MAtk (base+modifier) of the L2Character for a skill used in function of abnormal effects in progress.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Calculate Magic damage</li> <BR>
	 * <BR>
	 * @param target The L2Character targeted by the skill
	 * @param skill The L2Skill used against the target
	 * @return
	 */
	public int getMAtk(L2Character target, L2Skill skill)
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		float bonusAtk = 1;
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.get().isChampion())
		{
			bonusAtk = Config.L2JMOD_CHAMPION_ATK;
		}
		double attack = _activeChar.get().getTemplate().baseMAtk * bonusAtk;
		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		Stats stat = skill == null ? null : skill.getStat();
		
		if (stat != null)
		{
			switch (stat)
			{
				case AGGRESSION:
					attack += _activeChar.get().getTemplate().baseAggression;
					break;
				case BLEED:
					attack += _activeChar.get().getTemplate().baseBleed;
					break;
				case POISON:
					attack += _activeChar.get().getTemplate().basePoison;
					break;
				case STUN:
					attack += _activeChar.get().getTemplate().baseStun;
					break;
				case ROOT:
					attack += _activeChar.get().getTemplate().baseRoot;
					break;
				case MOVEMENT:
					attack += _activeChar.get().getTemplate().baseMovement;
					break;
				case CONFUSION:
					attack += _activeChar.get().getTemplate().baseConfusion;
					break;
				case SLEEP:
					attack += _activeChar.get().getTemplate().baseSleep;
					break;
				case FIRE:
					attack += _activeChar.get().getTemplate().baseFire;
					break;
				case WIND:
					attack += _activeChar.get().getTemplate().baseWind;
					break;
				case WATER:
					attack += _activeChar.get().getTemplate().baseWater;
					break;
				case EARTH:
					attack += _activeChar.get().getTemplate().baseEarth;
					break;
				case HOLY:
					attack += _activeChar.get().getTemplate().baseHoly;
					break;
				case DARK:
					attack += _activeChar.get().getTemplate().baseDark;
					break;
			}
		}
		
		// Add the power of the skill to the attack effect
		if (skill != null)
		{
			attack += skill.getPower();
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}
	
	/**
	 * Return the MAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
	 * @return
	 */
	public int getMAtkSpd()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		float bonusSpdAtk = 1;
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.get().isChampion())
		{
			bonusSpdAtk = Config.L2JMOD_CHAMPION_SPD_ATK;
		}
		double val = calcStat(Stats.MAGIC_ATTACK_SPEED, _activeChar.get().getTemplate().baseMAtkSpd * bonusSpdAtk, null, null);
		val /= _activeChar.get().getArmourExpertisePenalty();
		return (int) val;
	}
	
	/**
	 * Return the Magic Critical Hit rate (base+modifier) of the L2Character.
	 * @param target
	 * @param skill
	 * @return
	 */
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		double mrate = calcStat(Stats.MCRITICAL_RATE, 5, target, skill);
		return (int) mrate;
	}
	
	/**
	 * Return the MDef (base+modifier) of the L2Character against a skill in function of abnormal effects in progress.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Calculate Magic damage</li> <BR>
	 * @param target The L2Character targeted by the skill
	 * @param skill The L2Skill used against the target
	 * @return
	 */
	public int getMDef(L2Character target, L2Skill skill)
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		// Get the base MAtk of the L2Character
		double defence = _activeChar.get().getTemplate().baseMDef;
		
		// Calculate modifier for Raid Bosses
		if (_activeChar.get().isRaid())
		{
			defence *= Config.RAID_DEFENCE_MULTIPLIER;
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}
	
	/**
	 * Return the MEN of the L2Character (base+modifier).
	 * @return
	 */
	public int getMEN()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_MEN, _activeChar.get().getTemplate().baseMEN, null, null);
	}
	
	public final float getMovementSpeedMultiplier()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (getRunSpeed() * 1f) / _activeChar.get().getTemplate().baseRunSpd;
	}
	
	/**
	 * Return the RunSpeed (base+modifier) or WalkSpeed (base+modifier) of the L2Character in function of the movement type.
	 * @return
	 */
	public final float getMoveSpeed()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		if (_activeChar.get().isRunning())
		{
			return getRunSpeed();
		}
		return getWalkSpeed();
	}
	
	/**
	 * Return the MReuse rate (base+modifier) of the L2Character.
	 * @param skill
	 * @return
	 */
	public final double getMReuseRate(L2Skill skill)
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return calcStat(Stats.MAGIC_REUSE_RATE, _activeChar.get().getTemplate().baseMReuseRate, null, skill);
	}
	
	/**
	 * Return the PAtk (base+modifier) of the L2Character.
	 * @param target
	 * @return
	 */
	public int getPAtk(L2Character target)
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		float bonusAtk = 1;
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.get().isChampion())
		{
			bonusAtk = Config.L2JMOD_CHAMPION_ATK;
		}
		return (int) calcStat(Stats.POWER_ATTACK, _activeChar.get().getTemplate().basePAtk * bonusAtk, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against animals.
	 * @param target
	 * @return
	 */
	public final double getPAtkAnimals(L2Character target)
	{
		return calcStat(Stats.PATK_ANIMALS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against dragons.
	 * @param target
	 * @return
	 */
	public final double getPAtkDragons(L2Character target)
	{
		return calcStat(Stats.PATK_DRAGONS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against insects.
	 * @param target
	 * @return
	 */
	public final double getPAtkInsects(L2Character target)
	{
		return calcStat(Stats.PATK_INSECTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against monsters.
	 * @param target
	 * @return
	 */
	public final double getPAtkMonsters(L2Character target)
	{
		return calcStat(Stats.PATK_MONSTERS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against plants.
	 * @param target
	 * @return
	 */
	public final double getPAtkPlants(L2Character target)
	{
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
	 * @return
	 */
	public int getPAtkSpd()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		float bonusAtk = 1;
		if (Config.L2JMOD_CHAMPION_ENABLE && _activeChar.get().isChampion())
		{
			bonusAtk = Config.L2JMOD_CHAMPION_SPD_ATK;
		}
		return (int) (calcStat(Stats.POWER_ATTACK_SPEED, _activeChar.get().getTemplate().basePAtkSpd * bonusAtk, null, null) / _activeChar.get().getArmourExpertisePenalty());
	}
	
	/**
	 * Return the PAtk Modifier against undead.
	 * @param target
	 * @return
	 */
	public final double getPAtkUndead(L2Character target)
	{
		return calcStat(Stats.PATK_UNDEAD, 1, target, null);
	}
	
	public final double getPDefUndead(L2Character target)
	{
		return calcStat(Stats.PDEF_UNDEAD, 1, target, null);
	}
	
	/**
	 * Return the PDef (base+modifier) of the L2Character.
	 * @param target
	 * @return
	 */
	public int getPDef(L2Character target)
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(
			Stats.POWER_DEFENCE,
				(_activeChar.get().isRaid()) ? _activeChar.get().getTemplate().basePDef * Config.RAID_DEFENCE_MULTIPLIER : _activeChar.get().getTemplate().basePDef,
				target,
				null);
	}
	
	/**
	 * Return the Physical Attack range (base+modifier) of the L2Character.
	 * @return
	 */
	public final int getPhysicalAttackRange()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, _activeChar.get().getTemplate().baseAtkRange, null, null);
	}
	
	/**
	 * Return the Skill/Spell reuse modifier.
	 * @param target
	 * @return
	 */
	public final double getReuseModifier(L2Character target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}
	
	/**
	 * Return the RunSpeed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.
	 * @return
	 */
	public int getRunSpeed()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		// err we should be adding TO the persons run speed
		// not making it a constant
		int val = (int) calcStat(Stats.RUN_SPEED, _activeChar.get().getTemplate().baseRunSpd, null, null);
		
		if (_activeChar.get().isFlying())
		{
			val += Config.WYVERN_SPEED;
			return val;
		}
		if (_activeChar.get().isRiding())
		{
			val += Config.STRIDER_SPEED;
			return val;
		}
		val /= _activeChar.get().getArmourExpertisePenalty();
		return val;
	}
	
	/**
	 * Return the ShieldDef rate (base+modifier) of the L2Character.
	 * @return
	 */
	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public void setSp(int value)
	{
		_sp = value;
	}
	
	/**
	 * Return the STR of the L2Character (base+modifier).
	 * @return
	 */
	public int getSTR()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_STR, _activeChar.get().getTemplate().baseSTR, null, null);
	}
	
	/**
	 * Return the WalkSpeed (base+modifier) of the L2Character.
	 * @return
	 */
	public final int getWalkSpeed()
	{
		
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		if (_activeChar.get() instanceof L2PcInstance)
		{
			return (getRunSpeed() * 70) / 100;
		}
		return (int) calcStat(Stats.WALK_SPEED, _activeChar.get().getTemplate().baseWalkSpd, null, null);
		
	}
	
	/**
	 * Return the WIT of the L2Character (base+modifier).
	 * @return
	 */
	public int getWIT()
	{
		if ((_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_WIT, _activeChar.get().getTemplate().baseWIT, null, null);
	}
	
	/**
	 * Return the mpConsume.
	 * @param skill
	 * @return
	 */
	public final int getMpConsume(L2Skill skill)
	{
		if ((skill == null) || (_activeChar == null) || (_activeChar.get() == null))
		{
			return 1;
		}
		int mpconsume = skill.getMpConsume();
		if (skill.isDance() && (_activeChar.get() != null) && (_activeChar.get().getDanceCount() > 0))
		{
			mpconsume += _activeChar.get().getDanceCount() * skill.getNextDanceMpCost();
		}
		return (int) calcStat(Stats.MP_CONSUME, mpconsume, null, skill);
	}
	
	/**
	 * Return the mpInitialConsume.
	 * @param skill
	 * @return
	 */
	public final int getMpInitialConsume(L2Skill skill)
	{
		if (skill == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
	}
}
