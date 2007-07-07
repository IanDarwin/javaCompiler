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


public class MissingClass
{
	private String simpleClassName;
	private JavaClass jc;
	private Set<Field> fieldSet = new HashSet<Field>();
	private Set<Method> methodSet = new HashSet<Method>(); // constructors and methods


	public MissingClass(String className, File libgcjDotJar) throws Exception
	{
		String fileName = className.replaceAll("\\.", "/") + ".class";
		//System.err.println("loading " + fileName);
		jc = (new ClassParser(libgcjDotJar.toString(), fileName)).parse();

		simpleClassName = jc.getClassName();
		simpleClassName = simpleClassName.substring(simpleClassName.lastIndexOf('.')+1);
	}


	// --------------- public methods ---------------

	public JavaClass getJavaClass()
	{
		return jc;
	}
	
	public String getClassName()
	{
		return jc.getClassName();
	}
	
	public String getSimpleClassName()
	{
		return simpleClassName;
	}
	
	public void addMissingConstructor(String[] argTypes) throws Exception
	{
		addMissingMethod("<init>", argTypes);
	}
	
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

			System.err.println("Matched " + methodName + "(" + join(argTypes, ", ") +
						") to " + methodName + "(" + join(m.getArgumentTypes(), ", ") + ")");

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
	
	public Set<Method> getMissingMethods()
	{
		return methodSet;
	}
	
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
			System.err.println("Matched Field \"" + fieldName + "\" to \"" + f.getName() + "\"");
			fieldSet.add(f);
			return;
		}
		
		// field definitely not found!
		StringBuffer sb = new StringBuffer();
		sb.append("Field \"" + fieldName + "\" not found in class \"" + getClassName() + "\". Candidates are:\n");
		for(Field f : jc.getFields()) sb.append("   - " + f.getName() + "\n");
		throw new Exception(sb.toString());
	}
	
	public Set<Field> getMissingFields()
	{
		return fieldSet;
	}


	// --------------- private methods ---------------
	
	private String join(Object[] array, String connector)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<array.length; i++)
		{
			if(i > 0) sb.append(connector);
			sb.append(array[i]);
		}
		return sb.toString();
	}
}
