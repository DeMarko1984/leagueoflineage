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
package net.sf.l2j.inmem;

import java.io.File;

import net.sf.l2j.Config;

/**
 * @author Домашний
 */
public enum InMemDatabaseConfigs
{
	CHARACTER_FRIENDS("data/character_friends.bin");
	
	private final File _file;
	
	InMemDatabaseConfigs(String fileName)
	{
		_file = new File(Config.DATAPACK_ROOT, fileName);
	}
	
	/**
	 * @return the _fileName
	 */
	public File getFile()
	{
		return _file;
	}
}
