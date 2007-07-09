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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;


/**
 * MissingClass represents a missing class from compilation with excluded object
 * (undefined reference) and contains all missed fields, constructors, methods
 * and inner classes.
 */
public class MissingClass
{
	private File libgcjDotJar;
	private String simpleClassName;
	private JavaClass jc;
	
	private Set<MissingClass> innerClassSet = new HashSet<MissingClass>();
	private Set<Field> fieldSet = new HashSet<Field>();
	private Set<Method> methodSet = new HashSet<Method>(); // constructors and methods


	/**
	 * Create a new instance.
	 * 
	 * @param className The name of the class.
	 * @param libgcjDotJar libgcj.jar. It will be used to gather complete information about the class.
	 * @throws Exception Thrown if reading from libgcj.jar fails.
	 */
	public MissingClass(String className, File libgcjDotJar) throws Exception
	{
		this.libgcjDotJar = libgcjDotJar;

		String fileName = className.replaceAll("\\.", "/") + ".class";
		//System.err.println("loading " + fileName);
		jc = (new ClassParser(libgcjDotJar.toString(), fileName)).parse();

		simpleClassName = jc.getClassName();
		int index = Math.max(simpleClassName.lastIndexOf('.'), simpleClassName.lastIndexOf('$'));
		simpleClassName = simpleClassName.substring(index+1);
	}


	// --------------- public methods ---------------

	/**
	 * Returns the bcel JavaClass of this missing class.
	 * 
	 * @return The bcel JavaClass of this missing class.
	 */
	public JavaClass getJavaClass()
	{
		return jc;
	}

	/**
	 * Returns the full name of the class. E.g. foo.bar.Test$InnerClass.
	 * 
	 * @return The full name of the class.
	 */
	public String getClassName()
	{
		return jc.getClassName();
	}
	
	/**
	 * Returns the simple name of the class. E.g. foo.bar.Test$InnerClass
	 * will return InnerClass.
	 * 
	 * @return The simple name of the class.
	 */
	public String getSimpleClassName()
	{
		return simpleClassName;
	}

	/**
	 * Add a missing inner class to this minimal class. If the class is nested into more
	 * inner classes, all levels will be created. The final inner class is returned.
	 * 
	 * @param innerClassName The name of the inner class.
	 * @return The most inner created MissingClass.
	 * @throws Exception Thrown if creating a MissingClass fails.
	 */
	public MissingClass addMissingInnerClass(String innerClassName) throws Exception
	{
		int endIndex = innerClassName.indexOf('$', getClassName().length()+1);
		if(endIndex == -1)
		{
			MissingClass innerClass = new MissingClass(innerClassName, libgcjDotJar);
			innerClassSet.add(innerClass);
			return innerClass;
		} else
		{
			String myInnerClassName = innerClassName.substring(0, endIndex);
			for(MissingClass innerClass : innerClassSet)
			{
				if(innerClass.getClassName().equals(myInnerClassName))
				{
					return innerClass.addMissingInnerClass(innerClassName);
				}
			}

			MissingClass innerClass = new MissingClass(innerClassName.substring(0, endIndex), libgcjDotJar);
			innerClassSet.add(innerClass);
			return innerClass.addMissingInnerClass(innerClassName);
		} 
	}
	
	/**
	 * Returns all missing inner classes. Please note that this will not flatten
	 * the inner classes. Only the direct inner classes are returned.
	 * 
	 * @return All missing inner classes.
	 */
	public Set<MissingClass> getInnerClasses()
	{
		return innerClassSet;
	}
	
	/**
	 * Returns the inner class with the giving name or null, if it doesn't exist.
	 * Please note that this method will search through the complete hirarchy.
	 * 
	 * @param innerClassName The name of the inner class.
	 * @return The inner class with the giving name or null, if it doesn't exist.
	 */
	public MissingClass getInnerClass(String innerClassName)
	{
		int endIndex = innerClassName.indexOf('$', getClassName().length()+1);
		if(endIndex == -1)
		{
			for(MissingClass innerClass : innerClassSet)
			{
				if(innerClass.getClassName().equals(innerClassName)) return innerClass;
			}
		} else
		{
			String myInnerClassName = innerClassName.substring(0, endIndex);
			for(MissingClass innerClass : innerClassSet)
			{
				if(innerClass.getClassName().equals(myInnerClassName))
				{
					return innerClass.getInnerClass(innerClassName);
				}
			}
		}

		return null;
	}
	
	/**
	 * Adds a missing constructor with the given argument types.
	 * 
	 * @param argTypes The argument types.
	 * @throws Exception Thrown if the constructor doesn't exist in the real class.
	 */
	public void addMissingConstructor(String[] argTypes) throws Exception
	{
		addMissingMethod("<init>", argTypes);
	}

	/**
	 * Adds a missing method with the given name and argument types.
	 * 
	 * @param methodName The name of the method.
	 * @param argTypes The argument types of the method.
	 * @throws Exception Thrown if the method doesn't exist in the real class.
	 */
	public void addMissingMethod(String methodName, String[] argTypes) throws Exception
	{
		// fix some namings first
		for(int i=0; i<argTypes.length; i++)
		{
			if(argTypes[i].equals("bool"))
			{
				argTypes[i] = "boolean";
			} else if(argTypes[i].equals("bool[]"))
			{
				argTypes[i] = "boolean[]";
			} else if(argTypes[i].equals("long long"))
			{
				argTypes[i] = "long";
			} else if(argTypes[i].equals("long long[]"))
			{
				argTypes[i] = "long[]";
			} else if(argTypes[i].equals("wchar_t"))
			{
				argTypes[i] = "char";
			} else if(argTypes[i].equals("wchar_t[]"))
			{
				argTypes[i] = "char[]";
			} else if(argTypes[i].equals("gnu.java.lang.String"))
			{
				argTypes[i] = "java.lang.String";
			}
		}

		mainLoop:
		for(Method m : jc.getMethods())
		{
			if(!m.getName().equals(methodName)) continue;

			Type[] ta = m.getArgumentTypes();
			if(ta.length != argTypes.length) continue;

			for(int i=0; i<argTypes.length; i++)
			{
				boolean equalType = argTypes[i].toString().equals(ta[i].toString());
				if(!equalType) continue mainLoop;
			}

			methodSet.add(m);
			return;
		}

		// method not found; additionally try replacing char[]/char with byte[]/byte.
		// the linker sometimes mixes these up
		mainLoop:
		for(Method m : jc.getMethods())
		{
			if(!m.getName().equals(methodName)) continue;

			Type[] ta = m.getArgumentTypes();
			if(ta.length != argTypes.length) continue;

			for(int i=0; i<argTypes.length; i++)
			{
				boolean equalType = argTypes[i].toString().equals(ta[i].toString());
				boolean charSearching = argTypes[i].toString().equals("char");
				boolean charArraySearching = argTypes[i].toString().equals("char[]");
				boolean byteProvided = ta[i].toString().equals("byte");
				boolean byteArrayProvided = ta[i].toString().equals("byte[]");

				if(!equalType && !(charSearching && byteProvided) &&
						!(charArraySearching && byteArrayProvided)) continue mainLoop;
			}

			//System.err.println("Matched " + methodName + "(" + join(argTypes, ", ") +
			//			") to " + methodName + "(" + join(m.getArgumentTypes(), ", ") + ")");

			methodSet.add(m);
			return;
		}

		// method definitely not found!
		StringBuffer sb = new StringBuffer();
		sb.append("Method \"" + methodName + "(");
		for(int i=0; i<argTypes.length; i++)
		{
			if(i > 0) sb.append(", ");
			sb.append(argTypes[i]);
		}
		sb.append(")\" not found in class \"" + getClassName() + "\". Candidates are:\n");

		for(Method m : jc.getMethods())
		{
			sb.append("   - " + m.getName() + "(");
			Type[] ta = m.getArgumentTypes();
			for(int i=0; i<ta.length; i++)
			{
				if(i > 0) sb.append(", ");
				sb.append(ta[i].toString());
			}
			sb.append(")\n");
		}
	
		throw new Exception(sb.toString());
	}
	
	/**
	 * Returns all missing methods.
	 * 
	 * @return All missing methods.
	 */
	public Set<Method> getMissingMethods()
	{
		return methodSet;
	}
	
	/**
	 * Adds a missing field with the given name.
	 * 
	 * @param fieldName The name of the field.
	 * @throws Exception Thrown if the field doesn't exist in the real class.
	 */
	public void addMissingField(String fieldName) throws Exception
	{
		for(Field f : jc.getFields())
		{
			if(!f.getName().equals(fieldName)) continue;
			fieldSet.add(f);
			return;
		}

		// field not found. try without "_". There seems to be a problem sometimes.
		String fieldNameNoUnderscore = fieldName.replaceAll("_", "");
		for(Field f : jc.getFields())
		{
			if(!f.getName().replaceAll("_", "").equals(fieldNameNoUnderscore)) continue;
			//System.err.println("Matched Field \"" + fieldName + "\" to \"" + f.getName() + "\"");
			fieldSet.add(f);
			return;
		}
		
		// field definitely not found!
		StringBuffer sb = new StringBuffer();
		sb.append("Field \"" + fieldName + "\" not found in class \"" + getClassName() + "\". Candidates are:\n");
		for(Field f : jc.getFields()) sb.append("   - " + f.getName() + "\n");
		throw new Exception(sb.toString());
	}
	
	/**
	 * Returns all missing fields.
	 * 
	 * @return All missing fields.
	 */
	public Set<Field> getMissingFields()
	{
		return fieldSet;
	}


	// --------------- private methods ---------------
	
	/*private String join(Object[] array, String connector)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<array.length; i++)
		{
			if(i > 0) sb.append(connector);
			sb.append(array[i]);
		}
		return sb.toString();
	}*/
}
