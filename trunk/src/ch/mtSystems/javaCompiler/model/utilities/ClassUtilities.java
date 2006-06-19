package ch.mtSystems.javaCompiler.model.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;


public class ClassUtilities
{
	public static final String CMD_CLASS = "ressources\\gcc-4.1.1-win\\bin\\jcf-dump.exe";

	private static final Pattern pPackage = Pattern.compile("package\\s+([^;]+);");
	private static final Pattern pAwt = Pattern.compile("java\\s*.\\s*awt\\s*.");
	private static final Pattern pSwing = Pattern.compile("javax\\s*.\\s*swing\\s*.");


	/**
	 * For Text.java in package ch.foo.bar, returns:<br>
	 * ch.foo.bar.Text
	 */
	public static String getFromSource(File f) throws IOException
	{
		byte[] ba = FileUtilities.readFile(f);
		String s = new String(ba);

		String fileName = f.getName();
		String simpleClassName = fileName.substring(0, fileName.lastIndexOf('.'));

		Matcher m = pPackage.matcher(s);
		if(m.find())
		{
			return m.group(1).replaceAll("\\s", "") + "." + simpleClassName;
		} else
		{
			return simpleClassName;
		}
	}

	public static void sourceAwtSwingToSwingWT(File f) throws IOException
	{
		byte[] ba = FileUtilities.readFile(f);
		String s = new String(ba);

		Matcher mAwt = pAwt.matcher(s);
		s = mAwt.replaceAll("swingwt.awt.");

		Matcher mSwing = pSwing.matcher(s);
		s = mSwing.replaceAll("swingwtx.swing.");

		FileWriter fw = new FileWriter(f);
		fw.write(s);
		fw.flush();
		fw.close();
	}

	/**
	 * For Text.class in package ch.foo.bar, returns:<br>
	 * ch.foo.bar.Text
	 */
	public static String getFromClass(File fClass) throws Exception
	{
		Process p = Runtime.getRuntime().exec(new String[] { CMD_CLASS, "--javap", fClass.toString() });
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;
		while((line = br.readLine()) != null)
		{
			if(line.startsWith("This class: ")) break;
		}
		br.close();
		if(p.waitFor() != 0) throw new Exception("Reading the class-name failed!");

		return line.substring(12, line.indexOf(", super:"));
	}

	public static void classAwtSwingToSwingWT(File f) throws IOException
	{
		JavaClass javaClass = (new ClassParser(f.toString())).parse();
		ConstantPool constantPool = javaClass.getConstantPool();

		for(int i=0; i<constantPool.getLength(); i++)
		{
			Constant generalConstant = constantPool.getConstant(i);
			if(!(generalConstant instanceof ConstantUtf8)) continue;

			ConstantUtf8 c = (ConstantUtf8)generalConstant;
			String s = c.getBytes();

			if(s.indexOf("java/awt/") > -1)
			{
				c.setBytes(s.replace("java/awt/", "swingwt/awt/"));
			} else if(s.indexOf("javax/swing/") > -1)
			{
				c.setBytes(s.replace("javax/swing/", "swingwtx/swing/"));
			}
		}

		javaClass.dump(f);
	}
}
