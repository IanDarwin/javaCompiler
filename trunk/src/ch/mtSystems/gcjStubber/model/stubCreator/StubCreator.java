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

package ch.mtSystems.gcjStubber.model.stubCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import ch.mtSystems.gcjStubber.model.CommandExecutor;
import ch.mtSystems.gcjStubber.model.MissingClass;
import ch.mtSystems.jnc.model.utilities.FileUtilities;


public abstract class StubCreator
{
	private File jar;
	private File object;
	private File cmdGcj;
	private File tmpDir;

	private Set<String> excludedClasses;
	private Set<String> hiddenMissingClassNames = new HashSet<String>();

	protected MissingClass[] missingClasses;
	protected File libgcjDotJar;


	public StubCreator(MissingClass[] missingClasses, File jar, File object,
			File cmdGcj, File tmpDir, File libgcjDotJar, Set<String> excludedClasses)
	{
		this.missingClasses = missingClasses;
		this.jar = jar;
		this.object = object;
		this.cmdGcj = cmdGcj;
		this.tmpDir = tmpDir;
		this.libgcjDotJar = libgcjDotJar;
		this.excludedClasses = excludedClasses;
	}


	// --------------- public methods ---------------

	public final void create() throws Exception
	{
		try
		{
			// create all .java files
			for(MissingClass missingClass : missingClasses)
			{
				File sourceFile = new File(tmpDir, missingClass.getClassName().replaceAll("\\.", "/") + ".java");
				if(!sourceFile.getParentFile().exists() && !sourceFile.getParentFile().mkdirs())
				{
					throw new Exception("Can't create directory: \"" + sourceFile.getParentFile() + "\"!");
				}
				
				FileWriter fileWriter = new FileWriter(sourceFile);
				dumpClass(missingClass, fileWriter, false);
				fileWriter.flush();
				fileWriter.close();
			}
			
			// also create all hidden missing classes
			Set<MissingClass> hiddenMissingClasses = new LinkedHashSet<MissingClass>();
			for(String className : hiddenMissingClassNames)
			{
				int index = className.indexOf('$');
				if(index == -1)
				{
					hiddenMissingClasses.add(new MissingClass(className, libgcjDotJar));
				} else
				{
					String parentClassName = className.substring(0, index);
					MissingClass parentClass = null;
					
					// check if the parent class already exists in the missing classes
					for(MissingClass missingClass : missingClasses)
					{
						if(missingClass.getClassName().equals(parentClassName))
						{
							parentClass = missingClass;
							hiddenMissingClasses.add(parentClass);
							break;
						}
					}

					if(parentClass == null)
					{
						// check if the parent was already added
						for(MissingClass missingClass : hiddenMissingClasses)
						{
							if(missingClass.getClassName().equals(parentClassName))
							{
								parentClass = missingClass;
								break;
							}
						}
					}
					
					if(parentClass == null)
					{
						parentClass = new MissingClass(parentClassName, libgcjDotJar);
						hiddenMissingClasses.add(parentClass);
					}
					
					parentClass.addMissingInnerClass(new MissingClass(className, libgcjDotJar));
				}
			}

			for(MissingClass missingClass : hiddenMissingClasses)
			{
				File sourceFile = new File(tmpDir, missingClass.getClassName().replaceAll("\\.", "/") + ".java");
				if(!sourceFile.getParentFile().exists() && !sourceFile.getParentFile().mkdirs())
				{
					throw new Exception("Can't create directory: \"" + sourceFile.getParentFile() + "\"!");
				}

				FileWriter fileWriter = new FileWriter(sourceFile);
				dumpClass(missingClass, fileWriter, false);
				fileWriter.flush();
				fileWriter.close();
			}

			// compile all .java to .class files
			List<String> cmd = new LinkedList<String>();
			cmd.add(cmdGcj.toString());
			cmd.add("-s");
			cmd.add("-w");
			cmd.add("-O2");
			cmd.add("-C");
			for(File f : listFiles(tmpDir)) cmd.add(f.toString());
	
			CommandExecutor commandExecutor = new CommandExecutor(cmd.toArray(new String[0]), tmpDir);
			commandExecutor.execute();
			if(commandExecutor.getOutput().length > 0 || commandExecutor.getError().length > 0)
			{
				StringBuffer sb = new StringBuffer("Compiling java source files failed:\n");
				for(String s : commandExecutor.getOutput()) { sb.append("   [stdout] "); sb.append(s); sb.append("\n"); }
				for(String s : commandExecutor.getError()) { sb.append("   [stderr] "); sb.append(s); sb.append("\n"); }
				throw new Exception(sb.toString());
			}
	
			// create a jar from the .class files
			ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(jar));
			byte[] buffer = new byte[2048];
	
			for(File f : listFiles(tmpDir))
			{
				if(!f.getName().endsWith(".class")) continue;
	
				String entryName = f.toString().substring(tmpDir.toString().length()+1).replaceAll("\\\\", "/");
				ZipEntry zipEntry = new ZipEntry(entryName);
				outputStream.putNextEntry(zipEntry);
	
				InputStream inputStream = new FileInputStream(f);
				while(true)
				{
					int len = inputStream.read(buffer);
					if(len < 0) break;
					outputStream.write(buffer, 0, len);
				}
				inputStream.close();
	
				outputStream.closeEntry();
			}
			outputStream.flush();
			outputStream.close();
			
			// compile the jar to an object
			cmd.clear();
			cmd.add(cmdGcj.toString());
			cmd.add("-s");
			cmd.add("-O2");
			cmd.add("-c");
			cmd.add(jar.toString());
			cmd.add("-o");
			cmd.add(object.toString());
	
			commandExecutor = new CommandExecutor(cmd.toArray(new String[0]), tmpDir);
			commandExecutor.execute();
			if(commandExecutor.getOutput().length > 0 || commandExecutor.getError().length > 0)
			{
				StringBuffer sb = new StringBuffer("Compiling the jar failed:\n");
				for(String s : commandExecutor.getOutput()) { sb.append("   [stdout] "); sb.append(s); sb.append("\n"); }
				for(String s : commandExecutor.getError()) { sb.append("   [stderr] "); sb.append(s); sb.append("\n"); }
				throw new Exception(sb.toString());
			}
		} finally
		{
			// clean up tmp dir
			if(tmpDir.exists())
			{
				File ret = FileUtilities.deleteDirRecursively(tmpDir);
				if(ret != null) throw new Exception("Unable to clean up tmp dir (" + ret + ")!");
			}
		}
	}


	// --------------- protected methods ---------------

	protected abstract void dumpClass(MissingClass missingClass, FileWriter fileWriter, boolean isInnerClass) throws Exception;
	
	protected String fieldToString(Field field)
	{
		StringBuffer sb = new StringBuffer();

		// access modifier
		if(field.isPublic()) sb.append("public ");
		if(field.isProtected()) sb.append("protected ");
		if(field.isPrivate()) sb.append("private ");
		if(field.isAbstract()) sb.append("abstract ");
		if(field.isFinal()) sb.append("final ");
		if(field.isStatic()) sb.append("static ");

		// type and name
		sb.append(field.getType().toString().replaceAll("\\$", "."));
		sb.append(" ");
		sb.append(field.getName());
		
		// default initialization
		sb.append(" = ");
		sb.append(createDummyValue(field.getType()));
		sb.append(";");

		return sb.toString();
	}
	
	protected String methodToString(Method method, String body)
	{
		StringBuffer sb = new StringBuffer();

		// access modifier
		if(method.isPublic()) sb.append("public ");
		if(method.isProtected()) sb.append("protected ");
		if(method.isPrivate()) sb.append("private ");
		if(method.isAbstract()) sb.append("abstract ");
		if(method.isFinal()) sb.append("final ");
		if(method.isStatic()) sb.append("static ");
		if(method.isNative()) sb.append("native ");

		// return value and name
		sb.append(method.getReturnType());
		sb.append(" ");
		sb.append(method.getName());

		// name and arguments
		sb.append("(");
		Type[] argumentTypes = method.getArgumentTypes();
		for(int i=0; i<argumentTypes.length; i++)
		{
			ensureCreated(argumentTypes[i].toString());

			if(i > 0) sb.append(", ");
			sb.append(argumentTypes[i].toString().replaceAll("\\$", "."));
			sb.append(" arg");
			sb.append(i);
		}
		sb.append(")");

		// body
		if(method.isNative())
		{
			sb.append(";");
		} else if(body != null)
		{
			sb.append(" { ");
			sb.append(body);
			sb.append(" }");
		} else if(method.getReturnType().equals(Type.VOID))
		{
			sb.append(" { }");
		} else
		{
			sb.append(" { return ");
			sb.append(createDummyValue(method.getReturnType()));
			sb.append("; }");
		}

		return sb.toString();
	}
	
	protected String createDummyValue(Type type)
	{
		if(type.equals(Type.VOID))    return null;
		if(type.equals(Type.BOOLEAN)) return "true";
		if(type.equals(Type.BYTE))    return "(byte)23";
		if(type.equals(Type.CHAR))    return "'2'";
		if(type.equals(Type.DOUBLE))  return "23";
		if(type.equals(Type.FLOAT))   return "23";
		if(type.equals(Type.INT))     return "23";
		if(type.equals(Type.LONG))    return "23";
		if(type.equals(Type.SHORT))   return "(short)23";
		return "(" + type.toString().replaceAll("\\$", ".") + ")null";
	}

	protected void ensureCreated(String className)
	{
		boolean excludedClass = false;
		for(String excludedClassName : excludedClasses)
		{
			// also supports inner classes:
			if(className.startsWith(excludedClassName))
			{
				excludedClass = true;
				break;
			}
		}
		if(!excludedClass) return;

		int index = className.indexOf('$');
		if(index > -1)
		{
			String parentClassName = className.substring(0, index);
			for(MissingClass missingClass : missingClasses)
			{
				if(missingClass.getClassName().equals(parentClassName))
				{
					if(missingClass.getInnerClass(className) != null) return;
				}
			}
			
		} else
		{
			for(MissingClass missingClass : missingClasses)
			{
				if(missingClass.getClassName().equals(className)) return;
			}
		}
		
		hiddenMissingClassNames.add(className);
	}


	// --------------- private methods ---------------

	private File[] listFiles(File dir)
	{
		if(!dir.exists()) return new File[0];

		Set<File> fileSet = new LinkedHashSet<File>();
		List<File> dirList = new LinkedList<File>();
		dirList.add(dir);

		while(!dirList.isEmpty())
		{
			for(File f : dirList.remove(0).listFiles())
			{
				if(f.isDirectory()) dirList.add(f);
				else                fileSet.add(f);
			}
		}

		return fileSet.toArray(new File[0]);
	}
}
