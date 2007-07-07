/*
 *   GcjStubber - A stub creator for GCJ (JNC).
 *   Copyright (C) 2007  Marco Trudel <mtrudel@gmx.ch>
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

package ch.mtSystems.gcjStubber.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.mtSystems.gcjStubber.model.stubCreator.FullPublicInterfaceStubCreator;
import ch.mtSystems.gcjStubber.model.stubCreator.MinimalStubCreator;
import ch.mtSystems.gcjStubber.model.stubCreator.MinimalWithInheritanceStubCreator;
import ch.mtSystems.gcjStubber.model.stubCreator.StubCreator;


public class StubsGenerator
{
	private List<StubsGeneratorListener> listeners = new LinkedList<StubsGeneratorListener>(); 
	
	private boolean stop = false;
	private File gcjDir, stubsDir;
	private String[] compilationArguments;

	private File helloWorldDotJava;
	private int origHelloWorldDotExeSize;
	private File libgcjDotSpec, libgcjDotA, libgcjDotJar;
	private File cmdGcj, cmdAr, cmdNm;


	// --------------- public methods ---------------
	
	public void addListener(StubsGeneratorListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(StubsGeneratorListener listener)
	{
		listeners.remove(listener);
	}

	public void createStubs(File gcjDir, File stubsDir, String[] compilationArguments)
	{
		this.gcjDir = gcjDir;
		this.stubsDir = stubsDir;
		this.compilationArguments = compilationArguments;

		helloWorldDotJava = new File(stubsDir, "HelloWorld.java");

		for(StubsGeneratorListener l : listeners) l.started();
		boolean restoreLibgcjDotSpec = false;

		try
		{
			log("Checking GCJ directory... ");
			if(!checkGcj()) return;
			log("Ok\n");

			log("Checking Output directory... ");
			if(!checkStubsDir()) return;
			log("Ok\n");

			log("Extracting libgcj.a to \"" + stubsDir + "\"... ");
			if(!extractLibgcjDotA()) return;
			log("Ok\n");

			log("Creating \"HelloWorld.java\" (used for stubbing)... ");
			if(!writeHelloWorld(helloWorldDotJava)) return;
			log("Ok\n");
			
			log("Adapting libgcj.spec for stubbing... ");
			if(!updateLibgcjDotSpec(true)) return;
			restoreLibgcjDotSpec = true;
			log("Ok\n");

			log("Compile HelloWorld (for statistics)... ");
			if(!compileHelloWorld()) return;
			log("Ok\n");
			
			// create all stubs
			File[] dirContent = stubsDir.listFiles(new ObjectFileFilter());
			Arrays.sort(dirContent);
			for(int i=0; i<dirContent.length && !stop; i++)
			{
				/*if(i != 1 && i != 9 && i != 34 && i != 38 && i != 41 && i != 43 &&
						i != 78 && i != 232 && i != 235 && i != 265 && i != 277 &&
						i != 333 && i != 439 && i != 452) continue;*/

				log((i+1) + "/" + dirContent.length + ": Handling \"" + dirContent[i].getName() + "\"...\n");
				if(!createStubForObject(dirContent[i], (i+1), dirContent.length)) return;
			}
		} finally
		{
			if(restoreLibgcjDotSpec)
			{
				log("Restoring libgcj.spec... ");
				log(updateLibgcjDotSpec(false) ? "Ok\n" : "Failed!\n");
			}

			for(StubsGeneratorListener l : listeners) l.done();
			stop = false;
		}
	}
	
	public void stopCreatingStubs()
	{
		stop = true;
	}


	// --------------- private methods --------------

	/**
	 * Sends the line to all registered listeners.
	 */
	private void log(String line)
	{
		for(StubsGeneratorListener l : listeners) l.actionDone(line);
	}
	
	/**
	 * Check if the provided GCJ contains a usable GCJ.
	 * 
	 * @return true if it's ok. false otherwise.
	 */
	private boolean checkGcj()
	{
		boolean linux = System.getProperty("os.name").equals("Linux");

		libgcjDotSpec = new File(gcjDir, "lib/libgcj.spec");
		if(!libgcjDotSpec.exists())
		{
			log("Invalid:\n   libgcj.spec not found!\n");
			return false;
		}

		libgcjDotA = new File(gcjDir, "lib/libgcj.a");
		if(!libgcjDotA.exists())
		{
			log("Invalid:\n   libgcj.a not found!\n");
			return false;
		}

		libgcjDotJar = new File(gcjDir, "share/java/libgcj-4.3.0.jar");
		if(!libgcjDotJar.exists())
		{
			log("Invalid:\n   libgcj.jar not found!\n");
			return false;
		}

		String name = linux ? "gcj" : "gcj.exe";
		cmdGcj = new File(gcjDir, "bin/" + name);
		if(!cmdGcj.exists())
		{
			log("Invalid:\n   " + name + " not found!\n");
			return false;
		}
		
		name = linux ? "ar" : "ar.exe";
		cmdAr = new File(gcjDir, "bin/" + name);
		if(!cmdAr.exists())
		{
			log("Invalid:\n   " + name + " not found!\n");
			return false;
		}

		name = linux ? "nm" : "nm.exe";
		cmdNm = new File(gcjDir, "bin/" + name);
		if(!cmdNm.exists())
		{
			log("Invalid:\n   " + name + " not found!\n");
			return false;
		}

		return true;
	}
	
	/**
	 * Ensures that the output (stubs) directory exists and is empty.
	 * 
	 * @return true if it's ok. false otherwise.
	 */
	private boolean checkStubsDir()
	{
		// stubsDir needs to exist and be empty
		if(!stubsDir.exists())
		{
			if(!stubsDir.mkdirs())
			{
				log("Invalid:\n   Creating it failed!\n");
				return false;
			}
		} else
		{
			if(stubsDir.listFiles().length > 0)
			{
				log("Invalid:\n   It needs to be empty!\n");
				return false;
			}
		}

		return true;
	}
	
	private boolean extractLibgcjDotA()
	{
		try
		{
			String[] cmd = { cmdAr.toString(), "x", libgcjDotA.toString() };
			CommandExecutor commandExecutor = new CommandExecutor(cmd, stubsDir);
			commandExecutor.execute();
	
			if(commandExecutor.getOutput().length != 0 || commandExecutor.getError().length != 0)
			{
				log("Failed (1):\n");
				for(String s : commandExecutor.getOutput()) log("   [stdout] " + s + "\n");
				for(String s : commandExecutor.getError()) log("   [stderr] " + s + "\n");
				return false;
			}
			
			return true;
		} catch(Exception ex)
		{
			log("Exception occured:\n   " + ex.getMessage() + "\n");
			return false;
		}
	}
	
	private boolean writeHelloWorld(File helloWorldSource)
	{
		try
		{
			FileWriter fw = new FileWriter(helloWorldSource);
			fw.write("public class HelloWorld\n");
			fw.write("{\n");
			fw.write("  public static void main(String[] args)\n");
			fw.write("  {\n");
			fw.write("    System.out.println(\"HelloWorld\");\n");
			fw.write("  }\n");
			fw.write("}\n");
			fw.flush();
			fw.close();
			
			return true;
		} catch(Exception ex)
		{
			log("Exception occured:\n   " + ex.getMessage() + "\n");
			return false;
		}
	}
	
	private boolean compileHelloWorld()
	{
		try
		{
			File helloWorldDotExe = new File(stubsDir, "HelloWorld.exe");
			List<String> cmd = new LinkedList<String>();
			cmd.add(cmdGcj.toString());
			cmd.add("-s");
			cmd.add("--main=HelloWorld");
			cmd.add("-o" + helloWorldDotExe.toString());
			cmd.add(helloWorldDotJava.toString());
			for(File f : stubsDir.listFiles(new ObjectFileFilter())) cmd.add(f.toString());
			for(String arg : compilationArguments) cmd.add(arg);
			
			// compile
			CommandExecutor commandExecutor = new CommandExecutor(cmd.toArray(new String[0]), stubsDir);
			commandExecutor.execute();
			if(commandExecutor.getOutput().length != 0 || commandExecutor.getError().length != 0)
			{
				log("Failed (2):\n");
				for(String s : commandExecutor.getOutput()) log("   [stdout] " + s + "\n");
				for(String s : commandExecutor.getError()) log("   [stderr] " + s + "\n");
				return false;
			}
			
			// try execution
			commandExecutor = new CommandExecutor(helloWorldDotExe.toString(), stubsDir);
			commandExecutor.execute();
			if(commandExecutor.getOutput().length != 1 ||
					!commandExecutor.getOutput()[0].equals("HelloWorld") ||
					commandExecutor.getError().length != 0)
			{
				log("Failed (3):\n");
				for(String s : commandExecutor.getOutput()) log("   [stdout] " + s + "\n");
				for(String s : commandExecutor.getError()) log("   [stderr] " + s + "\n");
				return false;
			}

			origHelloWorldDotExeSize = (int)helloWorldDotExe.length();
			return true;
		} catch(Exception ex)
		{
			log("Exception occured:\n   " + ex.getMessage() + "\n");
			return false;
		}
	}

	private boolean updateLibgcjDotSpec(boolean removeGcjArchive)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(libgcjDotSpec));
			List<String> lines = new LinkedList<String>();
			for(String line = br.readLine(); line != null; line = br.readLine()) lines.add(line);
			br.close();
			
			FileWriter fileWriter = new FileWriter(libgcjDotSpec);
			boolean replaced = false;
			for(int i=0; i<lines.size(); i++)
			{
				String line = lines.get(i);

				if(removeGcjArchive)
				{
					int index = line.indexOf("-lgcj");
					if(index > -1)
					{
						fileWriter.write("## GcjStubber ## ");
						fileWriter.write(line);
						fileWriter.write("\n");

						fileWriter.write(line.replaceAll("-lgcj", ""));
						fileWriter.write("\n");

						replaced = true;
						continue;
					}
				} else
				{
					if(line.startsWith("## GcjStubber ## "))
					{
						fileWriter.write(line.substring(17));
						fileWriter.write("\n");

						i++;
						replaced = true;
						continue;
					}
				}

				fileWriter.write(line);
				fileWriter.write("\n");
			}
			fileWriter.flush();
			fileWriter.close();

			if(!replaced)
			{
				log("Library entry not found!");
				return false;
			}

			return true;
		} catch(Exception ex)
		{
			log("Exception occured:\n   " + ex.getMessage() + "\n");
			return false;
		}
	}
	
	private boolean createStubForObject(File fObj, int objectIndex, int totalCount)
	{
		File fObjTmp = new File(fObj.getParentFile(), fObj.getName()+".bak");
		int phaseProcessed = 0;
		boolean renameBack = false;

		try
		{	
			// check if the object contains java classes or only resources
			ClassesInObjectLister lister = new ClassesInObjectLister(cmdNm);
			Set<String> classesInObject = lister.getClassesInObject(fObj);
			if(classesInObject.size() == 0)
			{
				for(StubsGeneratorListener l : listeners)
				{
					l.processed(fObj.getName(), 0, -1, null, -1, objectIndex, totalCount);
				}
				return true;
			}
			phaseProcessed = 1;
			
			// remove the current object from the compilation
			if(!fObj.renameTo(fObjTmp)) throw new Exception("rename failed");
			renameBack = true;
			
			// try to compile the HelloWorld, will get the "missing references"
			File helloWorldDotExe = new File(stubsDir, "HelloWorld.exe");
			List<String> cmd = new LinkedList<String>();
			cmd.add(cmdGcj.toString());
			cmd.add("-s");
			cmd.add("-w");
			cmd.add("--main=HelloWorld");
			cmd.add("-o" + helloWorldDotExe.toString());
			cmd.add(helloWorldDotJava.toString());
			for(File f : stubsDir.listFiles(new ObjectFileFilter())) cmd.add(f.toString());
			for(String arg : compilationArguments) cmd.add(arg);

			CommandExecutor commandExecutor = new CommandExecutor(cmd.toArray(new String[0]), stubsDir);
			commandExecutor.execute();
			if(commandExecutor.getOutput().length != 0)
			{
				log("   Unexpected stdout on first compilation:\n");
				for(String s : commandExecutor.getOutput()) log("   " + s + "\n");
				return false;
			}

			String[] saError = commandExecutor.getError();
			if(saError.length == 0)
			{
				for(StubsGeneratorListener l : listeners)
				{
					l.processed(fObj.getName(), 1, -1, null, -1, objectIndex, totalCount);
				}
				return true;
			}
			phaseProcessed = 2;

			// now create the stub
			UnresolvedReferenceParser parser = new UnresolvedReferenceParser(saError, classesInObject, libgcjDotJar);
			MissingClass[] missingClasses = parser.parse();

			File stubJar = new File(fObj.getParentFile(), "stub-" + fObj.getName() + ".jar");
			File stubObject = new File(fObj.getParentFile(), "stub-" + fObj.getName() + ".a");
			cmd.add(stubObject.toString());
			cmd.add("-I");
			cmd.add(stubJar.toString());

			for(int i=0; i < 3; i++)
			{
				phaseProcessed = i + 2;

				StubCreator stubCreator;
				if(i == 0)
				{
					stubCreator = new MinimalStubCreator(missingClasses, stubJar,
							stubObject, cmdGcj, new File(stubsDir, "tmp"));
				} else if(i == 1)
				{
					stubCreator = new MinimalWithInheritanceStubCreator(missingClasses,
							stubJar, stubObject, cmdGcj, new File(stubsDir, "tmp"));
				} else if(i == 2)
				{
					stubCreator = new FullPublicInterfaceStubCreator(missingClasses,
							stubJar, stubObject, cmdGcj, new File(stubsDir, "tmp"));
				} else
				{
					throw new Exception("Can't be here?!");
				}
				stubCreator.create();

				// compile again, has to work with the created stub
				commandExecutor = new CommandExecutor(cmd.toArray(new String[0]), stubsDir);
				commandExecutor.execute();
				if(commandExecutor.getOutput().length != 0 || commandExecutor.getError().length != 0)
				{
					StringBuffer sb = new StringBuffer("Unexpected output on compilation with stub:\n");
					for(String s : commandExecutor.getOutput()) sb.append("[stdout] " + s + "\n");
					for(String s : commandExecutor.getError()) sb.append("[stderr] " + s + "\n");
					sb.deleteCharAt(sb.length()-1);
					
					for(StubsGeneratorListener l : listeners)
					{
						l.processed(fObj.getName(), phaseProcessed, 2, sb.toString(), -1, objectIndex, totalCount);
					}
					continue;
				}
	
				// check if the compiled exe works
				commandExecutor = new CommandExecutor(helloWorldDotExe.toString(), stubsDir);
				commandExecutor.execute();
				if(commandExecutor.getOutput().length != 1 ||
						!commandExecutor.getOutput()[0].equals("HelloWorld") ||
						commandExecutor.getError().length != 0)
				{
					StringBuffer sb = new StringBuffer("Unexpected HelloWorld output:\n");
					for(String s : commandExecutor.getOutput()) sb.append("[stdout] " + s + "\n");
					for(String s : commandExecutor.getError()) sb.append("[stderr] " + s + "\n");
					if(sb.length() > 0) sb.deleteCharAt(sb.length()-1);
					
					for(StubsGeneratorListener l : listeners)
					{
						l.processed(fObj.getName(), phaseProcessed, 1, sb.toString(), -1, objectIndex, totalCount);
					}
					continue;
				}

				// stub works
				int saving = origHelloWorldDotExeSize - (int)helloWorldDotExe.length();
				for(StubsGeneratorListener l : listeners)
				{
					l.processed(fObj.getName(), phaseProcessed, 0, null, saving, objectIndex, totalCount);
				}
				return true;
			}

			return true; // no stubCreator worked
		} catch(Exception ex)
		{
			//ex.printStackTrace();

			for(StubsGeneratorListener l : listeners)
			{
				l.processed(fObj.getName(), phaseProcessed, 2, "Failed with exception: " + ex.getMessage(),
						-1, objectIndex, totalCount);
			}
			return true;
		} finally
		{
			// add the current object back to the compilation
			if(renameBack && !fObjTmp.renameTo(fObj))
			{
				log("   Renaming \"" + fObjTmp.getName() + "\" back to \"" + fObj.getName() + "\" failed!!!");
				return false;
			}
		}
	}
	
	
	// --------------- Singleton ---------------
	
	private static StubsGenerator stubsGenerator = new StubsGenerator();
	
	public static StubsGenerator getStubsGenerator()
	{
		return stubsGenerator;
	}
}

class ObjectFileFilter implements FileFilter
{
	public boolean accept(File pathname)
	{
		return pathname.getName().endsWith(".o");
	}
}
