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

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import ch.mtSystems.gcjStubber.model.MissingClass;


/**
 * MinimalStubCreator creates dummy java classes with the missing
 * references solved and additionally keeping the inheritance structure.
 */
public class MinimalWithInheritanceStubCreator extends StubCreator
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
	public MinimalWithInheritanceStubCreator(MissingClass[] missingClasses,
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
		Set<Field> fieldSet = missingClass.getMissingFields();
		for(Field f : fieldSet)
		{
			fileWriter.write("  ");
			fileWriter.write(fieldToString(f));
			fileWriter.write("\n");
		}
		if(fieldSet.size() > 0) fileWriter.write("\n");
		
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
			
			for(Method m : missingClass.getMissingMethods())
			{
				if(!m.getName().equals("<init>") ||
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

		List<Method> addedMethods = new LinkedList<Method>();
		
		// missing methods
		for(Method m : missingClass.getMissingMethods())
		{
			if(m.getName().equals("<init>")) continue;

			fileWriter.write("  ");
			fileWriter.write(methodToString(jc, m, null));
			fileWriter.write("\n");

			addedMethods.add(m);
		}

		// implemented abstract methods from superclass and interfaces
		for(Method m : jc.getMethods())
		{
			if(m.getName().equals("<init>")) continue; // omit constructor
			if(!m.isPublic() && !m.isProtected()) continue;   // only handle public and protected methods
			if(!isImplementedAbstractMethod(jc, m)) continue; // is implemented abstract method?

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


	// --------------- private methods ---------------
	
	private boolean isImplementedAbstractMethod(JavaClass jc, Method m) throws Exception
	{
		List<String> superClassNames = new LinkedList<String>(); // superclass and interfaces
		
		superClassNames.add(jc.getSuperclassName());
		for(String interfaceClassName : jc.getInterfaceNames()) superClassNames.add(interfaceClassName);
		
		while(!superClassNames.isEmpty())
		{
			String superClassName = superClassNames.remove(0);
			if(superClassName.equals("java.lang.Object")) continue;

			String fileName = superClassName.replaceAll("\\.", "/") + ".class";
			JavaClass jcSuper = (new ClassParser(libgcjDotJar.toString(), fileName)).parse();
			
			superClassNames.add(jcSuper.getSuperclassName());
			for(String interfaceClassName : jcSuper.getInterfaceNames()) superClassNames.add(interfaceClassName);
			
			for(Method mSuper : jcSuper.getMethods())
			{
				if(Utilities.signatureMatches(m, mSuper)) return true;
			}
		}

		return false;
	}
}
