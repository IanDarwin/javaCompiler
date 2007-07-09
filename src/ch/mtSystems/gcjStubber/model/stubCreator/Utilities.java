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
import java.util.regex.Pattern;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import ch.mtSystems.gcjStubber.model.MissingClass;


/**
 * Additional useful utilities for the stub creators.
 */
public class Utilities
{
	/**
	 * Checks if a class is static. Only applies to inner classes.
	 * 
	 * @param jc The class to check wether it is static or not.
	 * @return true if it is static, false otherwise.
	 */
	public static boolean isClassStatic(JavaClass jc)
	{
		Pattern p = Pattern.compile("InnerClass:.*static.*" + jc.getClassName().replaceAll("\\$", "\\\\\\$"));
		return p.matcher(jc.toString()).find();
	}
	
	/**
	 * Creates a dummy value for the given type.
	 * 
	 * @param type The type to create a dummy value for.
	 * @return The created dummy value.
	 */
	public static String createDummyValue(Type type)
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

	/**
	 * Creates a constructor call (e.g. super(true);) for the parent of given class.
	 * 
	 * @param jc The class that needs a constructor call for it's superclass.
	 * @param missingClasses The missing classes to check if the parent class is in the created stub.
	 * @param libgcjDotJar libgcj.jar to load the parent class.
	 * @return A superclass constructor call or null, if there's a default constructor.
	 * @throws Exception Thrown if no constructor can be found or the parent class can't be loaded.
	 */
	public static String createSuperclassConstructor(JavaClass jc, MissingClass[] missingClasses, File libgcjDotJar) throws Exception
	{
		String superClassName = jc.getSuperclassName();

		// check if the superclass is part of the stub. if yes, there's always a default constructor
		for(MissingClass missingClass : missingClasses)
		{
			if(superClassName.startsWith(missingClass.getClassName())) return null;
		}
		
		// otherwise, get a constructor from the real class
		String fileName = superClassName.replaceAll("\\.", "/") + ".class";
		JavaClass jcSuper = (new ClassParser(libgcjDotJar.toString(), fileName)).parse();

		// search default constructor
		for(Method m : jcSuper.getMethods())
		{
			if(m.isPrivate() || !m.getName().equals("<init>")) continue;
			if(!jcSuper.getPackageName().equals(jc.getPackageName()) &&
					!m.isPublic() && !m.isProtected()) continue;

			if(m.getArgumentTypes().length == 0 ||
					(m.getArgumentTypes().length == 1 && Utilities.removeFirstArgument(jcSuper, m)))
			{
				return null;
			}
		}

		// otherwise, take a random one
		for(Method m : jcSuper.getMethods())
		{
			if(m.isPrivate() || !m.getName().equals("<init>")) continue;
			if(!jcSuper.getPackageName().equals(jc.getPackageName()) &&
					!m.isPublic() && !m.isProtected()) continue;

			StringBuffer sb = new StringBuffer("super(");
			Type[] ta = m.getArgumentTypes();

			for(int i=0; i<ta.length; i++)
			{
				if(i > 0) sb.append(", ");
				sb.append(createDummyValue(ta[i]));
			}

			sb.append(");");
			return sb.toString();
		}

		throw new Exception("Not possible! Classes always have at least one constructor!");
	}

	/**
	 * Tests if the signature (name and argument types) of two methods match.
	 * 
	 * @param m1 One method.
	 * @param m2 Another method.
	 * @return true if the signature matches, false otherwise.
	 */
	public static boolean signatureMatches(Method m1, Method m2)
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
	
	/**
	 * Checks if the first argument from the given constructor should be removed
	 * because it's a inner class. There the first argument will be the reference
	 * to the outher class.
	 *  
	 * @param jc The class of the constructor.
	 * @param constructor The constructor to check.
	 * @return true if the given method is a constructor and the first argument
	 *              has to be removed. false otherwise.
	 */
	public static boolean removeFirstArgument(JavaClass jc, Method constructor)
	{
		Type[] argTypes = constructor.getArgumentTypes();

		return (constructor.getName().equals("<init>") &&
				jc.getClassName().indexOf('$') > -1 &&
				argTypes.length > 0 &&
				jc.getClassName().startsWith(argTypes[0].toString())); 
	}
}
