package ch.mtSystems.javaCompiler.model.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


public class GuiSettingsMemory
{
	private static final File SETTINGS_FILE = new File(System.getProperty("user.home"), "JavaCompilerGuiSettings.txt");

	private boolean skipIntro = false;


	public boolean skipIntro() { return skipIntro; }

	public void setSkipIntro(boolean skip)
	{
		skipIntro = skip;
		save();
	}


	// --------------- private methods ---------------

	private void save()
	{
		try
		{
			FileWriter fw = new FileWriter(SETTINGS_FILE);
			fw.write("skipIntro=" + (skipIntro ? "true" : "false"));
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

				if(sa[0].equals("skipIntro")) skipIntro = sa[1].equals("true");
			}

			br.close();
		} catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}


	// --------------- singleton pattern ---------------

	/**
	 * Returns the GuiSettingsMemory instance.
	 */
	public static GuiSettingsMemory getSettingsMemory()
	{
		if(settingsMemory == null)
		{
			settingsMemory = new GuiSettingsMemory();
			settingsMemory.load();
		}

		return settingsMemory;
	}

	private GuiSettingsMemory() { }
	private static GuiSettingsMemory settingsMemory;
}
