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

package ch.mtSystems.jnc.view.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.mtSystems.jnc.model.utilities.SettingsMemory;
import ch.mtSystems.jnc.view.JNC;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class SettingsDialog extends Dialog implements SelectionListener
{
	private static final String NO_COMP_SET = "No compiler set!";
	
	
	private Shell shell;
	private Text tWinCompilerPath, tLinCompilerPath, tLicense;
	private Button bAutoDetect, bOpenWin, bOpenLin, bOk, bCancel;


	public SettingsDialog(Shell parent)
	{
		super(parent);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == bAutoDetect)
		{
			boolean autoDetect = bAutoDetect.getSelection();

			// we have to change the settings to get the old user paths
			// but we don't want to save the selection yet, so we change it back afterwards
			SettingsMemory.getSettingsMemory().setAutoDetectCompilers(autoDetect);
			updateCompilerPaths();
			SettingsMemory.getSettingsMemory().setAutoDetectCompilers(!autoDetect);
		} else if(e.getSource() == bOpenWin || e.getSource() == bOpenLin)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(shell);
			String ret = dirDialog.open();
			if(ret == null || ret.length() == 0) return;
			
			if(e.getSource() == bOpenWin)
			{
				tWinCompilerPath.setText(ret);
			} else
			{
				tLinCompilerPath.setText(ret);
			}
		} else if(e.getSource() == bOk)
		{
			boolean autoDetect = bAutoDetect.getSelection();
			SettingsMemory.getSettingsMemory().setAutoDetectCompilers(autoDetect);
			if(!autoDetect)
			{
				String winPath = tWinCompilerPath.getText();
				String linPath = tLinCompilerPath.getText();
				SettingsMemory.getSettingsMemory().setWindowsCompilerPath((winPath.equals(NO_COMP_SET)) ? null : winPath);
				SettingsMemory.getSettingsMemory().setLinuxCompilerPath((linPath.equals(NO_COMP_SET)) ? null : linPath);
			}
			SettingsMemory.getSettingsMemory().setLicense(tLicense.getText());
			shell.dispose();
		} else if(e.getSource() == bCancel)
		{
			shell.dispose();
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) { }


	// --------------- public methods ---------------

	public void open()
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL|SWT.RESIZE);
		shell.setText("Settings");
		shell.setSize(400, 300);
		shell.setLayout(new GridLayout());

		buildContens(shell);

		shell.open();
	}


	// --------------- private methods ---------------

	private void buildContens(Shell shell)
	{
		Composite mainComposite = new Composite(shell, SWT.NONE);
		mainComposite.setLayout(LayoutUtilities.createGridLayout(2, 0));
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group groupCompilerPaths = new Group(mainComposite, SWT.SHADOW_ETCHED_IN);
		groupCompilerPaths.setLayout(new GridLayout(3, false));
		groupCompilerPaths.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_HORIZONTAL, 2, 1));
		groupCompilerPaths.setText("Compiler Paths");
		
		bAutoDetect = new Button(groupCompilerPaths, SWT.CHECK);
		bAutoDetect.setLayoutData(LayoutUtilities.createGridData(3, 1));
		bAutoDetect.setText("Automatically detect most recent compilers");
		bAutoDetect.addSelectionListener(this);
		
		(new Label(groupCompilerPaths, SWT.NONE)).setText("Windows:");
		
		tWinCompilerPath = new Text(groupCompilerPaths, SWT.BORDER);
		tWinCompilerPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		bOpenWin = new Button(groupCompilerPaths, SWT.NONE);
		bOpenWin.setImage(JNC.loadImage("open.png"));
		bOpenWin.addSelectionListener(this);
		
		(new Label(groupCompilerPaths, SWT.NONE)).setText("Linux:");
		
		tLinCompilerPath = new Text(groupCompilerPaths, SWT.BORDER);
		tLinCompilerPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		bOpenLin = new Button(groupCompilerPaths, SWT.NONE);
		bOpenLin.setImage(JNC.loadImage("open.png"));
		bOpenLin.addSelectionListener(this);
		
		(new Label(mainComposite, SWT.NONE)).setText("JNC License:");
		
		tLicense = new Text(mainComposite, SWT.BORDER);
		tLicense.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(shell, SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout gridLayout = LayoutUtilities.createGridLayout(2, 0);
		gridLayout.horizontalSpacing = 15;
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayout(gridLayout);
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		bOk = new Button(buttonComposite, SWT.NONE);
		bOk.setText("Ok");
		bOk.addSelectionListener(this);

		bCancel = new Button(buttonComposite, SWT.NONE);
		bCancel.setText("Cancel");
		bCancel.addSelectionListener(this);

		boolean autoDetect = SettingsMemory.getSettingsMemory().getAutoDetectCompilers();
		bAutoDetect.setSelection(autoDetect);
		updateCompilerPaths();

		String license = SettingsMemory.getSettingsMemory().getLicense();
		if(license != null) tLicense.setText(license);
	}
	
	private void updateCompilerPaths()
	{
		boolean autoDetect = SettingsMemory.getSettingsMemory().getAutoDetectCompilers();
		String winPath = SettingsMemory.getSettingsMemory().getWindowsCompilerPath();
		String linPath = SettingsMemory.getSettingsMemory().getLinuxCompilerPath();

		if(autoDetect)
		{
			tWinCompilerPath.setText((winPath != null) ? winPath : "No compiler found!");
			tLinCompilerPath.setText((linPath != null) ? linPath : "No compiler found!");
		} else
		{
			tWinCompilerPath.setText((winPath != null) ? winPath : NO_COMP_SET);
			tLinCompilerPath.setText((linPath != null) ? linPath : NO_COMP_SET);	
		}

		tWinCompilerPath.setEnabled(!autoDetect);
		bOpenWin.setEnabled(!autoDetect);
		tLinCompilerPath.setEnabled(!autoDetect);
		bOpenLin.setEnabled(!autoDetect);
	}
}
