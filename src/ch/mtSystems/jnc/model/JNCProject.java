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

package ch.mtSystems.jnc.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import ch.mtSystems.jnc.control.IAppControllerListener;


public class JNCProject
{
	private Vector<IAppControllerListener> vListeners = new Vector<IAppControllerListener>();

	// the source
	private Set<File> lFiles = new LinkedHashSet<File>();
	private Set<File> lDirectories = new LinkedHashSet<File>();
	private HashMap<File, Boolean> hmJars = new HashMap<File, Boolean>();

	// basic settings
	private File mainClassRessource;
	private String mainClass;
	private String javaLibPath;
	private boolean useCni;

	private File windowsFile, linuxFile;
	private boolean compileWindows = true, compileLinux = true;
	private boolean omitStripping, omitPacking, disableOptimisation;

	// windows settings
	private File iconFile;
	private boolean useIcon, hideConsole;

	// advanced settings
	private HashMap<String, Boolean> hmGcjFlags = new HashMap<String, Boolean>();
	private boolean showCommands;
	private boolean excludeGui, excludeJce, addGnuRegex;
	private boolean dontCacheJars;
	
	// compile settings
	private boolean beepWhenDone;

	// save file
	private File saveFile;


	// --------------- public methods ---------------

	public boolean addFile(File f)
	{
		if(!lFiles.add(f)) return false;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
		return true;
	}

	public void removeFile(File f)
	{
		lFiles.remove(f);
		checkIfMainClassDeleted(f);
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public File[] getFiles()
	{
		return lFiles.toArray(new File[0]);
	}


	// ----- directory handling -----

	public boolean addDirectory(File dir)
	{
		if(!lDirectories.add(dir)) return false;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
		return true;
	}

	public void removeDirectory(File dir)
	{
		lDirectories.remove(dir);
		checkIfMainClassDeleted(dir);
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public File[] getDirectories()
	{
		return lDirectories.toArray(new File[0]);
	}


	// ----- Jar handling -----

	public boolean addJar(File f, boolean complete)
	{
		if(hmJars.put(f, complete) != null) return false;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
		return true;
	}

	public void removeJar(File f)
	{
		hmJars.remove(f);
		checkIfMainClassDeleted(f);
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public File[] getJars()
	{
		return hmJars.keySet().toArray(new File[0]);
	}

	public boolean getCompileCompleteJar(File f)
	{
		Boolean b = hmJars.get(f);
		if(b == null) throw new IllegalArgumentException("File \"" + f + "\" is no project jar!");
		return b;
	}
	
	public void setCompileCompleteJar(File f, boolean complete)
	{
		if(!hmJars.containsKey(f)) throw new IllegalArgumentException("File \"" + f + "\" is no project jar!");
		hmJars.put(f, complete);
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}


	// ----- main-class handling -----

	public void setMainClass(File mainClassRessource, String mainClass)
	{
		this.mainClassRessource = mainClassRessource;
		this.mainClass = mainClass;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public String getMainClass()
	{
		return mainClass;
	}

	public void setJavaLibPath(String javaLibPath)
	{
		this.javaLibPath = javaLibPath;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public String getJavaLibPath()
	{
		return javaLibPath;
	}

	public boolean getUseCni() { return useCni; }

	public void setUseCni(boolean useCni)
	{
		this.useCni = useCni;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public boolean getCompileWindows()
	{
		return compileWindows;
	}
	
	public void setCompileWindows(boolean compileWindows)
	{
		this.compileWindows = compileWindows;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public File getWindowsFile()
	{
		return windowsFile;
	}
	
	public void setWindowsFile(File windowsFile)
	{
		this.windowsFile = windowsFile;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}
	
	public boolean getCompileLinux()
	{
		return compileLinux;
	}
	
	public void setCompileLinux(boolean compileLinux)
	{
		this.compileLinux = compileLinux;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public File getLinuxFile()
	{
		return linuxFile;
	}
	
	public void setLinuxFile(File linuxFile)
	{
		this.linuxFile = linuxFile;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public boolean getOmitStripping() { return omitStripping; }

	public void setOmitStripping(boolean omit)
	{
		omitStripping = omit;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public boolean getOmitPacking() { return omitPacking; }

	public void setOmitPacking(boolean omit)
	{
		omitPacking = omit;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public boolean getDisableOptimisation() { return disableOptimisation; }
	
	public void setDisableOptimisation(boolean disableOptimisation)
	{
		this.disableOptimisation = disableOptimisation;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}
	
	public boolean getUseIcon() { return useIcon; }

	public void setUseIcon(boolean useIt)
	{
		useIcon = useIt;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public File getIconFile() { return iconFile; }

	public void setIconFile(File iconFile)
	{
		this.iconFile = iconFile;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public boolean getHideConsole() { return hideConsole; }

	public void setHideConsole(boolean hide)
	{
		hideConsole = hide;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}


	// ----- GCJ flags handling -----

	public boolean addGcjFlags(String flag, boolean mainCompilationOnly)
	{
		if(hmGcjFlags.put(flag, mainCompilationOnly) != null) return false;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
		return true;
	}

	public void removeGcjFlag(String flag)
	{
		hmGcjFlags.remove(flag);
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public String[] getGcjFlags()
	{
		return hmGcjFlags.keySet().toArray(new String[0]);
	}

	public boolean getFlagMainCompilationOnly(String flag)
	{
		Boolean b = hmGcjFlags.get(flag);
		if(b == null) throw new IllegalArgumentException("Flag \"" + flag + "\" not configured!");
		return b;
	}

	public void setFlagMainCompilationOnly(String flag, boolean mainCompilationOnly)
	{
		if(!hmGcjFlags.containsKey(flag)) throw new IllegalArgumentException("Flag \"" + flag + "\" not configured!");
		hmGcjFlags.put(flag, mainCompilationOnly);
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}


	// ----- show-commands handling -----

	public boolean getShowCommands()
	{
		return showCommands;
	}
	
	public void setShowCommands(boolean showCommands)
	{
		this.showCommands = showCommands;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}
	
	public boolean getExcludeGui()
	{
		return excludeGui;
	}
	
	public void setExcludeGui(boolean excludeGui)
	{
		this.excludeGui = excludeGui;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}
	
	public boolean getExcludeJce()
	{
		return excludeJce;
	}
	
	public void setExcludeJce(boolean excludeJce)
	{
		this.excludeJce = excludeJce;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}
	
	public boolean getAddGnuRegex()
	{
		return addGnuRegex;
	}
	
	public void setAddGnuRegex(boolean addGnuRegex)
	{
		this.addGnuRegex = addGnuRegex;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}
	
	public boolean getDontCacheJars()
	{
		return dontCacheJars;
	}
	
	public void setDontCacheJars(boolean dontCacheJars)
	{
		this.dontCacheJars = dontCacheJars;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}
	
	public boolean getBeepWhenDone() { return beepWhenDone; }

	public void setBeepWhenDone(boolean beep)
	{
		beepWhenDone = beep;
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public File getSaveFile() { return saveFile; }

	public void save(File f) throws IOException
	{
		saveFile = f;
		FileWriter fw = new FileWriter(f);

		// the source
		for(Iterator it=lFiles.iterator();       it.hasNext();) fw.write("file=" + it.next() + "\n");
		for(Iterator it=lDirectories.iterator(); it.hasNext();) fw.write("dir=" +  it.next() + "\n");
		for(File jarFile : getJars())
		{
			fw.write("jar=" + jarFile  + "," + hmJars.get(jarFile) + "\n");
		}

		// common settings
		fw.write("mainClass=" + mainClass + "#" + mainClassRessource + "\n");
		fw.write("javaLibPath=" + javaLibPath + "\n");
		fw.write("useCni=" + useCni + "\n");
		fw.write("compileWindows=" + compileWindows + "\n");
		fw.write("windowsFile=" + windowsFile + "\n");
		fw.write("compileLinux=" + compileLinux + "\n");
		fw.write("linuxFile=" + linuxFile + "\n");
		fw.write("omitStripping=" + omitStripping + "\n");
		fw.write("omitPacking=" + omitPacking + "\n");
		fw.write("disableOptimisation=" + disableOptimisation + "\n");

		// windows settings
		fw.write("iconFile=" + iconFile + "\n");
		fw.write("useIcon=" + useIcon + "\n");
		fw.write("hideConsole=" + hideConsole + "\n");

		// advanced settings
		for(String flag : getGcjFlags())
		{
			fw.write("gcjFlag=" + flag  + "," + hmGcjFlags.get(flag) + "\n");
		}
		fw.write("showCommands=" + showCommands + "\n");
		fw.write("excludeGui=" + excludeGui + "\n");
		fw.write("excludeJce=" + excludeJce + "\n");
		fw.write("addGnuRegex=" + addGnuRegex + "\n");
		fw.write("dontCacheJars=" + dontCacheJars + "\n");

		// compile settings
		fw.write("beepWhenDone=" + beepWhenDone);

		fw.flush();
		fw.close();

		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectSaved();
	}

	public static JNCProject open(File f) throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(f));
		JNCProject project = new JNCProject();
		project.saveFile = f;
		
		for(String line = br.readLine(); line != null; line = br.readLine())
		{
			String[] sa = line.split("=", 2);
			if(sa.length != 2) throw new IOException("Not a JNC project file!");

			     if(sa[1].equals("null")) continue; // ignore, defaults are null anyway
			else if(sa[0].equals("file")) project.lFiles.add(new File(sa[1]));
			else if(sa[0].equals("dir"))  project.lDirectories.add(new File(sa[1]));
			else if(sa[0].equals("jar"))
			{
				int index = sa[1].lastIndexOf(',');
				if(index < 0) throw new IOException("Not a JNC project file!");
				project.hmJars.put(new File(sa[1].substring(0, index)),
						sa[1].substring(index+1).equals("true"));
			}
			else if(sa[0].equals("mainClass"))
			{
				String[] saSub = sa[1].split("#", 2);
				if(saSub.length != 2) throw new IOException("Not a JNC project file!");
				if(saSub[0].equals("null")) continue;

				project.mainClass = saSub[0];
				project.mainClassRessource = new File(saSub[1]);
			}
			else if(sa[0].equals("javaLibPath"))         project.javaLibPath = sa[1];
			else if(sa[0].equals("useCni"))              project.useCni = sa[1].equals("true");
			else if(sa[0].equals("compileWindows"))      project.compileWindows = sa[1].equals("true");
			else if(sa[0].equals("windowsFile"))         project.windowsFile = new File(sa[1]);
			else if(sa[0].equals("compileLinux"))        project.compileLinux = sa[1].equals("true");
			else if(sa[0].equals("linuxFile"))           project.linuxFile = new File(sa[1]);
			else if(sa[0].equals("omitStripping"))       project.omitStripping = sa[1].equals("true");
			else if(sa[0].equals("omitPacking"))         project.omitPacking = sa[1].equals("true");
			else if(sa[0].equals("iconFile"))            project.iconFile = new File(sa[1]);
			else if(sa[0].equals("useIcon"))             project.useIcon = sa[1].equals("true");
			else if(sa[0].equals("hideConsole"))         project.hideConsole = sa[1].equals("true");
			else if(sa[0].equals("gcjFlag"))
			{
				int index = sa[1].lastIndexOf(',');
				if(index < 0) throw new IOException("Not a JNC project file!");
				project.hmGcjFlags.put(sa[1].substring(0, index),
						sa[1].substring(index+1).equals("true"));
			}
			else if(sa[0].equals("showCommands"))        project.showCommands = sa[1].equals("true");
			else if(sa[0].equals("excludeGui"))          project.excludeGui = sa[1].equals("true");
			else if(sa[0].equals("excludeJce"))          project.excludeJce = sa[1].equals("true");
			else if(sa[0].equals("addGnuRegex"))         project.addGnuRegex = sa[1].equals("true");
			else if(sa[0].equals("dontCacheJars"))       project.dontCacheJars = sa[1].equals("true");
			else if(sa[0].equals("beepWhenDone"))        project.beepWhenDone = sa[1].equals("true");
			else if(sa[0].equals("disableOptimisation")) project.disableOptimisation = sa[1].equals("true");
		}

		br.close();
		return project;
	}

	public void addProjectListener(IAppControllerListener acl) { vListeners.add(acl); }
	public void removeProjectListener(IAppControllerListener acl) { vListeners.remove(acl); }


	// --------------- private methods ---------------

	private void checkIfMainClassDeleted(File f)
	{
		if(f.equals(mainClassRessource))
		{
			mainClassRessource = null;
			mainClass = null;
		}
	}
}
