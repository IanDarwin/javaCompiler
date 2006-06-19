/*
 *   JavaCompiler - A java to native compiler for Windows and Linux.
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

package ch.mtSystems.javaCompiler.view;

import java.io.File;

import ch.mtSystems.javaCompiler.model.ICompilationProgressLogger;
import ch.mtSystems.javaCompiler.model.JavaCompiler;
import ch.mtSystems.javaCompiler.model.JavaCompilerProject;


public class JavaCompilerCmd
{
	public static void main(String[] args) throws Exception
	{
		File f;
		if(args.length != 1 || args[0] == null || !(f = new File(args[0])).exists() || !f.getName().endsWith(".jcp"))
		{
			System.out.println("Usage: JavaCompilerCmd jcfProjectFile");
			return;
		}

		JavaCompilerProject project = JavaCompilerProject.open(f);
		JavaCompiler jc = new JavaCompiler(new ICompilationProgressLogger()
				{
					public void log(String s, boolean indent)
					{
						if(indent) System.out.print("\t");
						System.out.println(s);
					}
				}, project);

		if(jc.compile()) System.out.println("\n\ndone");
		else             System.out.println("\n\nfailed...");
	}
}
