/*
 *   JavaCompiler - A java to native compiler for Windows and Linux.
 *   Copyright (C) 2006  Marco Trudel <mtrudel@gmx.ch>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ch.mtSystems.javaCompiler.model.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


public class SettingsMemory
{
	private static final File SETTINGS_FILE = new File(System.getProperty("user.home"), "JavaCompilerSettings.txt");

	private String javac;


	public String getJavac() { return javac; }

	public void setJavac(String javac)
	{
		this.javac = javac;
		save();
	}


	// --------------- private methods ---------------

	private void save()
	{
		try
		{
			FileWriter fw = new FileWriter(SETTINGS_FILE);
			fw.write("javaHome=" + javac);
			fw.flush();
			fw.close();
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void load()
	{
		if(!SETTINGS_FILE.exists()) return;

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(SETTINGS_FILE));
			String line;

			while((line = br.readLine()) != null)
			{
				String[] sa = line.split("=", 2);
				if(sa.length != 2) continue;

				if(sa[0].equals("javaHome")) javac = sa[1];
			}

			br.close();
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}


	// --------------- singleton pattern ---------------

	/**
	 * Returns the SettingsMemory instance.
	 */
	public static SettingsMemory getSettingsMemory()
	{
		if(settingsMemory == null)
		{
			settingsMemory = new SettingsMemory();
			settingsMemory.load();
		}

		return settingsMemory;
	}

	private SettingsMemory() { }
	private static SettingsMemory settingsMemory;
}
