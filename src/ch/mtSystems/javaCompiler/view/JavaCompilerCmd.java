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
