package ch.mtSystems.javaCompiler.model.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;


public class ClassUtilities
{
	private static final Pattern pPackage = Pattern.compile("package\\s+([^;]+);");
	private static final Pattern pAwt = Pattern.compile("java\\s*.\\s*awt\\s*.");
	private static final Pattern pSwing = Pattern.compile("javax\\s*.\\s*swing\\s*.");


	/**
	 * For Test.java or Test.class in package ch.foo.bar, returns:<br>
	 * ch.foo.bar.Text
	 */
	public static String getClassName(File f) throws IOException
	{
		String fileName = f.getName();
		String simpleClassName = fileName.substring(0, fileName.lastIndexOf('.'));
		String thePackage = getPackage(f);

		return (thePackage.length() == 0) ? simpleClassName : thePackage + "." + simpleClassName;
	}

	/**
	 * For Test.java or Test.class in package ch.foo.bar, returns:<br>
	 * ch.foo.bar
	 */
	public static String getPackage(File f) throws IOException
	{
		if(f.getName().endsWith(".java")) return getPackageFromSource(f);
		if(f.getName().endsWith(".class")) return getPackageFromClass(f);

		throw new IOException("Filetype " + f.getName() + " not supported!");
	}

	/**
	 * replaces:<br>
	 * - "javax.swing" with "swingwtx.swing"
	 * - "java.awt" with "swingwt.awt"
	 */
	public static void convertToSwingWT(File f) throws IOException
	{
		if(f.getName().endsWith(".java")) { convertSourceToSwingWT(f); return; }
		if(f.getName().endsWith(".class")) { convertClassToSwingWT(f); return; }

		throw new IOException("Filetype " + f.getName() + " not supported!");
	}

	private static String getPackageFromSource(File f) throws IOException
	{
		String s = new String(FileUtilities.readFile(f));
		Matcher m = pPackage.matcher(s);
		return (m.find()) ? m.group(1).replaceAll("\\s", "") : "";
	}

	private static String getPackageFromClass(File f) throws IOException
	{
		JavaClass javaClass = (new ClassParser(f.toString())).parse();
		return javaClass.getPackageName();
	}

	private static void convertSourceToSwingWT(File f) throws IOException
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

	private static void convertClassToSwingWT(File f) throws IOException
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
