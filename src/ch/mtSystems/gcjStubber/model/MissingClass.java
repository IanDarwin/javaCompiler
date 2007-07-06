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
			} else if(argTypes[i].equals("long long"))
			{
				argTypes[i] = "long";
			} else if(argTypes[i].equals("wchar_t[]"))
			{
				argTypes[i] = "char[]";
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
				if(!argTypes[i].toString().equals(ta[i].toString())) continue mainLoop;
			}

			methodSet.add(m);
			return;
		}

		// method not found!
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
		//TODO: why are these wrong in the linker output???
		if(fieldName.equals("END_OF_SEQU_ENCE")) fieldName = "END_OF_SEQUENCE";
		
		for(Field f : jc.getFields())
		{
			if(!f.getName().equals(fieldName)) continue;
			fieldSet.add(f);
			return;
		}

		// field not found!
		StringBuffer sb = new StringBuffer();
		sb.append("Field \"" + fieldName + "\" not found in class \"" + getClassName() + "\". Candidates are:\n");
		for(Field f : jc.getFields()) sb.append("   - " + f.getName() + "\n");
		throw new Exception(sb.toString());
	}
	
	public Set<Field> getMissingFields()
	{
		return fieldSet;
	}
}
