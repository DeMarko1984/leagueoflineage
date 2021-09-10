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
package net.sf.l2j.gameserver.lib;

/**
 * @author Домашний
 */
public class FormulasConfig
{
	
	private int MaxStatValue;
	private double[] STRCompute;
	private double[] INTCompute;
	private double[] DEXCompute;
	private double[] WITCompute;
	private double[] CONCompute;
	private double[] MENCompute;
	private double MaxHPMod;
	private double MaxMPMod;
	private double MaxCPMod;
	
	/**
	 * @param maxStatValue
	 * @param sTRCompute
	 * @param iNTCompute
	 * @param dEXCompute
	 * @param wITCompute
	 * @param cONCompute
	 * @param mENCompute
	 * @param _MaxHPMod
	 * @param _MaxMPMod
	 * @param _MaxCPMod
	 */
	public FormulasConfig(int maxStatValue, double[] sTRCompute, double[] iNTCompute, double[] dEXCompute, double[] wITCompute, double[] cONCompute, double[] mENCompute, double _MaxHPMod,
		double _MaxMPMod, double _MaxCPMod)
	{
		super();
		MaxStatValue = maxStatValue;
		STRCompute = sTRCompute;
		INTCompute = iNTCompute;
		DEXCompute = dEXCompute;
		WITCompute = wITCompute;
		CONCompute = cONCompute;
		MENCompute = mENCompute;
		MaxHPMod = _MaxHPMod;
		MaxMPMod = _MaxMPMod;
		MaxCPMod = _MaxCPMod;
	}
	
	/**
	 * Class for searalization
	 */
	public FormulasConfig()
	{
		super();
		
		MaxStatValue = 120;
		
		STRCompute = new double[]
		{
			1.036,
			34.845
		};
		INTCompute = new double[]
		{
			1.020,
			31.375
		};
		DEXCompute = new double[]
		{
			1.009,
			19.360
		};
		WITCompute = new double[]
		{
			1.050,
			20.000
		};
		CONCompute = new double[]
		{
			1.030,
			27.632
		};
		MENCompute = new double[]
		{
			1.010,
			-0.060
		};
		MaxHPMod = 1;
		MaxMPMod = 1;
		MaxCPMod = 1;
	}
	
	/**
	 * @return the maxStatValue
	 */
	public final int getMaxStatValue()
	{
		return MaxStatValue;
	}
	
	/**
	 * @return the sTRCompute
	 */
	public final double[] getSTRCompute()
	{
		return STRCompute;
	}
	
	/**
	 * @return the iNTCompute
	 */
	public final double[] getINTCompute()
	{
		return INTCompute;
	}
	
	/**
	 * @return the dEXCompute
	 */
	public final double[] getDEXCompute()
	{
		return DEXCompute;
	}
	
	/**
	 * @return the wITCompute
	 */
	public final double[] getWITCompute()
	{
		return WITCompute;
	}
	
	/**
	 * @return the cONCompute
	 */
	public final double[] getCONCompute()
	{
		return CONCompute;
	}
	
	/**
	 * @return the mENCompute
	 */
	public final double[] getMENCompute()
	{
		return MENCompute;
	}
	
	/**
	 * @param maxStatValue the maxStatValue to set
	 */
	public final void setMaxStatValue(int maxStatValue)
	{
		MaxStatValue = maxStatValue;
	}
	
	/**
	 * @param sTRCompute the sTRCompute to set
	 */
	public final void setSTRCompute(double[] sTRCompute)
	{
		STRCompute = sTRCompute;
	}
	
	/**
	 * @param iNTCompute the iNTCompute to set
	 */
	public final void setINTCompute(double[] iNTCompute)
	{
		INTCompute = iNTCompute;
	}
	
	/**
	 * @param dEXCompute the dEXCompute to set
	 */
	public final void setDEXCompute(double[] dEXCompute)
	{
		DEXCompute = dEXCompute;
	}
	
	/**
	 * @param wITCompute the wITCompute to set
	 */
	public final void setWITCompute(double[] wITCompute)
	{
		WITCompute = wITCompute;
	}
	
	/**
	 * @param cONCompute the cONCompute to set
	 */
	public final void setCONCompute(double[] cONCompute)
	{
		CONCompute = cONCompute;
	}
	
	/**
	 * @param mENCompute the mENCompute to set
	 */
	public final void setMENCompute(double[] mENCompute)
	{
		MENCompute = mENCompute;
	}
	
	/**
	 * @return the maxHPMod
	 */
	public final double getMaxHPMod()
	{
		return MaxHPMod;
	}
	
	/**
	 * @param maxHPMod the maxHPMod to set
	 */
	public final void setMaxHPMod(double maxHPMod)
	{
		MaxHPMod = maxHPMod;
	}
	
	/**
	 * @return the maxMPMod
	 */
	public final double getMaxMPMod()
	{
		return MaxMPMod;
	}
	
	/**
	 * @param maxMPMod the maxMPMod to set
	 */
	public final void setMaxMPMod(double maxMPMod)
	{
		MaxMPMod = maxMPMod;
	}
	
	/**
	 * @return the maxCPMod
	 */
	public final double getMaxCPMod()
	{
		return MaxCPMod;
	}
	
	/**
	 * @param maxCPMod the maxCPMod to set
	 */
	public final void setMaxCPMod(double maxCPMod)
	{
		MaxCPMod = maxCPMod;
	}
	
}
