/*
 *   GcjStubber - A stub creator for GCJ (JNC).
 *   Copyright (C) 2007  Marco Trudel <mtrudel@gmx.ch>
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

package ch.mtSystems.gcjStubber.view;

import java.io.File;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import ch.mtSystems.gcjStubber.model.StubsGenerator;
import ch.mtSystems.gcjStubber.model.StubsGeneratorListener;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class GcjStubber implements SelectionListener, StubsGeneratorListener
{
	public static final String VERSION = "0.1";

	private static Shell shell;
	
	private Button bOpenGcjDir, bOpenStubDir;
	private Text tGcjDir, tStubDir, tLog;
	private Button bStart, bStop;
	private ProgressBar progressBar;

	private Text tGcjArguments;


	public GcjStubber(String[] args)
	{
		shell = new Shell(Display.getDefault());
		shell.setText("GcjStubber v" + VERSION);
		shell.setLayout(LayoutUtilities.createGridLayout(1, 0));
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabFolder tabFolder = new TabFolder(shell, SWT.TOP); 
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		createMainTab(tabFolder);
		createAdvancedTab(tabFolder);
		new PhaseStatisticsTab(tabFolder);
		new ObjectStatisticsTab(tabFolder);
		
		// start/stop
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		GridLayout buttonCompositeLayout = LayoutUtilities.createGridLayout(2, 3);
		buttonCompositeLayout.horizontalSpacing = 10;
		buttonComposite.setLayout(buttonCompositeLayout);
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		
		bStart = new Button(buttonComposite, SWT.NONE);
		bStart.setText("Start");
		bStart.addSelectionListener(this);
		bStart.setEnabled(false);
		
		bStop = new Button(buttonComposite, SWT.NONE);
		bStop.setText("Stop");
		bStop.addSelectionListener(this);
		bStop.setEnabled(false);
		
		// check if user provided paths and settings on the command line
		for(int i=0; i<args.length; i++)
		{
			if(i == 0)
			{
				tGcjDir.setText(args[i]);
			} else if(i == 1)
			{
				tStubDir.setText(args[i]);
			} else
			{
				if(i > 2) tGcjArguments.append("\n");
				tGcjArguments.append(args[i]);
			}
		}
		if(tGcjDir.getText().length() > 0 && tStubDir.getText().length() > 0) bStart.setEnabled(true);
		StubsGenerator.getStubsGenerator().addListener(this);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent se)
	{
		if(se.getSource() == bOpenGcjDir || se.getSource() == bOpenStubDir)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(shell);
			String ret = dirDialog.open();
			if(ret == null || ret.length() == 0) return;

			if(se.getSource() == bOpenGcjDir)
			{
				tGcjDir.setText(ret);
			} else
			{
				tStubDir.setText(ret);
			}

			if(tGcjDir.getText().length() > 0 && tStubDir.getText().length() > 0) bStart.setEnabled(true);
		} else if(se.getSource() == bStart)
		{
			final File gcjDir = new File(tGcjDir.getText());
			final File stubsDir = new File(tStubDir.getText());
			final String[] args = (tGcjArguments.getText().length() == 0) ?
					new String[0] :
					tGcjArguments.getText().split("\r\n");

			new Thread()
			{
				public void run()
				{
					StubsGenerator.getStubsGenerator().createStubs(gcjDir, stubsDir, args);
				}
			}.start();
		} else if(se.getSource() == bStop)
		{
			StubsGenerator.getStubsGenerator().stopCreatingStubs();
			bStop.setEnabled(false);
		}
	}

	public void widgetDefaultSelected(SelectionEvent se) { }


	// --------------- StubsGeneratorListener ---------------

	public void started()
	{
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				bOpenGcjDir.setEnabled(false);
				bOpenStubDir.setEnabled(false);
				bStart.setEnabled(false);
				bStop.setEnabled(true);
				tLog.setText("");
				progressBar.setSelection(0);
			}
		});
	}

	public void actionDone(final String msg)
	{
		Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						tLog.append(msg);				
					}
				});
	}

	public void processed(String objectName, int phaseProcessed, int phaseResult,
			String phaseResultMsg, int savings, final int objectIndex, final int totalCount)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				progressBar.setSelection(progressBar.getMaximum() * objectIndex / totalCount);				
			}
		});
	}

	public void done()
	{
		Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{
						bOpenGcjDir.setEnabled(true);
						bOpenStubDir.setEnabled(true);
						bStart.setEnabled(true);
						bStop.setEnabled(false);	
						
						Display.getDefault().beep();
					}
				});
	}


	// --------------- private methods ---------------

	private Image loadImage(String name)
	{
		try
		{
			InputStream stream = GcjStubber.class.getResourceAsStream(name);
			Image img = new Image(Display.getDefault(), stream);
			stream.close();
			return img;
		} catch(Exception ex)
		{
			System.err.println("--- loadImage(" + name + ") ---");
			ex.printStackTrace();
			return null;
		}
	}
	
	private void createMainTab(TabFolder tabFolder)
	{
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Main");

		Composite parentComposite = new Composite(tabFolder, SWT.NONE);
		parentComposite.setLayout(new GridLayout());
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabItem.setControl(parentComposite);

		// user input (GCJ dir and stub dir)
		Image imgOpen = loadImage("open.png");
		Composite inputComposite = new Composite(parentComposite, SWT.NONE);
		inputComposite.setLayout(LayoutUtilities.createGridLayout(3, 3));
		inputComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		(new Label(inputComposite, SWT.NONE)).setText("GCJ Directory:");
		tGcjDir = new Text(inputComposite, SWT.BORDER|SWT.READ_ONLY);
		tGcjDir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tGcjDir.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		bOpenGcjDir = new Button(inputComposite, SWT.NONE);
		bOpenGcjDir.setImage(imgOpen);
		bOpenGcjDir.addSelectionListener(this);

		(new Label(inputComposite, SWT.NONE)).setText("Output (Stubs) Directory:");
		tStubDir = new Text(inputComposite, SWT.BORDER|SWT.READ_ONLY);
		tStubDir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tStubDir.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		bOpenStubDir = new Button(inputComposite, SWT.NONE);
		bOpenStubDir.setImage(imgOpen);
		bOpenStubDir.addSelectionListener(this);
		
		(new Label(parentComposite, SWT.SEPARATOR|SWT.HORIZONTAL)).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// progress
		Composite progressComposite = new Composite(parentComposite, SWT.NONE);
		progressComposite.setLayout(LayoutUtilities.createGridLayout(2, 3));
		progressComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		(new Label(progressComposite, SWT.NONE)).setText("Progress:");
		progressBar = new ProgressBar(progressComposite, SWT.HORIZONTAL|SWT.SMOOTH);
		progressBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		progressBar.setMaximum(1000);
		progressBar.setSelection(0);
		
		// log
		tLog = new Text(parentComposite, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL|SWT.READ_ONLY);
		tLog.setLayoutData(new GridData(GridData.FILL_BOTH));
		tLog.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
	}
	
	private void createAdvancedTab(TabFolder tabFolder)
	{
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Advanced");

		Composite parentComposite = new Composite(tabFolder, SWT.NONE);
		parentComposite.setLayout(new GridLayout());
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabItem.setControl(parentComposite);
		
		(new Label(parentComposite, SWT.NONE)).setText("Custom GCJ arguments (one each line):");
		
		tGcjArguments = new Text(parentComposite, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL);
		GridData gcjArgumentsGridData = new GridData(GridData.FILL_HORIZONTAL);
		gcjArgumentsGridData.heightHint = 120;
		tGcjArguments.setLayoutData(gcjArgumentsGridData);
	}


	// --------------- our mighty main paw ---------------

	public static void main(String[] args) throws Exception
	{	
		new GcjStubber(args);
		shell.setSize(800, 600);
		shell.open();

		while(!shell.isDisposed())
		{
			if(!shell.getDisplay().readAndDispatch()) shell.getDisplay().sleep();
		}
	}
}
