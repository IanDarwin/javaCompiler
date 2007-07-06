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


public class StubsGenerator
{
	private List<StubsGeneratorListener> listeners = new LinkedList<StubsGeneratorListener>(); 
	
	private boolean stop = false;
	private File gcjDir, stubsDir;
	private String[] compilationArguments;
	
	private File helloWorldDotJava;
	private File libgcjDotSpec, libgcjDotA, libgcjDotJar;
	private File cmdGcj, cmdAr, cmdNm;
	
	
	public StubsGenerator(File gcjDir, File stubsDir, String[] compilationArguments)
	{
		this.gcjDir = gcjDir;
		this.stubsDir = stubsDir;
		this.compilationArguments = compilationArguments;

		helloWorldDotJava = new File(stubsDir, "HelloWorld.java");
	}


	// --------------- public methods ---------------
	
	public void addListener(StubsGeneratorListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(StubsGeneratorListener listener)
	{
		listeners.remove(listener);
	}

	public void createStubs()
	{
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
				//if(i < 20) continue;

				log((i+1) + "/" + dirContent.length + ": Handling \"" + dirContent[i].getName() + "\"... ");
				if(!createStubForObject(dirContent[i])) return;
				for(StubsGeneratorListener l : listeners) l.progress(i+1, dirContent.length);
			}
		} finally
		{
			if(restoreLibgcjDotSpec)
			{
				log("Restoring libgcj.spec... ");
				log(updateLibgcjDotSpec(false) ? "Ok\n" : "Failed!\n");
			}

			for(StubsGeneratorListener l : listeners) l.done();
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
		for(StubsGeneratorListener l : listeners) l.log(line);
	}
	
	/**
	 * Check if the provided GCJ contains a usable GCJ.
	 * 
	 * @return true if it's ok. false otherwise.
	 */
	private boolean checkGcj()
	{
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

		cmdGcj = new File(gcjDir, "bin/gcj.exe");
		if(!cmdGcj.exists())
		{
			log("Invalid:\n   gcj.exe not found!\n");
			return false;
		}
		
		cmdAr = new File(gcjDir, "bin/ar.exe");
		if(!cmdAr.exists())
		{
			log("Invalid:\n   ar.exe not found!\n");
			return false;
		}
		
		cmdNm = new File(gcjDir, "bin/nm.exe");
		if(!cmdNm.exists())
		{
			log("Invalid:\n   nm.exe not found!\n");
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
			cmd.add("*.o");
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

						fileWriter.write(line.substring(0, index));
						fileWriter.write(line.substring(index+5));
						fileWriter.write("\n");

						continue;
					}
				} else
				{
					if(line.startsWith("## GcjStubber ## "))
					{
						fileWriter.write(line.substring(17));
						fileWriter.write("\n");

						i++;
						continue;
					}
				}

				fileWriter.write(line);
				fileWriter.write("\n");
			}
			fileWriter.flush();
			fileWriter.close();

			return true;
		} catch(Exception ex)
		{
			log("Exception occured:\n   " + ex.getMessage() + "\n");
			return false;
		}
	}
	
	private boolean createStubForObject(File fObj)
	{
		File fObjTmp = new File(fObj.getParentFile(), fObj.getName()+".bak");
		boolean renameBack = false;

		try
		{	
			// check if the object contains java classes or only resources
			ClassesInObjectLister lister = new ClassesInObjectLister(cmdNm);
			Set<String> classesInObject = lister.getClassesInObject(fObj);
			if(classesInObject.size() == 0)
			{
				log("Skipped, no Java classes.\n");
				return true;
			}
			
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
			cmd.add("*.o");
			for(String arg : compilationArguments) cmd.add(arg);

			CommandExecutor commandExecutor = new CommandExecutor(cmd.toArray(new String[0]), stubsDir);
			commandExecutor.execute();
			if(commandExecutor.getOutput().length != 0)
			{
				log("Unexpected stdout on first compilation:\n");
				for(String s : commandExecutor.getOutput()) log("   " + s + "\n");
				return false;
			}
			
			String[] saError = commandExecutor.getError();
			if(saError.length == 0)
			{
				log("Skipped, won't be pulled in.\n");
				if(!helloWorldDotExe.delete()) throw new Exception("Unable to delete HelloWorld!");
				return true;
			}

			// now create the stub
			UnresolvedReferenceParser parser = new UnresolvedReferenceParser(saError, classesInObject, libgcjDotJar);
			MissingClass[] missingClasses = parser.parse();

			File stubJar = new File(fObj.getParentFile(), "stub-" + fObj.getName() + ".jar");
			File stubObject = new File(fObj.getParentFile(), "stub-" + fObj.getName() + ".a");
			MinimalStubCreator stubCreator = new MinimalStubCreator(missingClasses, stubJar, stubObject,
					cmdGcj, new File(stubsDir, "tmp"));
			stubCreator.create();

			// compile again, has to work with the created stub
			cmd.add(stubObject.toString());
			cmd.add("-I");
			cmd.add(stubJar.toString());
			commandExecutor = new CommandExecutor(cmd.toArray(new String[0]), stubsDir);
			commandExecutor.execute();
			if(commandExecutor.getOutput().length != 0 || commandExecutor.getError().length != 0)
			{
				log("Unexpected output on compilation with stub:\n");
				for(String s : commandExecutor.getOutput()) log("   [stdout] " + s + "\n");
				for(String s : commandExecutor.getError()) log("   [stderr] " + s + "\n");
				return false;
			}

			// check if the compiled exe works
			commandExecutor = new CommandExecutor(helloWorldDotExe.toString(), stubsDir);
			commandExecutor.execute();
			if(commandExecutor.getOutput().length != 1 ||
					!commandExecutor.getOutput()[0].equals("HelloWorld") ||
					commandExecutor.getError().length != 0)
			{
				log("Phase1 Failed\n");
				//for(String s : commandExecutor.getOutput()) log("   [stdout] " + s + "\n");
				//for(String s : commandExecutor.getError()) log("   [stderr] " + s + "\n");
				return true; //TODO: Phase 2 and 3, report output and error output
			}

			log("Ok\n");
			return true;
		} catch(Exception ex)
		{
			ex.printStackTrace();
			log("Failed with exception:\n   " + ex.getMessage() + "\n");
			return true;
		} finally
		{
			// add the current object back to the compilation
			if(renameBack && !fObjTmp.renameTo(fObj))
			{
				log("   Renaming back failed!!!");
				return false;
			}
		}
	}
}

class ObjectFileFilter implements FileFilter
{
	public boolean accept(File pathname)
	{
		return pathname.getName().endsWith(".o");
	}
}
