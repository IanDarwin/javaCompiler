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
	private JavaClass jc;
	private Set<Field> fieldSet = new HashSet<Field>();
	private Set<Method> methodSet = new HashSet<Method>(); // constructors and methods


	public MissingClass(String className, File libgcjDotJar) throws Exception
	{
		String fileName = className.replaceAll("\\.", "/") + ".class";
		jc = (new ClassParser(libgcjDotJar.toString(), fileName)).parse();
	}

	
	public String getClassName()
	{
		return jc.getClassName();
	}

	public String getSimpleClassName()
	{
		String className = jc.getClassName();
		return className.substring(className.lastIndexOf('.')+1);
	}
	
	public void addConstructor(String[] argTypes) throws Exception
	{
		addMethod("<init>", argTypes);
	}
	
	public void addMethod(String methodName, String[] argTypes) throws Exception
	{
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

		throw new Exception("Method \"" + methodName + "\" not found in class \"" + getSimpleClassName() + "\"!");
	}
	
	public void addField(String fieldName) throws Exception
	{
		for(Field f : jc.getFields())
		{
			if(!f.getName().equals(fieldName)) continue;
			fieldSet.add(f);
			return;
		}

		throw new Exception("Field \"" + fieldName + "\" not found in class \"" + getSimpleClassName() + "\"!");
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		// package
		sb.append("package ");
		sb.append(jc.getPackageName());
		sb.append(";\n\n");
		
		// class declaration
		if(jc.isPublic()) sb.append("public ");
		if(jc.isProtected()) sb.append("protected ");
		if(jc.isPrivate()) sb.append("private ");
		if(jc.isAbstract()) sb.append("abstract ");
		if(jc.isFinal()) sb.append("final ");
		if(jc.isClass()) sb.append("class ");
		if(jc.isInterface()) sb.append("interface ");
		sb.append(getSimpleClassName());
		sb.append("\n");

		// start the class
		sb.append("{\n");

		// fields
		for(Field f : fieldSet)
		{
			sb.append("  ");
			sb.append(f);
			sb.append(" = ");
			sb.append(createDummyValue(f.getType()));
			sb.append(";\n");
		}
		if(fieldSet.size() > 0) sb.append("\n");
		
		// constructors
		if(jc.isClass())
		{
			sb.append("  public ");
			sb.append(getSimpleClassName());
			sb.append("()\n");
			sb.append("  { }\n");

			for(Method m : methodSet)
			{
				if(!m.getName().equals("<init>")) continue;

				sb.append("  ");

				// note: bug in bcel, constructors have a "void" return type.
				sb.append(methodToString(m).replace("void <init>", getSimpleClassName()));

				sb.append("  { }\n");
			}

			sb.append("\n");
		} 

		// methods
		for(Method m : methodSet)
		{
			if(m.getName().equals("<init>")) continue;

			sb.append("  ");
			sb.append(methodToString(m));
			sb.append(" {");
			String dummyReturn = createDummyValue(m.getReturnType());
			if(dummyReturn != null)
			{
				sb.append(" return ");
				sb.append(dummyReturn);
				sb.append(";");
			}
			sb.append(" }\n");
		}

		// finish the class
		sb.append("}\n");

		return sb.toString();
	}


	// --------------- private methods ---------------

	private String createDummyValue(Type t)
	{
		if(t.equals(Type.VOID))    return null;
		if(t.equals(Type.BOOLEAN)) return "true";
		if(t.equals(Type.BYTE))    return "(byte)23";
		if(t.equals(Type.CHAR))    return "'2'";
		if(t.equals(Type.DOUBLE))  return "23";
		if(t.equals(Type.FLOAT))   return "23";
		if(t.equals(Type.INT))     return "23";
		if(t.equals(Type.LONG))    return "23";
		if(t.equals(Type.SHORT))   return "(short)23";
		return "(" + t.toString().replaceAll("\\$", ".") + ")null";
	}
	
	private String methodToString(Method m)
	{
		return m.toString().
			replaceAll("\\)\\s*?throws.*", ")"). // cut exceptions
			replaceAll("transient\\s+(.*?\\()", "$1"). // cut "transient"
			replaceAll("native\\s+(.*?\\()", "$1"); // cut "native"
	}
}
