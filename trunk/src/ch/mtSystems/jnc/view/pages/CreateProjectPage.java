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

import ch.mtSystems.jnc.control.AppController;
import ch.mtSystems.jnc.model.JNCProject;
import ch.mtSystems.jnc.view.JNC;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class CreateProjectPage extends WizzardPage implements SelectionListener, DisposeListener
{
	private static Image imgOpen = JNC.loadImage("open.png");


	private Button rbNewProject;
	private Button rbOpenProject, bOpenProject, rbKeepCurrent;
	private Text tOpenProject;

	private ArrayList<Button> buttonList = new ArrayList<Button>();


	public CreateProjectPage()
	{
		Label lTitle = new Label(JNC.getContentComposite(), SWT.NONE);
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getDefault(), fd));
		lTitle.setText("Start: Create/Open a project");

		rbNewProject = new Button(JNC.getContentComposite(), SWT.RADIO);
		rbNewProject.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, 10, 15));
		rbNewProject.setText("Create new project");
		rbNewProject.addSelectionListener(this);
		buttonList.add(rbNewProject);

		Composite openComposite = new Composite(JNC.getContentComposite(), SWT.NO_RADIO_GROUP);
		openComposite.setLayout(LayoutUtilities.createGridLayout(3, 0));
		openComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		rbOpenProject = new Button(openComposite, SWT.RADIO);
		rbOpenProject.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
		rbOpenProject.setText("Open project:");
		rbOpenProject.addSelectionListener(this);
		buttonList.add(rbOpenProject);

		tOpenProject = new Text(openComposite, SWT.BORDER|SWT.READ_ONLY);
		tOpenProject.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tOpenProject.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		bOpenProject = new Button(openComposite, SWT.NONE);
		bOpenProject.setImage(imgOpen);
		bOpenProject.addSelectionListener(this);

		JNCProject project = AppController.getAppController().getCurrentProject();
		if(project != null)
		{
			rbKeepCurrent = new Button(JNC.getContentComposite(), SWT.RADIO);
			rbKeepCurrent.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
			rbKeepCurrent.setText("Keep current project:");
			rbKeepCurrent.setSelection(true);
			rbKeepCurrent.addSelectionListener(this);
			buttonList.add(rbKeepCurrent);

			Label fileLabel = new Label(JNC.getContentComposite(), SWT.NONE);
			fileLabel.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 40));

			File saveFile = project.getSaveFile();
			fileLabel.setText("- " + ((saveFile != null) ? saveFile.toString() : "not yet saved"));
		}

		// page settings
		JNC.getNextButton().setEnabled(project != null);
		JNC.getNextButton().addSelectionListener(this);
		JNC.getPreviousButton().setVisible(true);
		JNC.getPreviousButton().addSelectionListener(this);
		lTitle.addDisposeListener(this);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == JNC.getNextButton())
		{
			AppController ap = AppController.getAppController();

			if(ap.getCurrentProject() != null && rbNewProject.getSelection() == true)
			{
				String title = "discard current project?";
				String msg = "You have currently another project that will be discarded.\nContinue?";

				MessageBox messageBox = new MessageBox(JNC.getContentComposite().getShell(),
						SWT.ICON_QUESTION|SWT.YES|SWT.NO);
				messageBox.setText(title);
				messageBox.setMessage(msg);
				if(messageBox.open() != SWT.YES) return;
			}

			if(rbNewProject.getSelection())
			{
				ap.setCurrentProject(new JNCProject());
			} else if(rbOpenProject.getSelection())
			{
				try
				{
					File f = new File(tOpenProject.getText());
					ap.setCurrentProject(JNCProject.open(f));
				} catch(Exception ex)
				{
					ex.printStackTrace();

					String title = "error on open";
					String msg = "An error occured while trying to open:\n" + ex.getMessage();

					MessageBox mb = new MessageBox(JNC.getContentComposite().getShell(),
							SWT.ICON_ERROR|SWT.OK);
					mb.setText(title);
					mb.setMessage(msg);
					mb.open();

					return;
				}
			}

			ap.loadPage(AppController.PAGE_SOURCE);
		} else if(e.getSource() == JNC.getPreviousButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_INTRODUCTION);
		} else if(e.getSource() == bOpenProject)
		{
			FileDialog fileDialog = new FileDialog(JNC.getContentComposite().getShell(), SWT.OPEN);
			if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
			fileDialog.setText("Open Project");
			fileDialog.setFilterExtensions(new String[] { "*.jnc" });
			fileDialog.setFilterNames(new String[] { "JNC Project (*.jnc)" });
			String ret = fileDialog.open();
			if(ret != null) setProjectFile(new File(ret));
		} else if(buttonList.contains(e.getSource()))
		{
			for(int i=0; i<buttonList.size(); i++)
			{
				Button b = buttonList.get(i);
				b.setSelection(b == e.getSource());
			}

			JNC.getNextButton().setEnabled(rbOpenProject.getSelection() ?
					tOpenProject.getCharCount() > 0 : true);
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) { }
	
	// --------------- public methods ---------------
	
	public void setProjectFile(File f)
	{
		if(f == null) return;

		AppController.curDir = f.getParentFile();
		tOpenProject.setText(f.toString());

		Event newEvent = new Event();
		newEvent.widget = rbOpenProject;
		widgetSelected(new SelectionEvent(newEvent));
	}


	// --------------- DisposeListener ---------------

	public void widgetDisposed(DisposeEvent e)
	{
		JNC.getNextButton().removeSelectionListener(this);
		JNC.getPreviousButton().removeSelectionListener(this);
	}
}
