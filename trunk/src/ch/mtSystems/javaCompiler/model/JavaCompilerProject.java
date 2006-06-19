package ch.mtSystems.javaCompiler.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.model.projects.ManagedAwtSwingProject;
import ch.mtSystems.javaCompiler.model.projects.ManagedJFaceProject;
import ch.mtSystems.javaCompiler.model.projects.ManagedSwtProject;
import ch.mtSystems.javaCompiler.model.projects.UnmanagedProject;


public abstract class JavaCompilerProject
{
	// the source
	private Set<File> lFiles = new LinkedHashSet<File>();
	private Set<File> lDirectories = new LinkedHashSet<File>();
	private Set<File> lJars = new LinkedHashSet<File>();

	// common settings
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

	// windows settings
	private File iconFile;
	private boolean useIcon = false;
	private boolean hideConsole = false;

	// save file
	private File saveFile = null;


	// --------------- public methods ---------------

	public void addFile(File f)
	{
		lFiles.add(f);
		AppController.getAppController().fireProjectChanged();
	}

	public void removeFile(File f)
	{
		lFiles.remove(f);
		checkIfMainClassDeleted(f);
		AppController.getAppController().fireProjectChanged();
	}

	public File[] getFiles()
	{
		return lFiles.toArray(new File[0]);
	}



	public void addDirectory(File dir)
	{
		lDirectories.add(dir);
		AppController.getAppController().fireProjectChanged();
	}

	public void removeDirectory(File dir)
	{
		lDirectories.remove(dir);
		checkIfMainClassDeleted(dir);
		AppController.getAppController().fireProjectChanged();
	}

	public File[] getDirectories()
	{
		return lDirectories.toArray(new File[0]);
	}



	public void addJar(File f)
	{
		lJars.add(f);
		AppController.getAppController().fireProjectChanged();
	}

	public void removeJar(File f)
	{
		lJars.remove(f);
		checkIfMainClassDeleted(f);
		AppController.getAppController().fireProjectChanged();
	}

	public File[] getJars()
	{
		return lJars.toArray(new File[0]);
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
		AppController.getAppController().fireProjectChanged();
	}

	public String getMainClass()
	{
		return mainClass;
	}

	public boolean getJava5Preprocessing() { return java5Preprocessing; }

	public void setJava5Preprocessing(boolean preprocess)
	{
		java5Preprocessing = preprocess;
		AppController.getAppController().fireProjectChanged();
	}

	public boolean getSuppressDeprecationWarnings() { return suppressDeprecationWarnings; }

	public void setSuppressDeprecationWarnings(boolean suppress)
	{
		suppressDeprecationWarnings = suppress;
		AppController.getAppController().fireProjectChanged();
	}

	public File getOutputDir() { return outputDir; }

	public void setOutputDir(File outputDir)
	{
		this.outputDir = outputDir;
		AppController.getAppController().fireProjectChanged();
	}

	public String getOutputName() { return outputName; }

	public void setOutputName(String outputName)
	{
		this.outputName = outputName;
		AppController.getAppController().fireProjectChanged();
	}

	public boolean getOmitWindows() { return omitWindows; }

	public void setOmitWindows(boolean omit)
	{
		omitWindows = omit;
		AppController.getAppController().fireProjectChanged();
	}

	public boolean getOmitLinux() { return omitLinux; }

	public void setOmitLinux(boolean omit)
	{
		omitLinux = omit;
		AppController.getAppController().fireProjectChanged();
	}

	public boolean getOmitStripping() { return omitStripping; }

	public void setOmitStripping(boolean omit)
	{
		omitStripping = omit;
		AppController.getAppController().fireProjectChanged();
	}

	public boolean getOmitPacking() { return omitPacking; }

	public void setOmitPacking(boolean omit)
	{
		omitPacking = omit;
		AppController.getAppController().fireProjectChanged();
	}

	public boolean getUseIcon() { return useIcon; }

	public void setUseIcon(boolean useIt)
	{
		useIcon = useIt;
		AppController.getAppController().fireProjectChanged();
	}

	public File getIconFile() { return iconFile; }

	public void setIconFile(File iconFile)
	{
		this.iconFile = iconFile;
		AppController.getAppController().fireProjectChanged();
	}

	public boolean getHideConsole() { return hideConsole; }

	public void setHideConsole(boolean hide)
	{
		hideConsole = hide;
		AppController.getAppController().fireProjectChanged();
	}

	public File getSaveFile() { return saveFile; }

	public void save(File f) throws IOException
	{
		saveFile = f;
		FileWriter fw = new FileWriter(f);
		fw.write("projectType=" + getClass().getSimpleName() + "\n");

		// the source
		for(Iterator it=lFiles.iterator();       it.hasNext();) fw.write("file=" + it.next() + "\n");
		for(Iterator it=lDirectories.iterator(); it.hasNext();) fw.write("dir=" +  it.next() + "\n");
		for(Iterator it=lJars.iterator();        it.hasNext();) fw.write("jar=" +  it.next() + "\n");

		// common settings
		fw.write("mainClass=" + mainClass + "#" + mainClassRessource + "\n");
		fw.write("outputDir=" + outputDir + "\n");
		fw.write("outputName=" + outputName + "\n");
		fw.write("java5Preprocessing=" + java5Preprocessing + "\n");
		fw.write("suppressDeprecationWarnings=" + suppressDeprecationWarnings + "\n");
		fw.write("omitWindows=" + omitWindows + "\n");
		fw.write("omitLinux=" + omitLinux + "\n");
		fw.write("omitStripping=" + omitStripping + "\n");
		fw.write("omitPacking=" + omitPacking + "\n");

		// windows settings
		fw.write("iconFile=" + iconFile + "\n");
		fw.write("useIcon=" + useIcon + "\n");
		fw.write("hideConsole=" + hideConsole);

		fw.flush();
		fw.close();

		AppController.getAppController().fireProjectSaved();
	}

	public static JavaCompilerProject open(File f) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(f));
		JavaCompilerProject project = null;
		String line;

		while((line = br.readLine()) != null)
		{
			String[] sa = line.split("=", 2);
			if(sa.length != 2) throw new IOException("Not a JavaCompilerProject file!");

			if(sa[0].equals("projectType"))
			{
					 if(sa[1].equals("null") || project != null)                    throw new IOException("Not a JavaCompilerProject file!");
				else if(sa[1].equals(UnmanagedProject.class.getSimpleName()))       project = new UnmanagedProject();
				else if(sa[1].equals(ManagedSwtProject.class.getSimpleName()))      project = new ManagedSwtProject();
				else if(sa[1].equals(ManagedJFaceProject.class.getSimpleName()))    project = new ManagedJFaceProject();
				else if(sa[1].equals(ManagedAwtSwingProject.class.getSimpleName())) project = new ManagedAwtSwingProject();
				else                                                                throw new IOException("Not a JavaCompilerProject file!");

				project.saveFile = f;
			} else
			{
					 if(project == null)      throw new IOException("Not a JavaCompilerProject file!");
				else if(sa[1].equals("null")) continue; // ignore, defaults are null anyway
				else if(sa[0].equals("file")) project.lFiles.add(new File(sa[1]));
				else if(sa[0].equals("dir"))  project.lDirectories.add(new File(sa[1]));
				else if(sa[0].equals("jar"))  project.lJars.add(new File(sa[1]));
				else if(sa[0].equals("mainClass"))
				{
					String[] saSub = sa[1].split("#", 2);
					if(saSub.length != 2) throw new IOException("Not a JavaCompilerProject file!");
					if(saSub[0].equals("null")) continue;

					project.mainClass = saSub[0];
					project.mainClassRessource = new File(saSub[1]);
				}
				else if(sa[0].equals("outputDir"))                   project.outputDir = new File(sa[1]);
				else if(sa[0].equals("outputName"))                  project.outputName = sa[1];
				else if(sa[0].equals("java5Preprocessing"))          project.java5Preprocessing = sa[1].equals("true");
				else if(sa[0].equals("suppressDeprecationWarnings")) project.suppressDeprecationWarnings = sa[1].equals("true");
				else if(sa[0].equals("omitWindows"))                 project.omitWindows = sa[1].equals("true");
				else if(sa[0].equals("omitLinux"))                   project.omitLinux = sa[1].equals("true");
				else if(sa[0].equals("omitStripping"))               project.omitStripping = sa[1].equals("true");
				else if(sa[0].equals("omitPacking"))                 project.omitPacking = sa[1].equals("true");
				else if(sa[0].equals("iconFile"))                    project.iconFile = new File(sa[1]);
				else if(sa[0].equals("useIcon"))                     project.useIcon = sa[1].equals("true");
				else if(sa[0].equals("hideConsole"))                 project.hideConsole = sa[1].equals("true");
			}
		}

		br.close();

		if(project == null) throw new IOException("Not a JavaCompilerProject file!");
		return project;
	}


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