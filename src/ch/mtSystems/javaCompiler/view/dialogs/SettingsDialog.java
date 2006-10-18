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

package ch.mtSystems.javaCompiler.view.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.model.utilities.SettingsMemory;
import ch.mtSystems.javaCompiler.view.JavaCompilerGui;
import ch.mtSystems.javaCompiler.view.utilities.LayoutUtilities;


public class SettingsDialog extends Dialog implements SelectionListener
{
	private Shell shell;

	private Text tJavac;
	private Button bOpen;

	private Button bOk, bCancel;


	public SettingsDialog()
	{
		super(Display.getDefault().getActiveShell());
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == bOpen)
		{
			FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
			if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
			fileDialog.setText("select a JDK 1.5 javac");
			fileDialog.setFilterExtensions(new String[] { "javac.exe" });
			String ret = fileDialog.open();
			if(ret != null) tJavac.setText(ret);
		} else if(e.getSource() == bOk)
		{
			SettingsMemory.getSettingsMemory().setJavac(tJavac.getText());
			shell.dispose();
		} else if(e.getSource() == bCancel)
		{
			shell.dispose();
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) { }


	// --------------- public methods ---------------

	public Object open()
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL|SWT.RESIZE);
		shell.setText("Settings");
		shell.setSize(400, 300);
		shell.setLayout(new GridLayout());

		buildContens(shell);

		shell.open();
		Display display = parent.getDisplay();
		while(!shell.isDisposed())
		{
			if(!display.readAndDispatch()) display.sleep();
		}

		return null;
	}


	// --------------- private methods ---------------

	private void buildContens(Shell shell)
	{
		Composite mainComposite = new Composite(shell, SWT.NONE);
		mainComposite.setLayout(LayoutUtilities.createGridLayout(1, 0));
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group javaHomeGroup = new Group(mainComposite, SWT.SHADOW_ETCHED_IN);
		javaHomeGroup.setLayout(new GridLayout(2, false));
		javaHomeGroup.setText("JDK 1.5 javac");
		javaHomeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		tJavac = new Text(javaHomeGroup, SWT.BORDER);
		tJavac.setEditable(false);
		tJavac.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 300;
		tJavac.setLayoutData(gridData);

		bOpen = new Button(javaHomeGroup, SWT.NONE);
		bOpen.setImage(JavaCompilerGui.loadImage("open.png"));
		bOpen.setToolTipText("öffnen");
		bOpen.addSelectionListener(this);

		new Label(shell, SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout gridLayout = new GridLayout(2, false);
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

		String javaHome = SettingsMemory.getSettingsMemory().getJavac();
		if(javaHome != null) tJavac.setText(javaHome);
	}
}
