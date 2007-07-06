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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UnresolvedReferenceParser
{
	private static final boolean DEBUG = false;
	private static final Pattern UNDEFINED_REFERENCE_PATTERN = Pattern.compile("undefined reference to `(.*?)'");
	private static final Pattern METHOD_PATTERN = Pattern.compile("((?:.*? )+)(.*?)\\((.*?)\\)"); // return type could be "long long", so "(.*?) (.*?)\\((.*?)\\)" is not sufficent. 
	private static final Pattern CONSTRUCTOR_PATTERN = Pattern.compile("(\\S*?)\\((.*?)\\)");
	private static final Pattern FIELDNAME_PATTERN = Pattern.compile("\\S+");

	
	private String[] errorMsg;
	private Set<String> excludedClasses;
	private File libgcjDotJar;
	private Map<String, MissingClass> missingClasses = new HashMap<String, MissingClass>();


	public UnresolvedReferenceParser(String[] errorMsg, Set<String> excludedClasses, File libgcjDotJar)
	{
		this.errorMsg = errorMsg;
		this.excludedClasses = excludedClasses;
		this.libgcjDotJar = libgcjDotJar;
	}


	// --------------- public methods ---------------

	public MissingClass[] parse() throws Exception
	{
		Set<String> duplicateSet = new HashSet<String>();
		for(String line: errorMsg)
		{
			//System.out.println(line);
			
			Matcher m1 = UNDEFINED_REFERENCE_PATTERN.matcher(line);
			while(m1.find())
			{
				if(DEBUG) System.out.println("handling [" + m1.group(1) + "]");
				String reference = m1.group(1).
						replaceAll("::", ".").
						replaceAll("\\*", "").
						replaceAll("JArray<(.*?)>", "$1[]");
				if(DEBUG) System.out.println("  -> [" + reference + "]");
				
				if(duplicateSet.add(reference)) handleUndefinedReference(reference);
			}
		}

		if(DEBUG) System.out.println();
		return missingClasses.values().toArray(new MissingClass[0]);
	}


	// --------------- private methods ---------------
	
	private void handleUndefinedReference(String reference) throws Exception
	{
		if(reference.endsWith(".class$"))
		{
			if(DEBUG) System.out.println("  Recognized as missing class");
			getMissingClass(reference.substring(0, reference.length()-7));
		} else
		{
			Matcher m = METHOD_PATTERN.matcher(reference);
			if(m.find())
			{
				if(DEBUG) System.out.println("  Recognized as missing method");

				int lastPoint = m.group(2).lastIndexOf('.');
				String longClassName = m.group(2).substring(0, lastPoint);
				String methodName = m.group(2).substring(lastPoint+1);
				String[] argTypes = (m.group(3).length() > 0) ? m.group(3).split(", ") : new String[0];
				getMissingClass(longClassName).addMissingMethod(methodName, argTypes);
			} else
			{
				m = CONSTRUCTOR_PATTERN.matcher(reference);
				if(m.find())
				{
					if(DEBUG) System.out.println("  Recognized as missing constructor");

					int lastPoint = m.group(1).lastIndexOf('.');
					String longClassName = m.group(1).substring(0, lastPoint);
					String className = m.group(1).substring(lastPoint+1);

					if(longClassName.endsWith(className))
					{
						if(m.group(2).length() == 0)
						{
							if(DEBUG) System.out.println("  -> Default constructor...");
						} else
						{
							String[] argTypes = m.group(2).split(", ");
							getMissingClass(longClassName).addMissingConstructor(argTypes);
						}
					} else
					{
						System.err.println("  #################### NOT RECOGNIZED 1 ####################");
						System.err.println("  ## " + reference);
					}
				} else
				{
					// should be a field...
					int lastPoint = reference.lastIndexOf('.');
					String longClassName = reference.substring(0, lastPoint);
					String fieldName = reference.substring(lastPoint+1);

					if(excludedClasses.contains(longClassName) && FIELDNAME_PATTERN.matcher(fieldName).matches())
					{
						getMissingClass(longClassName).addMissingField(fieldName);
					} else
					{
						System.err.println("  #################### NOT RECOGNIZED 2 ####################");
						System.err.println("  ## " + reference);
					}
				}
			}
		}
	}

	private MissingClass getMissingClass(String className) throws Exception
	{
		MissingClass mc = missingClasses.get(className);
		if(mc == null)
		{
			mc = new MissingClass(className, libgcjDotJar);
			missingClasses.put(className, mc);
		}
		return mc;
	}
}
