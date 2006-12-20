/*
 *   JavaNativeCompiler - A Java to native compiler.
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

package ch.mtSystems.jnc.model.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SettingsMemory
{
	private static final File SETTINGS_FILE = new File(System.getProperty("user.home"), ".JNC.conf");
	private static final Pattern WIN_COMPILER_PATH_PATTERN = Pattern.compile("gcc-(\\d+?)-win");
	private static final Pattern LIN_COMPILER_PATH_PATTERN = Pattern.compile("gcc-(\\d+?)-lin");

	private boolean skipIntro = false;
	private boolean autoDetectCompilers = true;
	private String winCompilerPath, linCompilerPath; // null (not set) or a value with length > 0
	private String license; // null (not set) or a value with length > 0


	// -------------- public methods ---------------

	public boolean getSkipIntro()
	{
		return skipIntro;
	}

	public void setSkipIntro(boolean skip)
	{
		skipIntro = skip;
		save();
	}

	public boolean getAutoDetectCompilers()
	{
		return autoDetectCompilers;
	}

	public void setAutoDetectCompilers(boolean auto)
	{
		autoDetectCompilers = auto;
		save();
	}

	public String getWindowsCompilerPath()
	{
		if(autoDetectCompilers) return detectCompilerPath(WIN_COMPILER_PATH_PATTERN);
		return winCompilerPath;
	}

	public void setWindowsCompilerPath(String newPath)
	{
		winCompilerPath = (newPath == null || newPath.length() == 0) ? null : newPath;
		save();
	}

	public String getLinuxCompilerPath()
	{
		if(autoDetectCompilers) return detectCompilerPath(LIN_COMPILER_PATH_PATTERN);
		return linCompilerPath;
	}

	public void setLinuxCompilerPath(String newPath)
	{
		linCompilerPath = (newPath == null || newPath.length() == 0) ? null : newPath;
		save();
	}

	public String getLicense() { return license; }
	
	public void setLicense(String license)
	{
		this.license = (license == null || license.length() == 0) ? null : license;
		save();
	}


	// --------------- private methods ---------------

	private String detectCompilerPath(Pattern p)
	{
		int iCur = 0;
		String sCur = null;

		File[] fa = (new File(".")).listFiles();
		for(File f : fa)
		{
			if(!f.isDirectory()) continue;
			Matcher m = p.matcher(f.getName());
			if(!m.matches()) continue;

			int tmpCur = Integer.parseInt(m.group(1));
			if(tmpCur > iCur)
			{
				iCur = tmpCur;
				sCur = f.getName();
			}
		}

		return sCur;
	}
	
	private void save()
	{
		try
		{
			FileWriter fw = new FileWriter(SETTINGS_FILE);
			fw.write("skipIntro=" + (skipIntro ? "true" : "false") + "\n");
			fw.write("autoDetectCompilers=" + autoDetectCompilers + "\n");
			fw.write("winCompilerPath=" + winCompilerPath + "\n");
			fw.write("linCompilerPath=" + linCompilerPath + "\n");
			fw.write("license=" + license + "\n");
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
				if(sa.length != 2 || sa[1].equals("null") || sa[1].length() == 0) continue;

				     if(sa[0].equals("skipIntro"))           skipIntro = sa[1].equals("true");
				else if(sa[0].equals("autoDetectCompilers")) autoDetectCompilers = sa[1].equals("true");
				else if(sa[0].equals("winCompilerPath"))     winCompilerPath = sa[1];
				else if(sa[0].equals("linCompilerPath"))     linCompilerPath = sa[1];
				else if(sa[0].equals("license"))             license = sa[1];
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
