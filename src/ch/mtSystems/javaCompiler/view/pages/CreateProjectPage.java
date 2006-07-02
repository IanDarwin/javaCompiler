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
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.model.JavaCompilerProject;
import ch.mtSystems.javaCompiler.model.projects.ManagedAwtSwingProject;
import ch.mtSystems.javaCompiler.model.projects.ObjectProject;
import ch.mtSystems.javaCompiler.model.projects.UnmanagedProject;
import ch.mtSystems.javaCompiler.model.projects.ManagedJFaceProject;
import ch.mtSystems.javaCompiler.model.projects.ManagedSwtProject;
import ch.mtSystems.javaCompiler.view.JavaCompilerGui;
import ch.mtSystems.javaCompiler.view.utilities.LayoutUtilities;


public class CreateProjectPage implements SelectionListener, DisposeListener
{
	private static Image imgOpen = new Image(Display.getCurrent(), "ressources/open.png");


	private Button rbUnmanagedProject, rbSwtProject, rbJFaceProject, rbAwtSwingProject, rbObjectProject;
	private Button rbOpenProject, bOpenProject, rbKeepCurrent;
	private Text tOpenProject;

	private ArrayList<Button> buttonList = new ArrayList<Button>();


	public CreateProjectPage()
	{
		Label lTitle = new Label(JavaCompilerGui.getContentComposite(), SWT.NONE);
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getCurrent(), fd));
		lTitle.setText("Start: create/open a project");

		rbUnmanagedProject = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
		rbUnmanagedProject.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, 10, 15));
		rbUnmanagedProject.setText("create unmanaged application project");
		rbUnmanagedProject.addSelectionListener(this);
		buttonList.add(rbUnmanagedProject);

		rbSwtProject = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
		rbSwtProject.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
		rbSwtProject.setText("create managed SWT application project");
		rbSwtProject.addSelectionListener(this);
		buttonList.add(rbSwtProject);

		rbJFaceProject = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
		rbJFaceProject.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
		rbJFaceProject.setText("create managed JFace application project");
		rbJFaceProject.addSelectionListener(this);
		buttonList.add(rbJFaceProject);

		rbAwtSwingProject = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
		rbAwtSwingProject.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
		rbAwtSwingProject.setText("create managed AWT or Swing application project");
		rbAwtSwingProject.addSelectionListener(this);
		buttonList.add(rbAwtSwingProject);

		rbObjectProject = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
		rbObjectProject.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
		rbObjectProject.setText("create jar object project");
		rbObjectProject.addSelectionListener(this);
		buttonList.add(rbObjectProject);

		Composite openComposite = new Composite(JavaCompilerGui.getContentComposite(), SWT.NO_RADIO_GROUP);
		openComposite.setLayout(LayoutUtilities.createGridLayout(3, 0));
		openComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		rbOpenProject = new Button(openComposite, SWT.RADIO);
		rbOpenProject.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
		rbOpenProject.setText("open project:");
		rbOpenProject.addSelectionListener(this);
		buttonList.add(rbOpenProject);

		tOpenProject = new Text(openComposite, SWT.BORDER|SWT.READ_ONLY);
		tOpenProject.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tOpenProject.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		bOpenProject = new Button(openComposite, SWT.NONE);
		bOpenProject.setImage(imgOpen);
		bOpenProject.addSelectionListener(this);

		if(AppController.getAppController().getCurrentProject() != null)
		{
			rbKeepCurrent = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
			rbKeepCurrent.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
			rbKeepCurrent.setText("keep current project");
			rbKeepCurrent.setSelection(true);
			rbKeepCurrent.addSelectionListener(this);
			buttonList.add(rbKeepCurrent);
		}

		// page settings
		JavaCompilerGui.getNextButton().setEnabled(AppController.getAppController().getCurrentProject() != null);
		JavaCompilerGui.getNextButton().addSelectionListener(this);
		JavaCompilerGui.getPreviousButton().setVisible(true);
		JavaCompilerGui.getPreviousButton().addSelectionListener(this);
		JavaCompilerGui.setTitle("JavaCompiler v" + JavaCompilerGui.VERSION + " - create/open a project");

		lTitle.addDisposeListener(this);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == JavaCompilerGui.getNextButton())
		{
			AppController ap = AppController.getAppController();

			if(ap.getCurrentProject() != null &&
			   (rbUnmanagedProject.getSelection() == true || rbSwtProject.getSelection() == true ||
				rbJFaceProject.getSelection() == true || rbAwtSwingProject.getSelection() == true ||
				rbObjectProject.getSelection() == true))
			{
				String title = "discard current project?";
				String msg = "You have currently another project that will be discarded.\nContinue?";

				MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_QUESTION|SWT.YES|SWT.NO);
				messageBox.setText(title);
				messageBox.setMessage(msg);
				if(messageBox.open() != SWT.YES) return;
			}

			if(rbUnmanagedProject.getSelection())
			{
				ap.setCurrentProject(new UnmanagedProject());
			} else if(rbSwtProject.getSelection())
			{
				ap.setCurrentProject(new ManagedSwtProject());
			} else if(rbJFaceProject.getSelection())
			{
				ap.setCurrentProject(new ManagedJFaceProject());
			} else if(rbAwtSwingProject.getSelection())
			{
				ap.setCurrentProject(new ManagedAwtSwingProject());
			} else if(rbObjectProject.getSelection())
			{
				ap.setCurrentProject(new ObjectProject());
			} else if(rbOpenProject.getSelection())
			{
				try
				{
					File f = new File(tOpenProject.getText());
					ap.setCurrentProject(JavaCompilerProject.open(f));
				} catch(Exception ex)
				{
					ex.printStackTrace();

					String title = "error on open";
					String msg = "An error occured while trying to open:\n" + ex.getMessage();

					MessageBox mb = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR|SWT.OK);
					mb.setText(title);
					mb.setMessage(msg);
					mb.open();

					return;
				}
			}

			ap.loadPage(AppController.PAGE_SOURCE);
		} else if(e.getSource() == JavaCompilerGui.getPreviousButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_INTRODUCTION);
		} else if(e.getSource() == bOpenProject)
		{
			FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
			if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
			fileDialog.setText("open project");
			fileDialog.setFilterExtensions(new String[] { "*jcp" });
			fileDialog.setFilterNames(new String[] { "JavaCompilerProject (*.jcp)" });
			String ret = fileDialog.open();
			if(ret == null) return;

			File f = new File(ret);
			AppController.curDir = f.getParentFile();

			tOpenProject.setText(f.toString());

			Event newEvent = new Event();
			newEvent.widget = rbOpenProject;
			widgetSelected(new SelectionEvent(newEvent));
		} else if(buttonList.contains(e.getSource()))
		{
			for(int i=0; i<buttonList.size(); i++)
			{
				Button b = buttonList.get(i);
				b.setSelection(b == e.getSource());
			}

			JavaCompilerGui.getNextButton().setEnabled(rbOpenProject.getSelection() ?
					tOpenProject.getCharCount() > 0 : true);
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) { }


	// --------------- DisposeListener ---------------

	public void widgetDisposed(DisposeEvent e)
	{
		JavaCompilerGui.getNextButton().removeSelectionListener(this);
		JavaCompilerGui.getPreviousButton().removeSelectionListener(this);
	}
}
