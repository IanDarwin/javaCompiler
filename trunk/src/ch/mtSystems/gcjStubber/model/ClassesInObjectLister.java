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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClassesInObjectLister
{
	private final static Pattern pattern = Pattern.compile("^.* (.+)\\.class\\$$");


	private File cmdNm;
	private Exception ex = null;
	private Set<String> classesInObject = new LinkedHashSet<String>();
	private List<String> errorList = new LinkedList<String>();


	public ClassesInObjectLister(File cmdNm)
	{
		this.cmdNm = cmdNm;
	}


	// --------------- public methods ---------------

	public Set<String> getClassesInObject(File fObject) throws Exception
	{
		classesInObject.clear();

		Process p = Runtime.getRuntime().exec(new String[] {
				cmdNm.toString(),
				"--defined-only",
				"--demangle=java",
				fObject.toString()
			});

		Thread t1 = check(p.getInputStream(), true);
		Thread t2 = check(p.getErrorStream(), false);
		p.waitFor();
		t1.join();
		t2.join();

		if(ex != null) throw ex;
		return classesInObject;
	}


	// --------------- private methods ---------------

	private Thread check(final InputStream inputStream, final boolean parse)
	{
		Thread t = new Thread()
		{
			public void run()
			{
				try
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
					for(String s = br.readLine(); s != null; s = br.readLine())
					{
						if(parse)
						{
							Matcher m = pattern.matcher(s);
							if(m.find()) classesInObject.add(m.group(1));
						} else
						{
							errorList.add(s);
						}
					}
					br.close();
				} catch(Exception ex)
				{
					ClassesInObjectLister.this.ex = ex;
				}
			}
		};

		t.start();
		return t;
	}
}
