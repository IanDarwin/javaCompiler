package ch.mtSystems.javaCompiler.view.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.model.ICompilationProgressLogger;
import ch.mtSystems.javaCompiler.model.JavaCompiler;
import ch.mtSystems.javaCompiler.model.exceptions.NoJavaException;
import ch.mtSystems.javaCompiler.view.JavaCompilerGui;
import ch.mtSystems.javaCompiler.view.dialogs.SettingsDialog;


public class CompilationPage implements SelectionListener, DisposeListener, ICompilationProgressLogger
{
	private Text tLog;
	private Button bCompile;


	public CompilationPage()
	{
		Label lTitle = new Label(JavaCompilerGui.getContentComposite(), SWT.NONE);
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getCurrent(), fd));
		lTitle.setText("Step 3 of 3: compilation");

		tLog = new Text(JavaCompilerGui.getContentComposite(), SWT.BORDER|SWT.READ_ONLY|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL);
		tLog.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		tLog.setLayoutData(new GridData(GridData.FILL_BOTH));

		bCompile = new Button(JavaCompilerGui.getContentComposite(), SWT.NONE);
		bCompile.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		bCompile.setText("compile");
		bCompile.addSelectionListener(this);

		// keep a little space at the bottom
		new Composite(JavaCompilerGui.getContentComposite(), SWT.NONE).setLayoutData(new GridData(0, 40));

		// page settings
		JavaCompilerGui.getNextButton().setVisible(false);
		JavaCompilerGui.getPreviousButton().addSelectionListener(this);
		JavaCompilerGui.setTitle("JavaCompiler v" + JavaCompilerGui.VERSION + " - 3/3: compilation");

		lTitle.addDisposeListener(this);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == JavaCompilerGui.getPreviousButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_SETTINGS);
		} else if(e.getSource() == bCompile)
		{
			compile(Display.getCurrent());
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) { }


	// --------------- DisposeListener ---------------

	public void widgetDisposed(DisposeEvent e)
	{
		JavaCompilerGui.getNextButton().removeSelectionListener(this);
		JavaCompilerGui.getPreviousButton().removeSelectionListener(this);
	}


	// --------------- CompilationProgressLogger ---------------

	public void log(final String s, final boolean intent)
	{
		tLog.getDisplay().syncExec(new Runnable()
				{
					public void run()
					{
						if(tLog.getCharCount() > 0) tLog.append("\n");
						if(intent) tLog.append("\t");
						tLog.append(s);
					}
				});
	}

	// --------------- private methods ---------------

	private void compile(final Display d)
	{
		final JavaCompiler jc = new JavaCompiler(this, AppController.getAppController().getCurrentProject());

		tLog.setText("");
		JavaCompilerGui.getPreviousButton().setEnabled(false);
		bCompile.setEnabled(false);

		new Thread()
		{
			public void run()
			{
				try
				{
					if(jc.compile()) syncLog("\n\ndone", d);
					else             syncLog("\n\nfailed...", d);
				} catch(Exception ex)
				{
					ex.printStackTrace();
					syncLog("\n\nfailed:\n", d);
					syncLog(ex.toString(), d);

					if(ex instanceof NoJavaException) showNoJavaExceptionInfo(d);
				}

				d.syncExec(new Runnable()
						{
							public void run()
							{
								bCompile.setEnabled(true);
								JavaCompilerGui.getPreviousButton().setEnabled(true);
							}
						});
			}
		}.start();
	}

	private void syncLog(final String s, Display d)
	{
		d.syncExec(new Runnable()
				{
					public void run() { tLog.append(s); }
				});
	}

	private void showNoJavaExceptionInfo(final Display d)
	{
		d.syncExec(new Runnable()
				{
					public void run()
					{
						String title = "JDK 1.5 javac not yet set";
						String msg = "Java 1.5 preprocessing from source needs a JDK 1.5 javac.\n" +
							"Would you like to configure the location of javac now?";

						MessageBox messageBox = new MessageBox(d.getActiveShell(), SWT.ICON_QUESTION|SWT.YES|SWT.NO);
						messageBox.setText(title);
						messageBox.setMessage(msg);

						if(messageBox.open() == SWT.YES) (new SettingsDialog()).open();
					}
				});
	}
}
