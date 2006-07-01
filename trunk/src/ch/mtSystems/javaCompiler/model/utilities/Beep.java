package ch.mtSystems.javaCompiler.model.utilities;


public class Beep
{
	static
	{
		System.loadLibrary("beep");
	}

	public static synchronized void beep(int frequency, int duration)
	{
		_beep( frequency , duration);

		try { Thread.sleep(duration); }
		catch(InterruptedException ie) { }

		_beep(0 , 0);
	}

	private Beep() { }
	private static native void _beep(int frequency, int duration);
}