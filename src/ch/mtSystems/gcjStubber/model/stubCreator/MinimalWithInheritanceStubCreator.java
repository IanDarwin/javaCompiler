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
import java.util.Set;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import ch.mtSystems.gcjStubber.model.MissingClass;


public class MinimalWithInheritanceStubCreator extends StubCreator
{
	public MinimalWithInheritanceStubCreator(MissingClass[] missingClasses,
			File jar, File object, File cmdGcj, File tmpDir, File libgcjDotJar)
	{
		super(missingClasses, jar, object, cmdGcj, tmpDir, libgcjDotJar);
	}


	// --------------- overwritten methods ---------------

	protected void dumpClass(MissingClass missingClass, FileWriter fileWriter) throws Exception
	{
		JavaClass jc = missingClass.getJavaClass();

		// package
		fileWriter.write("package ");
		fileWriter.write(jc.getPackageName());
		fileWriter.write(";\n\n");

		// class declaration
		if(jc.isPublic()) fileWriter.write("public ");
		if(jc.isProtected()) fileWriter.write("protected ");
		if(jc.isPrivate()) fileWriter.write("private ");
		if(jc.isAbstract()) fileWriter.write("abstract ");
		if(jc.isFinal()) fileWriter.write("final ");
		if(jc.isClass()) fileWriter.write("class ");
		if(jc.isInterface()) fileWriter.write("interface ");
		fileWriter.write(missingClass.getSimpleClassName());
		fileWriter.write(" ");
		
		// inheritance
		if(jc.isClass())
		{
			fileWriter.write("extends ");
			fileWriter.write(jc.getSuperclassName());
			fileWriter.write(" ");
	
			String[] sa = jc.getInterfaceNames();
			if(sa.length > 0)
			{
				fileWriter.write("implements ");
				for(int i=0; i<sa.length; i++)
				{
					if(i>0) fileWriter.write(", ");
					fileWriter.write(sa[i]);
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
			Method superClassConstructor = getSuperclassConstructor(jc);
			String superClassConstructorCall = createSuperclassConstructorCall(superClassConstructor);

			// always add the default constructor (otherwise it's not available if there's another)
			fileWriter.write("  public ");
			fileWriter.write(missingClass.getSimpleClassName());
			fileWriter.write("() { ");
			if(superClassConstructorCall != null) fileWriter.write(superClassConstructorCall);
			fileWriter.write(" }\n");
			
			for(Method m : missingClass.getMissingMethods())
			{
				if(!m.getName().equals("<init>")) continue;

				// note: bug in bcel, constructors have a "void" return type.
				String method = methodToString(m, superClassConstructorCall);
				method = method.replace("void <init>", missingClass.getSimpleClassName());

				fileWriter.write("  ");
				fileWriter.write(method);
				fileWriter.write("\n");
			}

			fileWriter.write("\n");
		} 

		// missing methods
		Set<Method> missingMethods = missingClass.getMissingMethods();
		for(Method m : missingMethods)
		{
			if(m.getName().equals("<init>")) continue;

			fileWriter.write("  ");
			fileWriter.write(methodToString(m, null));
			fileWriter.write("\n");
		}

		// implemented abstract methods from superclasses
		for(Method m : jc.getMethods())
		{
			if(m.getName().equals("<init>")) continue; // omit constructor
			if(missingMethods.contains(m)) continue;   // omit missing method, already added
			if(!m.isPublic() && !m.isProtected()) continue;   // only handle public and protected methods
			if(!isImplementedAbstractMethod(jc, m)) continue; // is implemented abstract method?

			fileWriter.write("  ");
			fileWriter.write(methodToString(m, null));
			fileWriter.write("\n");
		}

		// finish the class
		fileWriter.write("}\n");
	}


	// --------------- private methods ---------------

	private Method getSuperclassConstructor(JavaClass jc) throws Exception
	{
		String superClassName = jc.getSuperclassName();
		String fileName = superClassName.replaceAll("\\.", "/") + ".class";
		JavaClass jcSuper = (new ClassParser(libgcjDotJar.toString(), fileName)).parse();

		// search default constructor
		for(Method m : jcSuper.getMethods())
		{
			if(m.isPrivate() || !m.getName().equals("<init>")) continue;
			if(!jcSuper.getPackageName().equals(jc.getPackageName()) &&
					!m.isPublic() && !m.isProtected()) continue;
			if(m.getArgumentTypes().length > 0) continue;

			return null;
		}

		// otherwise, take a random one
		for(Method m : jcSuper.getMethods())
		{
			if(m.isPrivate() || !m.getName().equals("<init>")) continue;
			if(!jcSuper.getPackageName().equals(jc.getPackageName()) &&
					!m.isPublic() && !m.isProtected()) continue;

			return m;
		}

		throw new Exception("Not possible! Classes always have at least one constructor!");
	}
	
	private String createSuperclassConstructorCall(Method superClassConstructor)
	{
		if(superClassConstructor == null) return null;

		StringBuffer sb = new StringBuffer("super(");
		Type[] ta = superClassConstructor.getArgumentTypes();

		for(int i=0; i<ta.length; i++)
		{
			if(i > 0) sb.append(", ");
			sb.append(createDummyValue(ta[i]));
		}

		sb.append(");");
		return sb.toString();
	}
	
	private boolean isImplementedAbstractMethod(JavaClass jc, Method m) throws Exception
	{
		String superClassName = jc.getSuperclassName();
		if(superClassName.equals("java.lang.Object")) return false;
		
		String fileName = superClassName.replaceAll("\\.", "/") + ".class";
		JavaClass jcSuper = (new ClassParser(libgcjDotJar.toString(), fileName)).parse();
		
		for(Method mSuper : jcSuper.getMethods())
		{
			if(signatureMatches(m, mSuper)) return true;
		}
		return false;
	}
	
	private boolean signatureMatches(Method m1, Method m2)
	{	
		if(!m1.getName().equals(m2.getName())) return false;

		Type[] ta1 = m1.getArgumentTypes();
		Type[] ta2 = m2.getArgumentTypes();
		if(ta1.length != ta2.length) return false;

		for(int i=0; i<ta1.length; i++)
		{
			boolean equalType = ta1[i].toString().equals(ta2[i].toString());
			if(!equalType) return false;
		}

		return true;
	}
}
