package ch.mtSystems.javaCompiler.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import ch.mtSystems.javaCompiler.model.exceptions.NoJavaException;
import ch.mtSystems.javaCompiler.model.projects.ManagedAwtSwingProject;
import ch.mtSystems.javaCompiler.model.projects.ManagedJFaceProject;
import ch.mtSystems.javaCompiler.model.projects.ManagedSwtProject;
import ch.mtSystems.javaCompiler.model.utilities.ClassUtilities;
import ch.mtSystems.javaCompiler.model.utilities.FileUtilities;
import ch.mtSystems.javaCompiler.model.utilities.SettingsMemory;


public class JavaCompiler
{
	private static final String CMD_WIN_GCJ = "ressources\\gcc-4.1.1-win\\bin\\gcj.exe";
	private static final String CMD_LIN_GCJ = "ressources\\gcc-4.1.1-lin\\bin\\gcj.exe";
	private static final String CMD_WINDRES = "ressources\\gcc-4.1.1-win\\bin\\windres.exe";
	private static final String CMD_UPX = "ressources\\upx200w\\upx.exe";


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
		if(!project.getOmitWindows() && !compile("win")) return false;
		if(!project.getOmitLinux() && !compile("lin")) return false;
		return true;
	}

	private boolean compile(String os) throws Exception
	{
		outDir = FileUtilities.createTempDir("JavaCompilerTemp", ".out");
		if(!outDir.exists() && !outDir.mkdirs()) throw new IOException("Creating the output directory (" + outDir + ") failed!");

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

		try
		{
			copyDataToOutDir();

			swingWT(os);
			jFace(os);
			swt(os);
			if(!retroWeaver(os)) return false;
			if(!createObjects(os)) return false;
			if(!finalCompile(os)) return false;
		} finally
		{
			FileUtilities.deleteDirRecursively(outDir);
		}

		return true;
	}

	/**
	 * Copies<br>
	 * - all configured files (.java, .class)<br>
	 * - all files (.java, .class) from the configured directories<br>
	 * - all configured jars<br>
	 * to outDir.
	 */
	private void copyDataToOutDir() throws IOException
	{
		HashSet hsFiles = getAllFiles();
		for(Iterator it=hsFiles.iterator(); it.hasNext();)
		{
			File f = (File)it.next();
			FileUtilities.copyFile(f, new File(outDir, f.getName()));
		}

		File[] fa = project.getJars();
		for(int i=0; i<fa.length; i++)
		{
			FileUtilities.copyFile(fa[i], new File(outDir, fa[i].getName()));
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
		File[] fa = outDir.listFiles();

		// replace "javax.swing" with "swingwtx.swing" and
		// "java.awt" with "swingwt.awt" in all .java files
		for(int i=0; i<fa.length; i++)
		{
			if(fa[i].getName().endsWith(".java"))
			{
				ClassUtilities.sourceAwtSwingToSwingWT(fa[i]);
			} else if(fa[i].getName().endsWith(".class"))
			{
				ClassUtilities.classAwtSwingToSwingWT(fa[i]);
			}
		}

		copyJars(new File("ressources/swingWT-0.87"), os);
	}

	/**
	 * If it's a JFace project:<br>
	 * - copies the needed jar's and object jar.o's
	 */
	private void jFace(String os) throws IOException
	{
		if(!(project instanceof ManagedJFaceProject)) return;

		copyJars(new File("ressources/jface-3.1.1"), os);
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
			FileUtilities.copyFile(new File("ressources/swt3139/swt-win32-3139.dll"),
					new File(project.getOutputDir(), "swt-win32-3139.dll"));
		} else if(os.equals("lin"))
		{
			FileUtilities.copyFile(new File("ressources/swt3139/libswt-gtk-3139.so"),
					new File(project.getOutputDir(), "libswt-gtk-3139.so"));
			FileUtilities.copyFile(new File("ressources/swt3139/libswt-pi-gtk-3139.so"),
					new File(project.getOutputDir(), "libswt-pi-gtk-3139.so"));
		} else
		{
			throw new Exception("unknown plattform: " + os);
		}

		copyJars(new File("ressources/swt3139"), os);
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
		File[] fa = outDir.listFiles();


		// check if there are source (.java) files. they would have to be compiled
		boolean hasSourceFiles = false;
		for(int i=0; i<fa.length; i++)
		{
			if(fa[i].getName().endsWith(".java"))
			{
				hasSourceFiles = true;
				break;
			}
		}

		if(hasSourceFiles) // compile and delete them
		{
			// get javac
			String javac = SettingsMemory.getSettingsMemory().getJavac();
			if(javac == null) throw new NoJavaException("JDK 1.5 javac not specified!");
			if(!(new File(javac)).exists()) throw new NoJavaException("Configured JDK 1.5 javac doesn't exist!");


			// compile all .java files
			ArrayList<String> alCmd = new ArrayList<String>();
			alCmd.add(javac);

			StringBuffer sbJars = new StringBuffer();
			for(int i=0; i<fa.length; i++)
			{
				if(!fa[i].getName().endsWith(".jar")) continue;
				if(sbJars.length() > 0) sbJars.append(';');
				sbJars.append(fa[i].toString());
			}
			if(sbJars.length() > 0)
			{
				alCmd.add("-cp");
				alCmd.add(sbJars.toString());
			}

			alCmd.add(new File(outDir, "*.java").toString());

			if(!runCmd(alCmd.toArray(new String[0]),
					"compiling sources for Java 1.5 preprocessing", true)) return false;


			// delete all .java files
			for(int i=0; i<fa.length; i++)
			{
				if(fa[i].getName().endsWith(".java") && !fa[i].delete())
				{
					logger.log("deleting " + fa[i].getName() + "failed!", true);
					return false;
				}
			}
		}


		// weave all .class files
		String[] cmd = new String[]
				{
					"ressources\\retroweaver-1.2.3\\weaver.exe",
					"-source", outDir.toString()
				};
		if(!runCmd(cmd, "Java 1.5 file (*.class) preprocessing", true)) return false;


		// weave all .jar files in outDir and delete the original jars
		for(int i=0; i<fa.length; i++)
		{
			if(!fa[i].getName().endsWith(".jar")) continue;

			// ignore any jar that have already an object file (swt, jface, swingwt, ...)
			if((new File(fa[i].getParent(), fa[i].getName()+".o")).exists()) continue;

			cmd = new String[]
				{
					"ressources\\retroweaver-1.2.3\\weaver.exe",
					"-jar", fa[i].toString(), fa[i].toString() + "-weaved.jar"
				};
			if(!runCmd(cmd, "Java 1.5 preprocessing: " + fa[i].getName(), true)) return false;

			if(!fa[i].delete())
			{
				logger.log("deleting " + fa[i].getName() + "failed!", true);
				return false;
			}
		}

		copyJars(new File("ressources/retroweaver-1.2.3"), os);
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

			// some preprocessing steps might already copy an object for a jar
			// currently this is done for swt and retroweaver
			File fOut = new File(fa[i].getParentFile(), fa[i].getName()+".o");
			if(fOut.exists()) continue;

			String gcj;
				 if(os.equals("win")) gcj = CMD_WIN_GCJ;
			else if(os.equals("lin")) gcj = CMD_LIN_GCJ;
			else                      throw new Exception("unknown plattform: " + os);

			String[] cmd =
					{
						gcj,
						"-c", fa[i].toString(),
						"-o", fOut.toString()
					};

			if(!runCmd(cmd, "preprocessing " + fa[i].getName(), true)) return false;
		}

		return true;
	}

	private boolean finalCompile(String os) throws Exception
	{
		ArrayList<String> alCmd = new ArrayList<String>();

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
		alCmd.add("--main=" + project.getMainClass());
		alCmd.add("-o");

		File fExecutable = new File(project.getOutputDir(), fileName);
		alCmd.add(fExecutable.toString());

		if(project instanceof ManagedSwtProject) alCmd.add("-Djava.library.path=.");
		if(project.getSuppressDeprecationWarnings()) alCmd.add("-Wno-deprecated");

		if(os.equals("win"))
		{
			if(project.getHideConsole()) alCmd.add("-mwindows");
			if(!addIcon()) return false;
		}

		File[] fa = outDir.listFiles();
		for(int i=0; i<fa.length; i++) // all jars
		{
			if(fa[i].getName().endsWith(".jar")) alCmd.add("-I" + fa[i].toString());
		}
		for(int i=0; i<fa.length; i++) // all .java and .class
		{
			if(fa[i].getName().endsWith(".java") || fa[i].getName().endsWith(".class")) alCmd.add(fa[i].toString());
		}
		for(int i=0; i<fa.length; i++) // all jar objects
		{
			if(fa[i].getName().endsWith(".o")) alCmd.add(fa[i].toString());
		}

		String[] saCmd = alCmd.toArray(new String[0]);
		if(!runCmd(saCmd, "main compilation step", true)) return false;

		if(!project.getOmitPacking())
		{
			String[] saCmdUpx = { CMD_UPX, "--best", fExecutable.toString() };
			if(!runCmd(saCmdUpx, "packing executable", false)) return false;
		}

		return true;
	}

	private HashSet getAllFiles()
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

	private void getFiles(File dir, HashSet<File> hs)
	{
		File[] fa = dir.listFiles();

		for(int i=0; i<fa.length; i++)
		{
			if(fa[i].isDirectory())
			{
				getFiles(fa[i], hs);
			} else
			{
				String name = fa[i].getName();
				if(name.endsWith(".java") || name.endsWith(".class")) hs.add(fa[i]);
			}
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
		fw.write("1 ICON \"" + escape(iconFile.toString()) + "\"\n");
		fw.flush();
		fw.close();

		String[] saCmd = { CMD_WINDRES, fTmp.toString(), fTmp.toString()+".o" };
		if(!runCmd(saCmd, "including icon", true)) return false;

		fTmp.delete();
		return true;
	}

	private String escape(String in)
	{
		return in.replaceAll("\\\\", "\\\\\\\\");
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
}
