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
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import ch.mtSystems.gcjStubber.model.MissingClass;


/**
 * FullPublicInterfaceStubCreator creates dummy java classes with the
 * complete public interface of the stubbed classes.
 */
public class FullPublicInterfaceStubCreator extends StubCreator
{
	/**
	 * Create a new stubcreator.
	 * 
	 * @param missingClasses The missing classes to create stubs for.
	 * @param jar The jar file where to save the created bytecode.
	 * @param object The file where to save the created stub object.
	 * @param cmdGcj GCJ to compile java source to bytecode and to an object.
	 * @param tmpDir The temporary directory that can be used as wished.
	 * @param libgcjDotJar libgcj.jar to get additional informations about the real classes.
	 * @param excludedClasses The excluded classes from the compilation.
	 */
	public FullPublicInterfaceStubCreator(MissingClass[] missingClasses,
			File jar, File object, File cmdGcj, File tmpDir, File libgcjDotJar, Set<String> excludedClasses)
	{
		super(missingClasses, jar, object, cmdGcj, tmpDir, libgcjDotJar, excludedClasses);
	}


	// --------------- overwritten methods ---------------

	protected void dumpClass(MissingClass missingClass, FileWriter fileWriter, boolean isInnerClass) throws Exception
	{
		JavaClass jc = missingClass.getJavaClass();

		// package
		if(!isInnerClass)
		{
			fileWriter.write("package ");
			fileWriter.write(jc.getPackageName());
			fileWriter.write(";\n\n");
		}
		
		// class declaration
		if(jc.isPublic()) fileWriter.write("public ");
		if(jc.isProtected()) fileWriter.write("protected ");
		if(jc.isPrivate()) fileWriter.write("private ");
		if(jc.isStatic() || (isInnerClass && Utilities.isClassStatic(jc))) fileWriter.write("static "); 
		if(jc.isAbstract()) fileWriter.write("abstract ");
		if(jc.isFinal()) fileWriter.write("final ");
		if(jc.isClass()) fileWriter.write("class ");
		if(jc.isInterface()) fileWriter.write("interface ");
		fileWriter.write(missingClass.getSimpleClassName());
		fileWriter.write(" ");

		// inheritance
		if(jc.isClass())
		{
			ensureCreated(jc.getSuperclassName());
			fileWriter.write("extends ");
			fileWriter.write(jc.getSuperclassName().replaceAll("\\$", "."));
			fileWriter.write(" ");
	
			String[] sa = jc.getInterfaceNames();
			if(sa.length > 0)
			{
				fileWriter.write("implements ");
				for(int i=0; i<sa.length; i++)
				{
					ensureCreated(sa[i]);

					if(i>0) fileWriter.write(", ");
					fileWriter.write(sa[i].replaceAll("\\$", "."));
				}
			}
		}
		if(jc.isInterface())
		{
			String[] sa = jc.getInterfaceNames();
			if(sa.length > 0)
			{
				fileWriter.write("extends ");
				for(int i=0; i<sa.length; i++)
				{
					if(i>0) fileWriter.write(", ");
					fileWriter.write(sa[i]);
				}
			}
		}
		fileWriter.write("\n");

		// start the class
		fileWriter.write("{\n");

		// fields
		for(Field f : jc.getFields())
		{
			if(!f.isPublic()) continue;

			fileWriter.write("  ");
			fileWriter.write(fieldToString(f));
			fileWriter.write("\n");
		}
		if(jc.getFields().length > 0) fileWriter.write("\n");
		
		// constructors
		if(jc.isClass())
		{
			// get superclass constructor
			String superClassConstructorCall = Utilities.createSuperclassConstructor(jc, missingClasses, libgcjDotJar);

			// always add the default constructor (otherwise it's not available if there's another)
			fileWriter.write("  public ");
			fileWriter.write(missingClass.getSimpleClassName());
			fileWriter.write("() { ");
			if(superClassConstructorCall != null) fileWriter.write(superClassConstructorCall);
			fileWriter.write(" }\n");

			for(Method m : jc.getMethods())
			{
				if((!m.isPublic() && !m.isProtected()) ||
						!m.getName().equals("<init>") ||
						m.getArgumentTypes().length == 0 ||
						(Utilities.removeFirstArgument(jc, m) && m.getArgumentTypes().length == 1)) continue;

				// note: bug in bcel, constructors have a "void" return type.
				String method = methodToString(jc, m, superClassConstructorCall);
				method = method.replace("void <init>", missingClass.getSimpleClassName());

				fileWriter.write("  ");
				fileWriter.write(method);
				fileWriter.write("\n");
			}

			fileWriter.write("\n");
		}
		
		// methods
		List<Method> addedMethods = new LinkedList<Method>();
		for(Method m : jc.getMethods())
		{
			if((!m.isPublic() && !m.isProtected()) || m.getName().equals("<init>")) continue;

			boolean alreadyAdded = false;
			for(Method addedMethod : addedMethods)
			{
				if(Utilities.signatureMatches(addedMethod, m))
				{
					alreadyAdded = true;
					break;
				}
			}
			if(alreadyAdded) continue;

			fileWriter.write("  ");
			fileWriter.write(methodToString(jc, m, null));
			fileWriter.write("\n");

			addedMethods.add(m);
		}

		// inner classes
		fileWriter.write("\n");
		for(MissingClass innerClass : missingClass.getInnerClasses())
		{
			dumpClass(innerClass, fileWriter, true);
		}

		// finish the class
		fileWriter.write("}\n");
	}
}
