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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import ch.mtSystems.jnc.control.AppController;
import ch.mtSystems.jnc.model.JNCProject;
import ch.mtSystems.jnc.view.JNC;
import ch.mtSystems.jnc.view.dialogs.InputDialog;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class AdvancedSettingsPage extends WizzardPage implements SelectionListener, DisposeListener
{
	private static Image imgAdd = JNC.loadImage("flagAdd.png");
	private static Image imgRemove = JNC.loadImage("flagRemove.png");

	private Table tGcjFlags;
	private Button bAddFlag, bRemoveFlag, bShowCommands;
	private Button bExcludeGui, bExcludeJce, bAddGnuRegex, bDontCacheJars;


	public AdvancedSettingsPage()
	{
		Label lTitle = new Label(JNC.getContentComposite(), SWT.NONE);
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getDefault(), fd));
		lTitle.setText("Step 3 of 4: Advanced settings (optional)");


		Group groupGcjFlags = new Group(JNC.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupGcjFlags.setLayout(LayoutUtilities.createGridLayout(2, 3));
		groupGcjFlags.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupGcjFlags.setText("Custom GCJ flags");

		tGcjFlags = new Table(groupGcjFlags, SWT.BORDER|SWT.MULTI|SWT.CHECK);
		tGcjFlags.addSelectionListener(this);
		GridData gdFlags = new GridData(GridData.FILL_HORIZONTAL);
		gdFlags.verticalSpan = 2;
		gdFlags.heightHint = 55;
		tGcjFlags.setLayoutData(gdFlags);

		bAddFlag = new Button(groupGcjFlags, SWT.NONE);
		bAddFlag.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, true));
		bAddFlag.setImage(imgAdd);
		bAddFlag.addSelectionListener(this);

		bRemoveFlag = new Button(groupGcjFlags, SWT.NONE);
		bRemoveFlag.setImage(imgRemove);
		bRemoveFlag.addSelectionListener(this);

		Label lBoxInfo = new Label(groupGcjFlags, SWT.NONE);
		lBoxInfo.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_HORIZONTAL, 2, 1));
		lBoxInfo.setText("Checkbox: Flag for main compilation only (not for JAR compilations).");

		bShowCommands = new Button(groupGcjFlags, SWT.CHECK);
		bShowCommands.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_HORIZONTAL, 2, 1));
		bShowCommands.setText("Show used commands");
		bShowCommands.addSelectionListener(this);

		Group groupClassLibrary = new Group(JNC.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupClassLibrary.setLayout(LayoutUtilities.createGridLayout(1, 3));
		groupClassLibrary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupClassLibrary.setText("Class Library");
		
		bExcludeGui = new Button(groupClassLibrary, SWT.CHECK);
		bExcludeGui.setText("Exclude GUI (java.applet, java.awt, javax.swing, javax.print, javax.imageio)");
		bExcludeGui.addSelectionListener(this);
		
		bExcludeJce = new Button(groupClassLibrary, SWT.CHECK);
		bExcludeJce.setText("Exclude JCE (javax.crypto, javax.net.ssl)");
		bExcludeJce.addSelectionListener(this);
		
		bAddGnuRegex = new Button(groupClassLibrary, SWT.CHECK);
		bAddGnuRegex.setText("Add GNU regex (needed if there are invalid regular expressions)");
		bAddGnuRegex.addSelectionListener(this);


		Group groupMiscellaneous = new Group(JNC.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupMiscellaneous.setLayout(LayoutUtilities.createGridLayout(1, 3));
		groupMiscellaneous.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupMiscellaneous.setText("Miscellaneous");

		bDontCacheJars = new Button(groupMiscellaneous, SWT.CHECK);
		bDontCacheJars.setText("Don't cache compiled jars");
		bDontCacheJars.addSelectionListener(this);
		

		JNC.getNextButton().setVisible(true);
		JNC.getNextButton().setEnabled(true);
		JNC.getNextButton().addSelectionListener(this);
		JNC.getPreviousButton().addSelectionListener(this);
		lTitle.addDisposeListener(this);
		updateData();
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == JNC.getNextButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_COMPILATION);
		} else if(e.getSource() == JNC.getPreviousButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_BASIC_SETTINGS);
		} else if(e.getSource() == tGcjFlags)
		{
			if((e.detail & SWT.CHECK) == SWT.CHECK)
			{
				TableItem item = (TableItem)e.item;
				AppController.getAppController().getCurrentProject().
						setFlagMainCompilationOnly(item.getText(), item.getChecked());
			}
		} else if(e.getSource() == bAddFlag)
		{
			InputDialog inputDialog = new InputDialog(JNC.getContentComposite().getShell());
			inputDialog.setTitle("New GCJ flag");
			inputDialog.setMessage("GCJ flag to add:");
			String flag = inputDialog.open();
			if(flag == null) return;
			
			AppController.getAppController().getCurrentProject().addGcjFlags(flag, false);
			(new TableItem(tGcjFlags, SWT.NONE)).setText(flag);
		} else if(e.getSource() == bRemoveFlag)
		{
			int[] selIndices = tGcjFlags.getSelectionIndices();
			for(int i=selIndices.length-1; i>=0; i--)
			{
				String flag = tGcjFlags.getItem(selIndices[i]).getText();
				AppController.getAppController().getCurrentProject().removeGcjFlag(flag);
				tGcjFlags.remove(selIndices[i]);
			}
		} else if(e.getSource() == bShowCommands)
		{
			JNCProject project = AppController.getAppController().getCurrentProject();
			project.setShowCommands(bShowCommands.getSelection());
		} else if(e.getSource() == bExcludeGui)
		{
			JNCProject project = AppController.getAppController().getCurrentProject();
			project.setExcludeGui(bExcludeGui.getSelection());
		} else if(e.getSource() == bExcludeJce)
		{
			JNCProject project = AppController.getAppController().getCurrentProject();
			project.setExcludeJce(bExcludeJce.getSelection());
		} else if(e.getSource() == bAddGnuRegex)
		{
			JNCProject project = AppController.getAppController().getCurrentProject();
			project.setAddGnuRegex(bAddGnuRegex.getSelection());
		} else if(e.getSource() == bDontCacheJars)
		{
			JNCProject project = AppController.getAppController().getCurrentProject();
			project.setDontCacheJars(bDontCacheJars.getSelection());
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

		for(String flag : project.getGcjFlags())
		{
			TableItem item = new TableItem(tGcjFlags, SWT.NONE);
			item.setText(flag);
			item.setChecked(project.getFlagMainCompilationOnly(flag));
		}

		bShowCommands.setSelection(project.getShowCommands());
		bExcludeGui.setSelection(project.getExcludeGui());
		bExcludeJce.setSelection(project.getExcludeJce());
		bAddGnuRegex.setSelection(project.getAddGnuRegex());
		bDontCacheJars.setSelection(project.getDontCacheJars());
	}
}
