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

package ch.mtSystems.jnc.view.pages;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.mtSystems.jnc.control.AppController;
import ch.mtSystems.jnc.model.JNCProject;
import ch.mtSystems.jnc.view.JNC;
import ch.mtSystems.jnc.view.dialogs.MainClassDialog;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class BasicSettingsPage extends WizzardPage implements ModifyListener, SelectionListener, DisposeListener
{
	private static Image imgOpen = JNC.loadImage("open.png");


	private Text tMainClass, tJavaLibPath, tWindows, tIcon, tLinux, tMac;
	private Button bOpenMainClass, bUseCni;
	private Button bWindows, bOpenWindows, bIcon, bOpenIcon, bHideConsole;
	private Button bLinux, bOpenLinux;
	private Button bMac, bOpenMac;
	private Button bOmitStripping, bOmitPacking, bDisableOptimisation;

	private boolean ignoreEvents = false;


	public BasicSettingsPage()
	{
		Label lTitle = new Label(JNC.getContentComposite(), SWT.NONE);
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getDefault(), fd));
		lTitle.setText("Step 2 of 4: Basic settings (required)");


		// java settings
		Group groupJavaSettings = new Group(JNC.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupJavaSettings.setLayout(LayoutUtilities.createGridLayout(3, 3));
		groupJavaSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupJavaSettings.setText("Java settings");

		Label lMainClass = new Label(groupJavaSettings, SWT.NONE);
		lMainClass.setText("Main class: ");
		
		tMainClass = new Text(groupJavaSettings, SWT.BORDER|SWT.READ_ONLY);
		tMainClass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tMainClass.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		bOpenMainClass = new Button(groupJavaSettings, SWT.NONE);
		bOpenMainClass.setImage(imgOpen);
		bOpenMainClass.addSelectionListener(this);

		(new Label(groupJavaSettings, SWT.NONE)).setText("java.library.path: ");

		tJavaLibPath = new Text(groupJavaSettings, SWT.BORDER);
		tJavaLibPath.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_HORIZONTAL, 2, 1));
		tJavaLibPath.addModifyListener(this);

		Composite tmpComposite1 = new Composite(groupJavaSettings, SWT.NONE);
		tmpComposite1.setLayout(LayoutUtilities.createGridLayout(1, 0, 20));
		tmpComposite1.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_HORIZONTAL, 3, 0, 10, -1));

		bUseCni = new Button(tmpComposite1, SWT.CHECK);
		bUseCni.setText("Use CNI instead of JNI");
		bUseCni.addSelectionListener(this);


		// common output settings
		Group groupExecutableSettings = new Group(JNC.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupExecutableSettings.setLayout(LayoutUtilities.createGridLayout(3, 3));
		groupExecutableSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupExecutableSettings.setText("Executable settings");

		bWindows = new Button(groupExecutableSettings, SWT.CHECK);
		bWindows.setText("Windows:");
		bWindows.addSelectionListener(this);

		tWindows = new Text(groupExecutableSettings, SWT.BORDER);
		tWindows.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tWindows.addModifyListener(this);

		bOpenWindows = new Button(groupExecutableSettings, SWT.NONE);
		bOpenWindows.setImage(imgOpen);
		bOpenWindows.addSelectionListener(this);

		Composite iconComposite = new Composite(groupExecutableSettings, SWT.NONE);
		iconComposite.setLayout(LayoutUtilities.createGridLayout(3, 0));
		iconComposite.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_HORIZONTAL, 3, 1, -1, 40));

		bIcon = new Button(iconComposite, SWT.CHECK);
		bIcon.setText("Icon: ");
		bIcon.addSelectionListener(this);

		tIcon = new Text(iconComposite, SWT.BORDER|SWT.READ_ONLY);
		tIcon.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		bOpenIcon = new Button(iconComposite, SWT.NONE);
		bOpenIcon.setImage(imgOpen);
		bOpenIcon.addSelectionListener(this);

		bHideConsole = new Button(groupExecutableSettings, SWT.CHECK);
		bHideConsole.setText("Hide console");
		bHideConsole.setLayoutData(LayoutUtilities.createGridData(-1, 3, 1, -1, 40));
		bHideConsole.addSelectionListener(this);
		
		bLinux = new Button(groupExecutableSettings, SWT.CHECK);
		bLinux.setText("Linux:");
		bLinux.addSelectionListener(this);

		tLinux = new Text(groupExecutableSettings, SWT.BORDER);
		tLinux.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tLinux.addModifyListener(this);

		bOpenLinux = new Button(groupExecutableSettings, SWT.NONE);
		bOpenLinux.setImage(imgOpen);
		bOpenLinux.addSelectionListener(this);

		bMac = new Button(groupExecutableSettings, SWT.CHECK);
		bMac.setText("Mac:");
		bMac.addSelectionListener(this);
		bMac.setEnabled(false);

		tMac = new Text(groupExecutableSettings, SWT.BORDER|SWT.READ_ONLY);
		tMac.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tMac.setEnabled(false);

		bOpenMac = new Button(groupExecutableSettings, SWT.NONE);
		bOpenMac.setImage(imgOpen);
		bOpenMac.addSelectionListener(this);
		bOpenMac.setEnabled(false);

		Composite tmpComposite2 = new Composite(groupExecutableSettings, SWT.NONE);
		tmpComposite2.setLayout(LayoutUtilities.createGridLayout(3, 0, 20));
		tmpComposite2.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_HORIZONTAL, 3, 0, 10, -1));
		
		bOmitStripping = new Button(tmpComposite2, SWT.CHECK);
		bOmitStripping.setText("Omit stripping");
		bOmitStripping.addSelectionListener(this);
		
		bOmitPacking = new Button(tmpComposite2, SWT.CHECK);
		bOmitPacking.setText("Omit packing");
		bOmitPacking.addSelectionListener(this);
		
		bDisableOptimisation = new Button(tmpComposite2, SWT.CHECK);
		bDisableOptimisation.setText("Disable optimization");
		bDisableOptimisation.addSelectionListener(this);

		
		// page settings
		JNC.getNextButton().setVisible(true);
		updateData(); // will handle the next button
		JNC.getNextButton().addSelectionListener(this);
		JNC.getPreviousButton().addSelectionListener(this);
		lTitle.addDisposeListener(this);
	}

	// --------------- ModifyListener ---------------

	public void modifyText(ModifyEvent e)
	{
		if(ignoreEvents) return;

		if(e.getSource() == tJavaLibPath)
		{
			String s = tJavaLibPath.getText();
			if(s.trim().length() == 0) s = null;
			AppController.getAppController().getCurrentProject().setJavaLibPath(s);
		} else if(e.getSource() == tWindows)
		{
			String s = tWindows.getText();
			File f = (s.trim().length() == 0) ? null : new File(s);
			AppController.getAppController().getCurrentProject().setWindowsFile(f);
			updateNextButton();
		} else if(e.getSource() == tLinux)
		{
			String s = tLinux.getText();
			File f = (s.trim().length() == 0) ? null : new File(s);
			AppController.getAppController().getCurrentProject().setLinuxFile(f);
			updateNextButton();
		}
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == JNC.getNextButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_ADVANCED_SETTINGS);
		} else if(e.getSource() == JNC.getPreviousButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_SOURCE);
		} else if(e.getSource() == bOpenMainClass)
		{
			MainClassDialog mainClassDialog = new MainClassDialog(JNC.getContentComposite().getShell());
			mainClassDialog.setBlockOnOpen(true);
			Object[] oa = (Object[])mainClassDialog.open();
			if(oa == null) return;

			File outputDirSuggestion = (File)oa[0];
			File mainClassRessource = (File)oa[1];
			String mainClass = (String)oa[2];

			tMainClass.setText(mainClass);
			AppController.getAppController().getCurrentProject().setMainClass(mainClassRessource, mainClass);
			updateNextButton();

			if(outputDirSuggestion == null) return;
			File outFile = new File(outputDirSuggestion, mainClass.substring(mainClass.lastIndexOf('.')+1));
			if(tWindows.getText().equals(""))
			{
				File outFile2 = new File(outFile.getParentFile(), outFile.getName()+".exe");
				tWindows.setText(outFile2.toString());
				AppController.getAppController().getCurrentProject().setWindowsFile(outFile2);
			}
			if(tLinux.getText().equals(""))
			{
				tLinux.setText(outFile.toString());
				AppController.getAppController().getCurrentProject().setLinuxFile(outFile);
			}
		} else if(e.getSource() == bOpenWindows || e.getSource() == bOpenLinux)
		{
			boolean win = (e.getSource() == bOpenWindows);
			
			FileDialog fileDialog = new FileDialog(JNC.getContentComposite().getShell(), SWT.SAVE);
			if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
			fileDialog.setText("Where to save the " + (win ? "Windows" : "Linux") + " executable?");
			String ret = fileDialog.open();
			if(ret == null) return;

			File f = new File(ret);
			AppController.curDir = f.getParentFile();

			if(win)
			{
				tWindows.setText(f.toString());
				AppController.getAppController().getCurrentProject().setWindowsFile(f);
			} else
			{
				tLinux.setText(f.toString());
				AppController.getAppController().getCurrentProject().setLinuxFile(f);
			}
			updateNextButton();
		} else if(e.getSource() == bUseCni)
		{
			AppController.getAppController().getCurrentProject().
					setUseCni(bUseCni.getSelection());
		} else if(e.getSource() == bWindows)
		{
			AppController.getAppController().getCurrentProject().
					setCompileWindows(bWindows.getSelection());
			updateWindowsSettings();
			updateNextButton();
		} else if(e.getSource() == bLinux)
		{
			AppController.getAppController().getCurrentProject().
					setCompileLinux(bLinux.getSelection());
			updateLinuxSettings();
			updateNextButton();
		} else if(e.getSource() == bOmitStripping)
		{
			AppController.getAppController().getCurrentProject().
					setOmitStripping(bOmitStripping.getSelection());
		} else if(e.getSource() == bOmitPacking)
		{
			AppController.getAppController().getCurrentProject().
					setOmitPacking(bOmitPacking.getSelection());
		} else if(e.getSource() == bDisableOptimisation)
		{
			AppController.getAppController().getCurrentProject().
					setDisableOptimisation(bDisableOptimisation.getSelection());
		} else if(e.getSource() == bIcon)
		{
			AppController.getAppController().getCurrentProject().
					setUseIcon(bIcon.getSelection());
			updateWindowsSettings();
		} else if(e.getSource() == bOpenIcon)
		{
			FileDialog fileDialog = new FileDialog(JNC.getContentComposite().getShell(), SWT.OPEN);
			if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
			fileDialog.setText("Select icon for the exe file");
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
		JNC.getNextButton().removeSelectionListener(this);
		JNC.getPreviousButton().removeSelectionListener(this);
	}


	// --------------- private methods ---------------

	private void updateData()
	{
		JNCProject project = AppController.getAppController().getCurrentProject();
		ignoreEvents = true;

		if(project.getMainClass() != null) tMainClass.setText(project.getMainClass());
		if(project.getJavaLibPath() != null) tJavaLibPath.setText(project.getJavaLibPath());
		bUseCni.setSelection(project.getUseCni());

		bWindows.setSelection(project.getCompileWindows());
		if(project.getWindowsFile() != null) tWindows.setText(project.getWindowsFile().toString());
		bLinux.setSelection(project.getCompileLinux());
		if(project.getLinuxFile() != null) tLinux.setText(project.getLinuxFile().toString());

		bIcon.setSelection(project.getUseIcon());
		if(project.getIconFile() != null) tIcon.setText(project.getIconFile().toString());
		bHideConsole.setSelection(project.getHideConsole());
		
		bOmitStripping.setSelection(project.getOmitStripping());
		bOmitPacking.setSelection(project.getOmitPacking());
		bDisableOptimisation.setSelection(project.getDisableOptimisation());

		updateWindowsSettings();
		updateLinuxSettings();
		updateNextButton();

		ignoreEvents = false;
	}

	private void updateWindowsSettings()
	{
		JNCProject project = AppController.getAppController().getCurrentProject();

		tWindows.setEnabled(project.getCompileWindows());
		bOpenWindows.setEnabled(project.getCompileWindows());

		bIcon.setEnabled(project.getCompileWindows());
		bOpenIcon.setEnabled(project.getCompileWindows() && project.getUseIcon());
		bHideConsole.setEnabled(project.getCompileWindows());
		
		tIcon.setEnabled(project.getCompileWindows() && project.getUseIcon());
		tIcon.setBackground((project.getCompileWindows() && project.getUseIcon()) ?
				Display.getDefault().getSystemColor(SWT.COLOR_WHITE) :
				bHideConsole.getBackground());
	}

	private void updateLinuxSettings()
	{
		JNCProject project = AppController.getAppController().getCurrentProject();
		tLinux.setEnabled(project.getCompileLinux());
		bOpenLinux.setEnabled(project.getCompileLinux());
	}

	private void updateNextButton()
	{
		JNCProject project = AppController.getAppController().getCurrentProject();
		boolean b = false;
		try
		{
			if(project.getCompileWindows() && project.getWindowsFile() == null) return;
			if(project.getCompileLinux() && project.getLinuxFile() == null) return;
			if(project.getMainClass() == null) return;
			if(!project.getCompileWindows() && !project.getCompileLinux()) return;
			if(project.getCompileWindows() && project.getCompileLinux() &&
					project.getLinuxFile().equals(project.getWindowsFile())) return;
			b = true;
		} finally
		{
			JNC.getNextButton().setEnabled(b);
		}
	}
}
