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

package ch.mtSystems.javaCompiler.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import ch.mtSystems.javaCompiler.model.exceptions.NoJavaException;
import ch.mtSystems.javaCompiler.model.projects.ManagedAwtSwingProject;
import ch.mtSystems.javaCompiler.model.projects.ManagedJFaceProject;
import ch.mtSystems.javaCompiler.model.projects.ManagedSwtProject;
import ch.mtSystems.javaCompiler.model.projects.ObjectProject;
import ch.mtSystems.javaCompiler.model.utilities.Beep;
import ch.mtSystems.javaCompiler.model.utilities.ClassUtilities;
import ch.mtSystems.javaCompiler.model.utilities.FileUtilities;
import ch.mtSystems.javaCompiler.model.utilities.SettingsMemory;


public class JavaCompiler
{
	private static final String CMD_WIN_GCJ = "utilities/gcc-4.2.0-win/bin/gcj.exe";
	private static final String CMD_LIN_GCJ = "utilities/gcc-4.2.0-lin/bin/gcj.exe";
	private static final String CMD_WINDRES = "utilities/gcc-4.2.0-win/bin/windres.exe";
	private static final String CMD_UPX = "utilities/upx200w/upx.exe";


	private ICompilationProgressLogger logger;
	private JavaCompilerProject project;
	private File outDir;


	public JavaCompiler(ICompilationProgressLogger logger, JavaCompilerProject project)
	{
		this.logger = logger;
		this.project = project;
	}

	public boolean compile() throws Exception
	{
		try
		{
			if(!project.getOmitWindows() && !compile("win")) { beep(true); return false; }
			if(!project.getOmitLinux() && !compile("lin")) { beep(true); return false; }

			beep(false);
			return true;
		} catch(Exception ex)
		{
			beep(true);
			throw ex;
		}
	}

	private boolean compile(String os) throws Exception
	{
		outDir = FileUtilities.createTempDir("JavaCompilerTemp", ".out");
		if(!outDir.exists() && !outDir.mkdirs()) throw new IOException("Creating the output directory (" + outDir + ") failed!");

		try
		{
			if(project instanceof ObjectProject)
			{
				File in = new File(project.getMainClass());

				String outName = in.getName();
				outName = outName.substring(0, outName.length()-4) + "-" + os + ".jar.o";
				File out = new File(in.getParent(), outName);

				logger.log("creating " + outName, false);
				FileUtilities.copyFile(in, new File(outDir, in.getName()));
				if(!retroWeaver(os)) return false;

				LinkedList<File> llLibs = new LinkedList<File>();
				File[] allJars = project.getJars();
				for(int i=0; i<allJars.length; i++)
				{
					if(!allJars[i].equals(in)) llLibs.add(allJars[i]);
				}

				return createObject(os, outDir.listFiles()[0], out, llLibs.toArray(new File[0]));
			} else
			{
				if(os.equals("win"))
				{
					logger.log("creating " + project.getOutputName() + "-win.exe", false);
				} else if(os.equals("lin"))
				{
					logger.log("creating " + project.getOutputName() + "-lin", false);
				} else
				{
					throw new Exception("unknown plattform: " + os);
				}

				copyDataToOutDir(os);
				swingWT(os);
				jFace(os);
				swt(os);
				if(!retroWeaver(os)) return false;
				if(!createObjects(os)) return false;
				return finalCompile(os);
			}
		} finally
		{
			FileUtilities.deleteDirRecursively(outDir);
		}
	}

	/**
	 * Copies<br>
	 * - all configured files (.java, .class)<br>
	 * - all files (.java, .class) from the configured directories<br>
	 * - all configured jars<br>
	 * to outDir.
	 */
	private void copyDataToOutDir(String os) throws IOException
	{
		HashSet<File> hsFiles = getAllFiles();
		for(Iterator<File> it=hsFiles.iterator(); it.hasNext();)
		{
			File file = it.next();
			if(file.getName().endsWith(".java") || file.getName().endsWith(".class"))
			{
				String thePackage = ClassUtilities.getPackage(file);

				File dir = (thePackage == null) ? outDir : new File(outDir, thePackage.replaceAll("\\.", "/"));
				if(!dir.exists() && !dir.mkdirs()) throw new IOException("Create tmp dir failed:\n" + dir);

				FileUtilities.copyFile(file, new File(dir, file.getName()));
			}
		}

		File[] fa = project.getJars();
		for(int i=0; i<fa.length; i++)
		{
			FileUtilities.copyFile(fa[i], new File(outDir, fa[i].getName()));

			// check if there's already an object file
			String objectName = fa[i].getName();
			objectName = objectName.substring(0, objectName.length()-4) + "-" + os + ".jar.o";

			File fObject = new File(fa[i].getParent(), objectName);
			if(fObject.exists()) FileUtilities.copyFile(fObject, new File(outDir, fa[i].getName()+".o"));
		}
	}

	/**
	 * If it's a Awt/Swing project:<br>
	 * - replaces "javax.swing." with "swingwtx.swing." and
	 *   "java.awt." with "swingwt.awt." in all .java and .class files
	 * - copies the needed jar's and object jar.o's
	 */
	private void swingWT(String os) throws IOException
	{
		if(!(project instanceof ManagedAwtSwingProject)) return;

		HashSet<File> hsFiles = new HashSet<File>();
		getFiles(outDir, hsFiles);

		for(Iterator<File> it=hsFiles.iterator(); it.hasNext();)
		{
			File f = it.next();
			if(f.getName().endsWith(".java") || f.getName().endsWith(".class"))
			{
				ClassUtilities.convertToSwingWT(f);
			}
		}

		copyJars(new File("utilities/swingWT-0.87"), os);
	}

	/**
	 * If it's a JFace project:<br>
	 * - copies the needed jar's and object jar.o's
	 */
	private void jFace(String os) throws IOException
	{
		if(!(project instanceof ManagedJFaceProject)) return;

		copyJars(new File("utilities/jface-3.1.1"), os);
	}

	/**
	 * If it's a swt project:<br>
	 * - copies the needed dll to the executable output dir
	 * - copies the needed jar's and object jar.o's
	 */
	private void swt(String os) throws Exception
	{
		if(!(project instanceof ManagedSwtProject)) return;

		// copy the needed library to the executable output dir
		if(os.equals("win"))
		{
			FileUtilities.copyFile(new File("utilities/swt3139/swt-win32-3139.dll"),
					new File(project.getOutputDir(), "swt-win32-3139.dll"));
		} else if(os.equals("lin"))
		{
			FileUtilities.copyFile(new File("utilities/swt3139/libswt-gtk-3139.so"),
					new File(project.getOutputDir(), "libswt-gtk-3139.so"));
			FileUtilities.copyFile(new File("utilities/swt3139/libswt-pi-gtk-3139.so"),
					new File(project.getOutputDir(), "libswt-pi-gtk-3139.so"));
		} else
		{
			throw new Exception("unknown plattform: " + os);
		}

		copyJars(new File("utilities/swt3139"), os);
	}

	/**
	 * If Java 1.5 preprocessing is activated:<br>
	 * - compiles all .java files in outDir<br>
	 * - deletes all .java files in outDir<br>
	 * - weaves all .class files in outDir<br>
	 * - weaves all .jar files in outDir and deletes the original jars<br>
	 * - copies the needed jar's and object jar.o's
	 */
	private boolean retroWeaver(String os) throws Exception
	{
		if(!project.getJava5Preprocessing()) return true;

		HashSet<File> hsFiles = new HashSet<File>();
		getFiles(outDir, hsFiles);

		boolean hasSourceFiles = false;
		boolean hasClassFiles = false;

		for(Iterator<File> it=hsFiles.iterator(); it.hasNext();)
		{
			File f = it.next();

			if(f.getName().endsWith(".java"))
			{
				hasSourceFiles = true;
				if(hasClassFiles) break;
			} else if(f.getName().endsWith(".class"))
			{
				hasClassFiles = true;
				if(hasSourceFiles) break;
			}
		}

		if(hasSourceFiles) // compile and delete them
		{
			// get javac
			String javac = SettingsMemory.getSettingsMemory().getJavac();
			if(javac == null) throw new NoJavaException("JDK 1.5 javac not specified!");
			if(!(new File(javac)).exists()) throw new NoJavaException("Configured JDK 1.5 javac doesn't exist!");


			// compile all .java files
			LinkedList<String> alCmd = new LinkedList<String>();
			alCmd.add(javac);

			StringBuffer sbJars = new StringBuffer();
			for(Iterator<File> it=hsFiles.iterator(); it.hasNext();)
			{
				File f = it.next();
				if(f.getName().endsWith(".jar"))
				{
					if(sbJars.length() > 0) sbJars.append(';');
					sbJars.append(f);
				} else if(f.getName().endsWith(".java"))
				{
					alCmd.add(f.toString());
				}
			}
			if(sbJars.length() > 0)
			{
				alCmd.add(1, "-cp");
				alCmd.add(2, sbJars.toString());
			}

			if(!runCmd(alCmd.toArray(new String[0]),
					"compiling sources for Java 1.5 preprocessing", true)) return false;


			// delete all .java files
			for(Iterator<File> it=hsFiles.iterator(); it.hasNext();)
			{
				File f = it.next();
				if(f.getName().endsWith(".java") && !f.delete())
				{
					logger.log("deleting " + f.getName() + "failed!", true);
					return false;
				}
			}

			hasClassFiles = true; // might have been false until now
		}

		if(hasClassFiles) // weave all .class files
		{
			String[] cmd = new String[]
					{
						"utilities/retroweaver-1.2.3/weaver.exe",
						"-source", outDir.toString()
					};
			if(!runCmd(cmd, "Java 1.5 file (*.class) preprocessing", true)) return false;
		}


		// weave all .jar files in outDir and delete the original jars
		for(Iterator<File> it=hsFiles.iterator(); it.hasNext();)
		{
			File f = it.next();
			if(!f.getName().endsWith(".jar")) continue;

			// ignore any jar that have already an object file (swt, jface, swingwt, ...)
			if((new File(f.getParent(), f.getName()+".o")).exists()) continue;

			String[] cmd = new String[]
				{
					"utilities/retroweaver-1.2.3/weaver.exe",
					"-jar", f.toString(), f.toString() + "-weaved.jar"
				};
			if(!runCmd(cmd, "Java 1.5 preprocessing: " + f.getName(), true)) return false;

			if(!f.delete())
			{
				logger.log("deleting " + f.getName() + "failed!", true);
				return false;
			}
		}

		copyJars(new File("utilities/retroweaver-1.2.3"), os);
		return true;
	}

	/**
	 * Creates objects from all jars unless already there
	 */
	private boolean createObjects(String os) throws Exception
	{
		File[] fa = outDir.listFiles();

		for(int i=0; i<fa.length; i++)
		{
			if(!fa[i].getName().endsWith(".jar")) continue;

			// ignore any jar that have already an object file (swt, jface, swingwt, ...)
			File fOut = new File(fa[i].getParentFile(), fa[i].getName()+".o");
			if(fOut.exists()) continue;

			if(!createObject(os, fa[i], fOut, fa)) return false;
		}

		return true;
	}

	private boolean createObject(String os, File in, File out, File[] fa) throws Exception
	{
		String gcj;
			 if(os.equals("win")) gcj = CMD_WIN_GCJ;
		else if(os.equals("lin")) gcj = CMD_LIN_GCJ;
		else                      throw new Exception("unknown plattform: " + os);

		LinkedList<String> alCmd = new LinkedList<String>();
		alCmd.add(gcj);
		if(project.getUseJni()) alCmd.add("-fjni");
		if(project.getIgnoreMissingReferences()) alCmd.add("-findirect-dispatch");
		alCmd.add("-c"); alCmd.add(in.toString());
		alCmd.add("-o"); alCmd.add(out.toString());
		for(int i=0; i<fa.length; i++)
		{
			if(fa[i].getName().endsWith(".jar") && !fa[i].equals(in))
			{
				alCmd.add("-I"); alCmd.add(fa[i].toString());
			}
		}

		String[] saCmd = alCmd.toArray(new String[0]);
		return runCmd(saCmd, "processing " + in.getName(), true);
	}

	private boolean finalCompile(String os) throws Exception
	{
		LinkedList<String> alCmd = new LinkedList<String>();

		String gcj, fileName;
		if(os.equals("win"))
		{
			gcj = CMD_WIN_GCJ;
			fileName = project.getOutputName() + "-win.exe";
		} else if(os.equals("lin"))
		{
			gcj = CMD_LIN_GCJ;
			fileName = project.getOutputName() + "-lin";
		} else
		{
			throw new Exception("unknown plattform: " + os);
		}

		alCmd.add(gcj);
		if(!project.getOmitStripping()) alCmd.add("-s");
		if(project.getUseJni()) alCmd.add("-fjni");
		if(project.getIgnoreMissingReferences()) alCmd.add("-findirect-dispatch");
		alCmd.add("--main=" + project.getMainClass());
		alCmd.add("-o");

		File fExecutable = new File(project.getOutputDir(), fileName);
		alCmd.add(fExecutable.toString());

		if(project instanceof ManagedSwtProject) alCmd.add("-Djava.library.path=.");

		if(os.equals("win"))
		{
			if(project.getHideConsole()) alCmd.add("-mwindows");
			if(!addIcon()) return false;
		}

		HashSet<File> hsFiles = new HashSet<File>();
		getFiles(outDir, hsFiles);
		boolean hasObjects = false;

		File fInputList = File.createTempFile("SourceList", ".list", outDir);
		FileWriter fw = new FileWriter(fInputList);

		for(Iterator<File> it=hsFiles.iterator(); it.hasNext();)
		{
			File f = it.next();

			if(f.getName().endsWith(".jar"))
			{
				alCmd.add("-I");
				alCmd.add(f.toString());
			} else if(f.getName().endsWith(".java") || f.getName().endsWith(".class"))
			{
				fw.write(f.toString().replaceAll("\\\\", "/"));
				fw.write(" ");
			} else if(f.getName().endsWith(".o"))
			{
				hasObjects = true;
			}
		}

		fw.flush();
		fw.close();

		if(hasObjects) alCmd.add((new File(outDir, "*.o")).toString());
		alCmd.add("@" + fInputList.toString());

		String[] saCmd = alCmd.toArray(new String[0]);
		if(!runCmd(saCmd, "main compilation step", true)) return false;

		if(!project.getOmitPacking())
		{
			String[] saCmdUpx = { CMD_UPX, "--best", fExecutable.toString() };
			if(!runCmd(saCmdUpx, "packing executable", false)) return false;
		}

		return true;
	}

	private HashSet<File> getAllFiles()
	{
		HashSet<File> hsFiles = new HashSet<File>();

		// files
		File[] fa = project.getFiles();
		for(int i=0; i<fa.length; i++) hsFiles.add(fa[i]);

		// directories
		fa = project.getDirectories();
		for(int i=0; i<fa.length; i++) getFiles(fa[i], hsFiles);

		return hsFiles;
	}

	/**
	 * Adds all files from the directory and all subdirectories to the provided HashSet.
	 */
	private void getFiles(File dir, HashSet<File> hs)
	{
		File[] fa = dir.listFiles();

		for(int i=0; i<fa.length; i++)
		{
			if(fa[i].isDirectory()) getFiles(fa[i], hs);
			else                    hs.add(fa[i]);
		}
	}

	private void copyJars(File srcDir, String os) throws IOException
	{
		File[] fa = srcDir.listFiles();

		for(int i=0; i<fa.length; i++)
		{
			String name = fa[i].getName();

			if(name.endsWith("-all.jar"))
			{
				String newName = name.substring(0, name.length()-8) + ".jar";
				FileUtilities.copyFile(fa[i], new File(outDir, newName));
			} else if(name.endsWith("-" + os + ".jar"))
			{
				String newName = name.substring(0, name.length()-8) + ".jar";
				FileUtilities.copyFile(fa[i], new File(outDir, newName));
			} else if(name.endsWith("-" + os + ".jar.o"))
			{
				String newName = name.substring(0, name.length()-10) + ".jar.o";
				FileUtilities.copyFile(fa[i], new File(outDir, newName));
			}
		}
	}

	private boolean addIcon() throws Exception
	{
		File iconFile = project.getIconFile();

		if(!project.getUseIcon() || iconFile == null) return true;
		if(!iconFile.exists()) throw new IOException("Windows icon file doesn't exist:\n" + iconFile.toString());

		File fTmp = File.createTempFile("icon", ".rc", outDir);
		FileWriter fw = new FileWriter(fTmp);
		fw.write("1 ICON \"" + iconFile.toString().replaceAll("\\\\", "/") + "\"\n");
		fw.flush();
		fw.close();

		String[] saCmd = { CMD_WINDRES, fTmp.toString(), fTmp.toString()+".o" };
		if(!runCmd(saCmd, "including icon", true)) return false;

		fTmp.delete();
		return true;
	}

	private boolean runCmd(String[] cmd, String logLine, boolean logInput) throws Exception
	{
		logger.log("- " + logLine, false);

		//for(int i=0; i<cmd.length; i++) System.out.print(cmd[i] + " ");
		//System.out.println();

		Process p = Runtime.getRuntime().exec(cmd);
		if(logInput) log(p.getInputStream());
		log(p.getErrorStream());
		return (p.waitFor() == 0);
	}

	private void log(final InputStream inputStream)
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
					String line;

					while((line = br.readLine()) != null) logger.log(line, true);

					br.close();
				} catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * Beeps twice if error ist true, once otherwise.
	 */
	private void beep(boolean error) throws Exception
	{
		if(!project.getBeepWhenDone()) return;

		if(error) Beep.beep(900, 300);
		else      Beep.beep(400, 200);
	}
}
