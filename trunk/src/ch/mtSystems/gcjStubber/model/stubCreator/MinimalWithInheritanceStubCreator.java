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

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import ch.mtSystems.gcjStubber.model.MissingClass;


public class MinimalWithInheritanceStubCreator extends StubCreator
{
	public MinimalWithInheritanceStubCreator(MissingClass[] missingClasses,
			File jar, File object, File cmdGcj, File tmpDir)
	{
		super(missingClasses, jar, object, cmdGcj, tmpDir);
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
			// always add the default constructor (otherwise it's not available if there's another)
			fileWriter.write("  public ");
			fileWriter.write(missingClass.getSimpleClassName());
			fileWriter.write("() { }\n");
			
			for(Method m : missingClass.getMissingMethods())
			{
				if(!m.getName().equals("<init>")) continue;

				// note: bug in bcel, constructors have a "void" return type.
				fileWriter.write("  ");
				fileWriter.write(methodToString(m).replace("void <init>", missingClass.getSimpleClassName()));
				fileWriter.write("\n");
			}

			fileWriter.write("\n");
		} 

		// methods
		for(Method m : missingClass.getMissingMethods())
		{
			if(m.getName().equals("<init>")) continue;

			fileWriter.write("  ");
			fileWriter.write(methodToString(m));
			fileWriter.write("\n");
		}

		// finish the class
		fileWriter.write("}\n");
	}
}
