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

package ch.mtSystems.javaCompiler.model.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileUtilities
{
	public static File deleteDirRecursively(File dir)
	{
		File[] dirContent = dir.listFiles();

		for(int i=0; i<dirContent.length; i++)
		{
				 if(dirContent[i].isDirectory()) deleteDirRecursively(dirContent[i]);
			else if(!dirContent[i].delete())     return dirContent[i];
		}

		if(!dir.delete()) return dir;
		return null;
	}

	public static void copyFile(File src, File dest) throws IOException
	{
		byte[] bytes = readFile(src);

		FileOutputStream fos = new FileOutputStream(dest);
		fos.write(bytes);
		fos.close();
	}

	public static byte[] readFile(File src) throws IOException
	{
		byte[] ba = new byte[(int)src.length()];

		FileInputStream fis = new FileInputStream(src);
		fis.read(ba);
		fis.close();

		return ba;
	}

	public static File createTempDir(String prefix, String suffix) throws IOException
	{
		File f = File.createTempFile(prefix, suffix);
		if(!f.delete()) throw new IOException("(1) Unable to create temporary directory!");
		if(!f.mkdirs()) throw new IOException("(2) Unable to create temporary directory!");
		return f;
	}
}
