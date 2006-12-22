/*
 *   JavaNativeCompiler - A Java to native compiler.
 *   Copyright (C) 2006  Marco Trudel <mtrudel@gmx.ch>
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

package ch.mtSystems.jnc.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;


public class AutoCompiler
{
	public static void main(String[] args) throws Exception
	{
		String cmd = "./JavaNativeCompiler";
		if(!(new File(cmd)).exists() && !(new File(cmd+".exe")).exists())
		{
			System.err.println("JavaNativeCompiler not found in current path!");
			return;
		}

		List<String> lCmd = new LinkedList<String>();
		lCmd.add(cmd);
		
		if(args.length != 1)
		{
			System.err.println("Usage: AutoCompiler project.jnc");
			return;
		}

		if(!args[0].equals("-debug"))
		{
			File projectFile = new File(args[0]);
			if(projectFile.exists() && !projectFile.isDirectory())
			{
				lCmd.add("-compile");
				lCmd.add(projectFile.toString());
			} else
			{
				System.err.println("Usage: AutoCompiler project.jnc");
				return;
			}
		}

		Process p = Runtime.getRuntime().exec(lCmd.toArray(new String[0]));
		log(p.getInputStream(), false);
		log(p.getErrorStream(), true);
	}
	
	private static void log(final InputStream inputStream, final boolean isErrorStream)
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
					for(String s = br.readLine(); s != null; s = br.readLine())
					{
						if(isErrorStream) System.err.println(s);
						else              System.out.println(s);
					}
				} catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}.start();
	}
}
