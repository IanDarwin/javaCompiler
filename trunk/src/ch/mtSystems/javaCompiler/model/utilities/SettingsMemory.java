package ch.mtSystems.javaCompiler.model.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


public class SettingsMemory
{
	private static final File SETTINGS_FILE = new File(System.getProperty("user.home"), "JavaCompilerSettings.txt");

	private String javac;


	public String getJavac() { return javac; }

	public void setJavac(String javac)
	{
		this.javac = javac;
		save();
	}


	// --------------- private methods ---------------

	private void save()
	{
		try
		{
			FileWriter fw = new FileWriter(SETTINGS_FILE);
			fw.write("javaHome=" + javac);
			fw.flush();
			fw.close();
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void load()
	{
		if(!SETTINGS_FILE.exists()) return;

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(SETTINGS_FILE));
			String line;

			while((line = br.readLine()) != null)
			{
				String[] sa = line.split("=", 2);
				if(sa.length != 2) continue;

				if(sa[0].equals("javaHome")) javac = sa[1];
			}

			br.close();
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}


	// --------------- singleton pattern ---------------

	/**
	 * Returns the SettingsMemory instance.
	 */
	public static SettingsMemory getSettingsMemory()
	{
		if(settingsMemory == null)
		{
			settingsMemory = new SettingsMemory();
			settingsMemory.load();
		}

		return settingsMemory;
	}

	private SettingsMemory() { }
	private static SettingsMemory settingsMemory;
}
