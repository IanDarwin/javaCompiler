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
import java.util.LinkedList;
import java.util.List;


public class CommandExecutor
{
	private String[] cmd;
	private File workingDir;
	
	private Exception ex;
	private List<String> outputList = new LinkedList<String>();
	private List<String> errorList = new LinkedList<String>();


	public CommandExecutor(String[] cmd, File workingDir)
	{
		this.cmd = cmd;
		this.workingDir = workingDir;
	}


	public CommandExecutor(String cmd, File workingDir)
	{
		this(new String[] { cmd }, workingDir);
	}


	public void execute() throws Exception
	{
		Process p = Runtime.getRuntime().exec(cmd, null, workingDir);
		Thread t2 = log(p.getInputStream(), outputList);
		Thread t1 = log(p.getErrorStream(), errorList);
		p.waitFor();
		t1.join();
		t2.join();

		if(ex != null) throw ex;
	}

	public String[] getOutput()
	{
		return outputList.toArray(new String[0]);
	}

	public String[] getError()
	{
		return errorList.toArray(new String[0]);
	}

	private Thread log(final InputStream inputStream, final List<String> saveList) throws Exception
	{
		Thread t = new Thread()
		{
			public void run()
			{
				try
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
					for(String s = br.readLine(); s != null; s = br.readLine()) saveList.add(s);
					br.close();
				} catch(Exception ex)
				{
					CommandExecutor.this.ex = ex;
				}
			}
		};

		t.start();
		return t;
	}
}
