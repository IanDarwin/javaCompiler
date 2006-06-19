/*
 *   JavaCompiler - A java to native compiler for Windows and Linux.
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

package ch.mtSystems.javaCompiler.view;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.control.IAppControllerListener;
import ch.mtSystems.javaCompiler.model.JavaCompilerProject;
import ch.mtSystems.javaCompiler.model.utilities.FileUtilities;
import ch.mtSystems.javaCompiler.model.utilities.GuiSettingsMemory;
import ch.mtSystems.javaCompiler.view.dialogs.SettingsDialog;
import ch.mtSystems.javaCompiler.view.utilities.LayoutUtilities;


public class JavaCompilerGui implements SelectionListener, IAppControllerListener
{
	public final static String VERSION = "0.7";

	private static Shell shell;
	private SashForm sash;
	private static Composite contentComposite;
	private static Button bPrevious, bNext, bHelp;
	private Text tHelp;

	private ToolItem tiSettings, tiSave;
	private Menu popupMenu;
	private MenuItem miSave, miSaveAs;
	private Label lFileName;


	public JavaCompilerGui()
	{
		shell = new Shell(new Display());
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
		AppController.getAppController().loadPage(GuiSettingsMemory.getSettingsMemory().skipIntro() ?
				AppController.PAGE_CREATE_PROJECT : AppController.PAGE_INTRODUCTION);
	}


	// --------------- public static methods ---------------

	public static void setTitle(String title)
	{
		shell.setText(title);
	}

	public static Composite getContentComposite() { return contentComposite; }
	public static Button getPreviousButton() { return bPrevious; }
	public static Button getNextButton() { return bNext; }


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == tiSettings)
		{
			(new SettingsDialog()).open();
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
		} else if(e.getSource() == bHelp)
		{
			if(tHelp == null)
			{
				tHelp = new Text(sash, SWT.MULTI|SWT.BORDER|SWT.READ_ONLY|SWT.WRAP|SWT.V_SCROLL);
				tHelp.setLayoutData(new GridData(GridData.FILL_BOTH));
				tHelp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
				bHelp.setText("hide help");
				pageLoaded(AppController.getAppController().getCurrentPage());

				shell.setSize(500, 650);
				sash.setWeights(new int[] { 466, 147 });
			} else
			{
				tHelp.dispose();
				tHelp = null;
				bHelp.setText("show help");
				shell.setSize(500, 500);
			}

			sash.layout();
		}
	}


	public void widgetDefaultSelected(SelectionEvent e) { }


	// ---------------IAppControllerListener ---------------

	public void projectChanged(JavaCompilerProject project)
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

	public void pageLoaded(int page)
	{
		if(tHelp == null) return;

		if(page == AppController.PAGE_INTRODUCTION)
		{
			tHelp.setText(FileUtilities.readTextFile(new File("ressources/helpIntroPage.txt")));
		} else if(page == AppController.PAGE_CREATE_PROJECT)
		{
			tHelp.setText(FileUtilities.readTextFile(new File("ressources/helpCreateProjectPage.txt")));
		} else if(page == AppController.PAGE_SOURCE)
		{
			tHelp.setText(FileUtilities.readTextFile(new File("ressources/helpSourcePage.txt")));
		} else if(page == AppController.PAGE_SETTINGS)
		{
			tHelp.setText(FileUtilities.readTextFile(new File("ressources/helpSettingsPage.txt")));
		} else if(page == AppController.PAGE_COMPILATION)
		{
			tHelp.setText(FileUtilities.readTextFile(new File("ressources/helpCompilePage.txt")));
		}
	}


	// --------------- private methods ---------------


	private void createButtonComposite(Composite parent)
	{
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(LayoutUtilities.createGridLayout(5, 0));
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ToolBar toolBar = new ToolBar(buttonComposite, SWT.FLAT);

		tiSettings = new ToolItem(toolBar, SWT.NONE);
		tiSettings.setImage(new Image(Display.getCurrent(), "ressources/settings.png"));
		tiSettings.setToolTipText("settings");
		tiSettings.addSelectionListener(this);

		new ToolItem(toolBar, SWT.SEPARATOR);

		tiSave = new ToolItem(toolBar, SWT.DROP_DOWN);
		tiSave.setImage(new Image(Display.getCurrent(), "ressources/save.png"));
		tiSave.setToolTipText("save");
		tiSave.addSelectionListener(this);
		tiSave.setEnabled(false);

		popupMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
		miSave = new MenuItem(popupMenu, SWT.PUSH);
		miSave.setText("save");
		miSave.addSelectionListener(this);
		miSaveAs = new MenuItem(popupMenu, SWT.PUSH);
		miSaveAs.setText("save as...");
		miSaveAs.addSelectionListener(this);

		lFileName = new Label(buttonComposite, SWT.NONE);

		GridData gdSettings = new GridData();
		gdSettings.grabExcessHorizontalSpace = true;
		lFileName.setLayoutData(gdSettings);

		bPrevious = new Button(buttonComposite, SWT.NONE);
		bPrevious.setText("< previous");

		bNext = new Button(buttonComposite, SWT.NONE);
		bNext.setText("next >");

		bHelp = new Button(buttonComposite, SWT.NONE);
		bHelp.setText("show help");
		bHelp.addSelectionListener(this);
	}

	private void saveProject()
	{
		JavaCompilerProject project = AppController.getAppController().getCurrentProject();
		File f = project.getSaveFile();

		if(f == null) saveProjectAs();
		else          fileSave(f);
	}

	private void saveProjectAs()
	{
		FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		fileDialog.setText("projekt speichern");
		fileDialog.setFileName("default.jcp");
		if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
		fileDialog.setFilterExtensions(new String[] { "*.jcp" });
		fileDialog.setFilterNames(new String[] { "JavaCompilerProject (*.jcp)" });

		String ret = fileDialog.open();
		if(ret == null) return;

		if(!ret.toLowerCase().endsWith(".jcp")) ret += ".jcp";
		File f = new File(ret);
		AppController.curDir = f.getParentFile();

		if(f.exists())
		{
			String title = "replace file";
			String msg = "The file " + f.getName() + " exists already. Replace it?";

			MessageBox mb = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_QUESTION|SWT.YES|SWT.NO);
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

			MessageBox mb = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR|SWT.OK);
			mb.setText(title);
			mb.setMessage(msg);
			mb.open();
		}
	}

	// --------------- our mighty main paw ---------------

	public static void main(String[] args)
	{
		new JavaCompilerGui();
		shell.setSize(500, 500);
		shell.setImage(new Image(shell.getDisplay(), "ressources/icon.ico"));
		shell.setFocus(); // prevent autoselection of radiobuttons
		shell.open();

		while(!shell.isDisposed())
		{
			if(!shell.getDisplay().readAndDispatch()) shell.getDisplay().sleep();
		}
	}
}
