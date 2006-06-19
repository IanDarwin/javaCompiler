package ch.mtSystems.javaCompiler.model;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;


public abstract class JavaCompilerProject
{
	private Set lFiles = new LinkedHashSet();
	private Set lDirectories = new LinkedHashSet();
	private Set lJars = new LinkedHashSet();

	private File mainClassRessource;
	private String mainClass;

	private File outputDir;
	private String outputName;

	private boolean java5Preprocessing = false;
	private boolean suppressDeprecationWarnings = false;

	private boolean omitWindows = false;
	private boolean omitLinux = false;
	private boolean omitStripping = false;
	private boolean omitPacking = false;

	private File iconFile;
	private boolean useIcon = false;
	private boolean hideConsole = false;


	// --------------- public methods ---------------

	public void addFile(File f)
	{
		lFiles.add(f);
	}

	public void removeFile(File f)
	{
		lFiles.remove(f);
		checkIfMainClassDeleted(f);
	}

	public File[] getFiles()
	{
		return (File[])lFiles.toArray(new File[0]);
	}



	public void addDirectory(File dir)
	{
		lDirectories.add(dir);
	}

	public void removeDirectory(File dir)
	{
		lDirectories.remove(dir);
		checkIfMainClassDeleted(dir);
	}

	public File[] getDirectories()
	{
		return (File[])lDirectories.toArray(new File[0]);
	}



	public void addJar(File f)
	{
		lJars.add(f);
	}

	public void removeJar(File f)
	{
		lJars.remove(f);
		checkIfMainClassDeleted(f);
	}

	public File[] getJars()
	{
		return (File[])lJars.toArray(new File[0]);
	}



	/**
	 * Sets the main class for the project.
	 * 
	 * @param mainClassRessource The ressource (file, directory or jar) where the class is in.
	 * @param mainClass The main class in java notation. for example: ch.mtSystems.javaCompiler.view.JavaCompilerGui
	 */
	public void setMainClass(File mainClassRessource, String mainClass)
	{
		this.mainClassRessource = mainClassRessource;
		this.mainClass = mainClass;
	}

	public String getMainClass()
	{
		return mainClass;
	}

	public boolean getJava5Preprocessing() { return java5Preprocessing; }
	public void setJava5Preprocessing(boolean preprocess) { java5Preprocessing = preprocess; }

	public boolean getSuppressDeprecationWarnings() { return suppressDeprecationWarnings; }
	public void setSuppressDeprecationWarnings(boolean suppress) { suppressDeprecationWarnings = suppress; }

	public File getOutputDir() { return outputDir; }
	public void setOutputDir(File outputDir) { this.outputDir = outputDir; }

	public String getOutputName() { return outputName; }
	public void setOutputName(String outputName) { this.outputName = outputName; }

	public boolean getOmitWindows() { return omitWindows; }
	public void setOmitWindows(boolean omit) { omitWindows = omit; }

	public boolean getOmitLinux() { return omitLinux; }
	public void setOmitLinux(boolean omit) { omitLinux = omit; }

	public boolean getOmitStripping() { return omitStripping; }
	public void setOmitStripping(boolean omit) { omitStripping = omit; }

	public boolean getOmitPacking() { return omitPacking; }
	public void setOmitPacking(boolean omit) { omitPacking = omit; }

	public boolean getUseIcon() { return useIcon; }
	public void setUseIcon(boolean useIt) { useIcon = useIt; }

	public File getIconFile() { return iconFile; }
	public void setIconFile(File iconFile) { this.iconFile = iconFile; }

	public boolean getHideConsole() { return hideConsole; }
	public void setHideConsole(boolean hide) { hideConsole = hide; }


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
