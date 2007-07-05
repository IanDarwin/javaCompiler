/*
 *   JavaNativeCompiler - A Java to native compiler.
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

package ch.mtSystems.jnc.view;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ch.mtSystems.jnc.control.AppController;
import ch.mtSystems.jnc.control.IAppControllerListener;
import ch.mtSystems.jnc.model.ICompilationProgressLogger;
import ch.mtSystems.jnc.model.NativeCompiler;
import ch.mtSystems.jnc.model.JNCProject;
import ch.mtSystems.jnc.model.utilities.SettingsMemory;
import ch.mtSystems.jnc.view.dialogs.SettingsDialog;
import ch.mtSystems.jnc.view.pages.CreateProjectPage;
import ch.mtSystems.jnc.view.pages.WizzardPage;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class JNC implements SelectionListener, IAppControllerListener
{
	public final static String VERSION = "1.1.1";

	private static Shell shell;
	private SashForm sash;
	private static Composite contentComposite;
	private static Button bPrevious, bNext, bManual;

	private ToolItem tiSettings, tiSave;
	private Menu popupMenu;
	private MenuItem miSave, miSaveAs;
	private Label lFileName;


	public JNC(File projectFile)
	{
		shell = new Shell(Display.getDefault());
		shell.setText("JNC (JavaNativeCompiler) v" + VERSION);
		shell.setLayout(new GridLayout());

		sash = new SashForm(shell, SWT.VERTICAL);
		sash.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cSashNorth = new Composite(sash, SWT.NONE);
		cSashNorth.setLayout(LayoutUtilities.createGridLayout(1, 0));
		cSashNorth.setLayoutData(new GridData(GridData.FILL_BOTH));

		contentComposite = new Composite(cSashNorth, SWT.NO_RADIO_GROUP);
		contentComposite.setLayout(LayoutUtilities.createGridLayout(1, 0));
		contentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		new Label(cSashNorth, SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createButtonComposite(cSashNorth);

		AppController.getAppController().addAppControllerListener(this);
		boolean projectPage = (SettingsMemory.getSettingsMemory().getSkipIntro() || projectFile != null);
		WizzardPage page = AppController.getAppController().loadPage(projectPage ?
				AppController.PAGE_CREATE_PROJECT : AppController.PAGE_INTRODUCTION);
		if(projectFile != null) ((CreateProjectPage)page).setProjectFile(projectFile);
	}


	// --------------- public static methods ---------------
	
	public static Image loadImage(String s)
	{
		try
		{
			InputStream stream = JNC.class.getResourceAsStream(s);
			Image img = new Image(Display.getDefault(), stream);
			stream.close();
			return img;
		} catch(Exception ex)
		{
			System.err.println("--- loadImage(" + s + ") ---");
			ex.printStackTrace();
			return null;
		}
	}
	
	public static String loadText(String s)
	{
		try
		{
			StringBuffer sb = new StringBuffer();
			InputStream stream = JNC.class.getResourceAsStream(s);
			byte[] ba = new byte[1024];
			
			while(true)
			{
				int len = stream.read(ba);
				if(len < 0) break;
				sb.append(new String(ba, 0, len));
			}
			
			stream.close();
			return sb.toString();
		} catch(Exception ex)
		{
			System.err.println("--- loadText(" + s + ") ---");
			ex.printStackTrace();
			return "";
		}
	}

	public static Composite getContentComposite() { return contentComposite; }
	public static Button getPreviousButton() { return bPrevious; }
	public static Button getNextButton() { return bNext; }


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == tiSettings)
		{
			(new SettingsDialog(shell)).open();
		} else if(e.getSource() == tiSave)
		{
			if(e.detail == SWT.ARROW)
			{
				Rectangle rect = tiSave.getBounds();
				Point pt = new Point(rect.x, rect.y + rect.height);
				pt = tiSave.getParent().toDisplay(pt);

				popupMenu.setLocation(pt.x, pt.y);
				popupMenu.setVisible(true);
			} else
			{
				saveProject();
			}
		} else if(e.getSource() == miSave)
		{
			saveProject();
		} else if(e.getSource() == miSaveAs)
		{
			saveProjectAs();
		} else if(e.getSource() == bManual)
		{
			try
			{
				Program.launch((new File("manual/index.html")).toURI().toURL().toString());
			} catch(Exception ex)
			{
				MessageBox mb = new MessageBox(shell, SWT.NONE);
				mb.setText("Error");
				mb.setMessage("Opening the manual failed with this error:\n" + ex.toString());
				mb.open();
			}
		}
	}


	public void widgetDefaultSelected(SelectionEvent e) { }


	// ---------------IAppControllerListener ---------------

	public void projectChanged(JNCProject project)
	{
		File f = project.getSaveFile();

		tiSave.setEnabled(true);

		lFileName.setText((f == null) ? "not yet saved" : f.getName());
		lFileName.getParent().layout();
	}

	public void projectUpdated()
	{
		File f = AppController.getAppController().getCurrentProject().getSaveFile();
		if(f == null || f.getName().charAt(0) == '*') return;

		lFileName.setText("*" + f.getName());
		lFileName.getParent().layout();
	}

	public void projectSaved()
	{
		File f = AppController.getAppController().getCurrentProject().getSaveFile();
		if(f == null) return;

		lFileName.setText(f.getName());
		lFileName.getParent().layout();
	}


	// --------------- private methods ---------------


	private void createButtonComposite(Composite parent)
	{
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(LayoutUtilities.createGridLayout(5, 0));
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ToolBar toolBar = new ToolBar(buttonComposite, SWT.FLAT);

		tiSettings = new ToolItem(toolBar, SWT.NONE);
		tiSettings.setImage(loadImage("settings.png"));
		tiSettings.setToolTipText("Settings");
		tiSettings.addSelectionListener(this);

		new ToolItem(toolBar, SWT.SEPARATOR);

		tiSave = new ToolItem(toolBar, SWT.DROP_DOWN);
		tiSave.setImage(loadImage("save.png"));
		tiSave.setToolTipText("Save");
		tiSave.addSelectionListener(this);
		tiSave.setEnabled(false);

		popupMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
		miSave = new MenuItem(popupMenu, SWT.PUSH);
		miSave.setText("Save");
		miSave.addSelectionListener(this);
		miSaveAs = new MenuItem(popupMenu, SWT.PUSH);
		miSaveAs.setText("Save As...");
		miSaveAs.addSelectionListener(this);

		lFileName = new Label(buttonComposite, SWT.NONE);

		GridData gdSettings = new GridData();
		gdSettings.grabExcessHorizontalSpace = true;
		lFileName.setLayoutData(gdSettings);

		bPrevious = new Button(buttonComposite, SWT.NONE);
		bPrevious.setText("< Previous");

		bNext = new Button(buttonComposite, SWT.NONE);
		bNext.setText("Next >");

		bManual = new Button(buttonComposite, SWT.NONE);
		bManual.setText("Open Manual");
		bManual.addSelectionListener(this);
	}

	private void saveProject()
	{
		JNCProject project = AppController.getAppController().getCurrentProject();
		File f = project.getSaveFile();

		if(f == null) saveProjectAs();
		else          fileSave(f);
	}

	private void saveProjectAs()
	{
		FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		fileDialog.setText("Save Project");
		fileDialog.setFileName("default.jnc");
		if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
		fileDialog.setFilterExtensions(new String[] { "*.jnc" });
		fileDialog.setFilterNames(new String[] { "JNC Project (*.jnc)" });

		String ret = fileDialog.open();
		if(ret == null) return;

		if(!ret.toLowerCase().endsWith(".jnc")) ret += ".jnc";
		File f = new File(ret);
		AppController.curDir = f.getParentFile();

		if(f.exists())
		{
			String title = "replace file";
			String msg = "The file " + f.getName() + " exists already. Replace it?";

			MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION|SWT.YES|SWT.NO);
			mb.setText(title);
			mb.setMessage(msg);
			if(mb.open() != SWT.YES) return;
		}

		fileSave(f);
	}

	private void fileSave(File f)
	{
		try
		{
			AppController.getAppController().getCurrentProject().save(f);
		} catch(IOException ioex)
		{
			String title = "error on save";
			String msg = "An error occured while trying to save:\n" + ioex.getMessage();

			MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR|SWT.OK);
			mb.setText(title);
			mb.setMessage(msg);
			mb.open();
		}
	}

	// --------------- our mighty main paw ---------------

	public static void main(String[] args) throws Exception
	{
		checkLicense();
		File projectFile = null;

		if(args.length > 0)
		{
			for(int i=0; i<args.length; i++)
			{
				if(args[i].equals("--help"))
				{
					showHelp();
					return;
				}
			}

			if(args.length == 2)
			{
				if(!args[0].equals("-compile"))
				{
					showHelp();
					return;
				}
			}

			projectFile = new File((args.length == 2) ? args[1] : args[0]);
			if(!projectFile.exists() || projectFile.isDirectory())
			{
				System.out.println("\"" + projectFile + "\" does not exist!");
				System.out.println();
				showHelp();
				return;
			}

			if(args.length == 2)
			{
				JNCProject project = JNCProject.open(projectFile);
				NativeCompiler jc = new NativeCompiler(new ICompilationProgressLogger()
					{
						public void log(String s, boolean indent)
						{
							if(indent) System.out.print("\t");
							System.out.println(s);
						}
					}, project);

				if(jc.compile()) System.out.println("\n\ndone");
				else             System.out.println("\n\nfailed...");

				return;
			}
		}
		
		new JNC(projectFile);
		shell.setSize(500, 500);
		shell.setImage(loadImage("icon.ico"));
		shell.setFocus(); // prevent autoselection of radiobuttons
		shell.open();
		shell.forceActive(); // needed if a trial msg was shown

		while(!shell.isDisposed())
		{
			if(!shell.getDisplay().readAndDispatch()) shell.getDisplay().sleep();
		}
	}
	
	private static void checkLicense()
	{
		if(SettingsMemory.getSettingsMemory().getLicense() == null)
		{
			Shell shell = new Shell(Display.getDefault());

			MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
			mb.setText("Demo");
			mb.setMessage("This is a demo version of JNC.\n" +
					"All applications that are compiled for windows will display a message similar to this.\n" +
					"Please visit http://jnc.mtSystems.ch to get a license...");
			mb.open();

			shell.dispose();
			Display.getDefault().update();
		}
	}
	
	private static void showHelp()
	{
		System.out.println("JNC usage: JNC [-compile] [file.jnc]");
		System.out.println("-compile: Directly compile the given JNC project file.");
		System.out.println("file.jnc: The file to open or compile.");
	}
}
