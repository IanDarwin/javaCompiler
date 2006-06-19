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

	public static String readTextFile(File f)
	{
		try
		{
			byte[] ba = readFile(f);
			return new String(ba);
		} catch(IOException ioex)
		{
			return ioex.getMessage();
		}
	}

	public static File createTempDir(String prefix, String suffix) throws IOException
	{
		File f = File.createTempFile(prefix, suffix);
		if(!f.delete()) throw new IOException("(1) Unable to create temporary directory!");
		if(!f.mkdirs()) throw new IOException("(2) Unable to create temporary directory!");
		return f;
	}
}
