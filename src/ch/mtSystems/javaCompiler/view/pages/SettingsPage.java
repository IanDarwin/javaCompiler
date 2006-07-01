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

package ch.mtSystems.javaCompiler.view.pages;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.model.JavaCompilerProject;
import ch.mtSystems.javaCompiler.view.JavaCompilerGui;
import ch.mtSystems.javaCompiler.view.dialogs.MainClassDialog;
import ch.mtSystems.javaCompiler.view.utilities.LayoutUtilities;


public class SettingsPage implements ModifyListener, SelectionListener, DisposeListener
{
	private static Image imgOpen = new Image(Display.getCurrent(), "ressources/open.png");


	private Text tMainClass, tOutputDir, tOutputName, tIcon;
	private Button bOpenMainClass, bJava5Preprocessing, bUseJni;
	private Button bOpenOutputDir, bOmitWindows, bOmitLinux, bOmitMac, bOmitStripping, bOmitPacking;
	private Button bIcon, bOpenIcon, bHideConsole;

	private boolean ignoreEvents = false;


	public SettingsPage()
	{
		Label lTitle = new Label(JavaCompilerGui.getContentComposite(), SWT.NONE);
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getCurrent(), fd));
		lTitle.setText("Step 2 of 3: settings");


		// java settings
		Group groupJavaSettings = new Group(JavaCompilerGui.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupJavaSettings.setLayout(new GridLayout());
		groupJavaSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupJavaSettings.setText("java settings");

		Composite mainClassComposite = new Composite(groupJavaSettings, SWT.NONE);
		mainClassComposite.setLayout(LayoutUtilities.createGridLayout(3, 0));
		mainClassComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(mainClassComposite, SWT.NONE).setText("main class: ");

		tMainClass = new Text(mainClassComposite, SWT.BORDER|SWT.READ_ONLY);
		tMainClass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tMainClass.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		bOpenMainClass = new Button(mainClassComposite, SWT.NONE);
		bOpenMainClass.setImage(imgOpen);
		bOpenMainClass.addSelectionListener(this);

		bJava5Preprocessing = new Button(groupJavaSettings, SWT.CHECK);
		bJava5Preprocessing.setText("enable Java 1.5 preprocessing");
		bJava5Preprocessing.addSelectionListener(this);

		bUseJni = new Button(groupJavaSettings, SWT.CHECK);
		bUseJni.setText("use JNI (CNI is default)");
		bUseJni.addSelectionListener(this);


		// common output settings
		Group groupCommonOutputSettings = new Group(JavaCompilerGui.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupCommonOutputSettings.setLayout(LayoutUtilities.createGridLayout(2, 5, 20));
		groupCommonOutputSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupCommonOutputSettings.setText("common output settings");

		Composite outputDirComposite = new Composite(groupCommonOutputSettings, SWT.NONE);
		outputDirComposite.setLayout(LayoutUtilities.createGridLayout(3, 0));
		outputDirComposite.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_HORIZONTAL, 2, 1, 0, 0));

		new Label(outputDirComposite, SWT.NONE).setText("directory: ");

		tOutputDir = new Text(outputDirComposite, SWT.BORDER|SWT.READ_ONLY);
		tOutputDir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tOutputDir.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		bOpenOutputDir = new Button(outputDirComposite, SWT.NONE);
		bOpenOutputDir.setImage(imgOpen);
		bOpenOutputDir.addSelectionListener(this);

		new Label(outputDirComposite, SWT.NONE).setText("name: ");

		tOutputName = new Text(outputDirComposite, SWT.BORDER);
		tOutputName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tOutputName.addModifyListener(this);

		bOmitWindows = new Button(groupCommonOutputSettings, SWT.CHECK);
		bOmitWindows.setText("don't create windows binary");
		bOmitWindows.addSelectionListener(this);

		bOmitStripping = new Button(groupCommonOutputSettings, SWT.CHECK);
		bOmitStripping.setText("omit stripping");
		bOmitStripping.addSelectionListener(this);

		bOmitLinux = new Button(groupCommonOutputSettings, SWT.CHECK);
		bOmitLinux.setText("don't create linux binary");
		bOmitLinux.addSelectionListener(this);

		bOmitPacking = new Button(groupCommonOutputSettings, SWT.CHECK);
		bOmitPacking.setText("omit packing");
		bOmitPacking.addSelectionListener(this);

		bOmitMac = new Button(groupCommonOutputSettings, SWT.CHECK);
		bOmitMac.setText("don't create mac binary");
		bOmitMac.setEnabled(false);


		// windows output settings
		Group groupWindowsOutputSettings = new Group(JavaCompilerGui.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupWindowsOutputSettings.setLayout(LayoutUtilities.createGridLayout(2, 5, 20));
		groupWindowsOutputSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupWindowsOutputSettings.setText("windows output settings");

		Composite iconComposite = new Composite(groupWindowsOutputSettings, SWT.NONE);
		iconComposite.setLayout(LayoutUtilities.createGridLayout(3, 0));
		iconComposite.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_HORIZONTAL, 2, 1));

		bIcon = new Button(iconComposite, SWT.CHECK);
		bIcon.setText("icon: ");
		bIcon.addSelectionListener(this);

		tIcon = new Text(iconComposite, SWT.BORDER|SWT.READ_ONLY);
		tIcon.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		bOpenIcon = new Button(iconComposite, SWT.NONE);
		bOpenIcon.setImage(imgOpen);
		bOpenIcon.addSelectionListener(this);

		bHideConsole = new Button(groupWindowsOutputSettings, SWT.CHECK);
		bHideConsole.setText("hide console");
		bHideConsole.addSelectionListener(this);


		// page settings
		JavaCompilerGui.getNextButton().setVisible(true);
		updateData(); // will handle the next button
		JavaCompilerGui.getNextButton().addSelectionListener(this);
		JavaCompilerGui.getPreviousButton().addSelectionListener(this);
		JavaCompilerGui.setTitle("JavaCompiler v" + JavaCompilerGui.VERSION + " - 2/3: settings");

		lTitle.addDisposeListener(this);
	}

	// --------------- ModifyListener ---------------

	public void modifyText(ModifyEvent e)
	{
		if(ignoreEvents) return;

		String text = ((Text)e.getSource()).getText();
		if(text.trim().length() == 0) text = null;

		AppController.getAppController().getCurrentProject().setOutputName(text);
		updateNextButton();
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == JavaCompilerGui.getNextButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_COMPILATION);
		} else if(e.getSource() == JavaCompilerGui.getPreviousButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_SOURCE);
		} else if(e.getSource() == bOpenMainClass)
		{
			MainClassDialog mainClassDialog = new MainClassDialog();
			Object[] oa = (Object[])mainClassDialog.open();
			if(oa == null) return;

			File outputDirSuggestion = (File)oa[0];
			File mainClassRessource = (File)oa[1];
			String mainClass = (String)oa[2];

			tMainClass.setText(mainClass);
			AppController.getAppController().getCurrentProject().setMainClass(mainClassRessource, mainClass);
			updateNextButton();

			if(tOutputDir.getText().equals("") && outputDirSuggestion != null)
			{
				tOutputDir.setText(outputDirSuggestion.toString());
				AppController.getAppController().getCurrentProject().setOutputDir(outputDirSuggestion);
			}
			if(tOutputName.getText().equals(""))
			{
				tOutputName.setText(mainClass.substring(mainClass.lastIndexOf('.')+1));
			}
		} else if(e.getSource() == bOpenOutputDir)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(Display.getCurrent().getActiveShell());
			if(AppController.curDir != null) dirDialog.setFilterPath(AppController.curDir.toString());
			dirDialog.setText("select output directory");
			String ret = dirDialog.open();
			if(ret == null) return;

			File f = new File(ret);
			AppController.curDir = f;

			tOutputDir.setText(f.toString());
			AppController.getAppController().getCurrentProject().setOutputDir(f);
			updateNextButton();
		} else if(e.getSource() == bJava5Preprocessing)
		{
			AppController.getAppController().getCurrentProject().
					setJava5Preprocessing(bJava5Preprocessing.getSelection());
		} else if(e.getSource() == bUseJni)
		{
			AppController.getAppController().getCurrentProject().
					setUseJni(bUseJni.getSelection());
		} else if(e.getSource() == bOmitWindows)
		{
			AppController.getAppController().getCurrentProject().
					setOmitWindows(bOmitWindows.getSelection());
			updateWindowsSettings();
			updateNextButton();
		} else if(e.getSource() == bOmitLinux)
		{
			AppController.getAppController().getCurrentProject().
					setOmitLinux(bOmitLinux.getSelection());
			updateNextButton();
		} else if(e.getSource() == bOmitStripping)
		{
			AppController.getAppController().getCurrentProject().
					setOmitStripping(bOmitStripping.getSelection());
		} else if(e.getSource() == bOmitPacking)
		{
			AppController.getAppController().getCurrentProject().
					setOmitPacking(bOmitPacking.getSelection());
		} else if(e.getSource() == bIcon)
		{
			AppController.getAppController().getCurrentProject().
					setUseIcon(bIcon.getSelection());
			updateWindowsSettings();
		} else if(e.getSource() == bOpenIcon)
		{
			FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
			if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
			fileDialog.setText("select an icon for the exe file");
			fileDialog.setFilterExtensions(new String[] { "*.ico" });
			String ret = fileDialog.open();
			if(ret == null) return;

			File f = new File(ret);
			AppController.curDir = f.getParentFile();

			tIcon.setText(f.toString());
			AppController.getAppController().getCurrentProject().setIconFile(f);
		} else if(e.getSource() == bHideConsole)
		{
			AppController.getAppController().getCurrentProject().
					setHideConsole(bHideConsole.getSelection());
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) { }


	// --------------- DisposeListener ---------------

	public void widgetDisposed(DisposeEvent e)
	{
		JavaCompilerGui.getNextButton().removeSelectionListener(this);
		JavaCompilerGui.getPreviousButton().removeSelectionListener(this);
	}


	// --------------- private methods ---------------

	private void updateData()
	{
		JavaCompilerProject project = AppController.getAppController().getCurrentProject();
		ignoreEvents = true;

		if(project.getMainClass() != null) tMainClass.setText(project.getMainClass());
		if(project.getOutputDir() != null) tOutputDir.setText(project.getOutputDir().toString());
		if(project.getOutputName() != null) tOutputName.setText(project.getOutputName());
		if(project.getIconFile() != null) tIcon.setText(project.getIconFile().toString());

		bJava5Preprocessing.setSelection(project.getJava5Preprocessing());
		bUseJni.setSelection(project.getUseJni());

		bOmitWindows.setSelection(project.getOmitWindows());
		bOmitLinux.setSelection(project.getOmitLinux());
		bOmitStripping.setSelection(project.getOmitStripping());
		bIcon.setSelection(project.getUseIcon());
		bOmitPacking.setSelection(project.getOmitPacking());
		bHideConsole.setSelection(project.getHideConsole());

		updateWindowsSettings();
		updateNextButton();

		ignoreEvents = false;
	}

	private void updateWindowsSettings()
	{
		JavaCompilerProject project = AppController.getAppController().getCurrentProject();

		bIcon.setEnabled(!project.getOmitWindows());
		bHideConsole.setEnabled(!project.getOmitWindows());

		tIcon.setEnabled(!project.getOmitWindows() && project.getUseIcon());
		tIcon.setBackground((!project.getOmitWindows() && project.getUseIcon()) ?
				Display.getCurrent().getSystemColor(SWT.COLOR_WHITE) :
				bHideConsole.getBackground());
		bOpenIcon.setEnabled(!project.getOmitWindows() && project.getUseIcon());
	}

	private void updateNextButton()
	{
		JavaCompilerProject project = AppController.getAppController().getCurrentProject();
		JavaCompilerGui.getNextButton().setEnabled(project.getMainClass() != null &&
				project.getOutputDir() != null && project.getOutputName() != null &&
				(!project.getOmitWindows() || !project.getOmitLinux()));
	}
}