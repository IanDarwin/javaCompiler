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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import ch.mtSystems.jnc.control.AppController;
import ch.mtSystems.jnc.model.JNCProject;
import ch.mtSystems.jnc.view.JNC;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class SourcePage extends WizzardPage implements SelectionListener, DisposeListener
{
	private static Image imgOpen = JNC.loadImage("open.png");
	private static Image imgRemove = JNC.loadImage("remove.png");


	private Table tFiles, tDirs, tJars;
	private Button bAddFiles, bRemoveFiles, bAddDirs, bRemoveDirs, bAddJars, bRemoveJars;


	public SourcePage()
	{
		Label lTitle = new Label(JNC.getContentComposite(), SWT.NONE);
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getDefault(), fd));
		lTitle.setText("Step 1 of 4: Source");

		// files
		Group groupFiles = new Group(JNC.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupFiles.setLayout(new GridLayout(2, false));
		groupFiles.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupFiles.setText("Files (.java and .class)");

		tFiles = new Table(groupFiles, SWT.BORDER|SWT.MULTI);
		GridData gdFiles = new GridData(GridData.FILL_BOTH);
		gdFiles.verticalSpan = 2;
		gdFiles.heightHint = 200;
		tFiles.setLayoutData(gdFiles);

		bAddFiles = new Button(groupFiles, SWT.NONE);
		bAddFiles.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, true));
		bAddFiles.setImage(imgOpen);
		bAddFiles.addSelectionListener(this);

		bRemoveFiles = new Button(groupFiles, SWT.NONE);
		bRemoveFiles.setImage(imgRemove);
		bRemoveFiles.addSelectionListener(this);

		// directories
		Group groupDirs = new Group(JNC.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupDirs.setLayout(new GridLayout(2, false));
		groupDirs.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupDirs.setText("Directories (with subdirs)");

		tDirs = new Table(groupDirs, SWT.BORDER|SWT.MULTI);
		GridData gdDirs = new GridData(GridData.FILL_BOTH);
		gdDirs.verticalSpan = 2;
		gdDirs.heightHint = 200;
		tDirs.setLayoutData(gdDirs);

		bAddDirs = new Button(groupDirs, SWT.NONE);
		bAddDirs.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, true));
		bAddDirs.setImage(imgOpen);
		bAddDirs.addSelectionListener(this);

		bRemoveDirs = new Button(groupDirs, SWT.NONE);
		bRemoveDirs.setImage(imgRemove);
		bRemoveDirs.addSelectionListener(this);

		// jars
		Group groupJars = new Group(JNC.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupJars.setLayout(new GridLayout(2, false));
		groupJars.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupJars.setText("Archives (.jar)");

		tJars = new Table(groupJars, SWT.BORDER|SWT.MULTI|SWT.CHECK);
		tJars.addSelectionListener(this);
		GridData gdJars = new GridData(GridData.FILL_BOTH);
		gdJars.verticalSpan = 2;
		gdJars.heightHint = 200;
		tJars.setLayoutData(gdJars);

		bAddJars = new Button(groupJars, SWT.NONE);
		bAddJars.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, true));
		bAddJars.setImage(imgOpen);
		bAddJars.addSelectionListener(this);

		bRemoveJars = new Button(groupJars, SWT.NONE);
		bRemoveJars.setImage(imgRemove);
		bRemoveJars.addSelectionListener(this);
		
		Label lBoxInfo = new Label(groupJars, SWT.NONE);
		lBoxInfo.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_HORIZONTAL, 2, 1));
		lBoxInfo.setText("Checkbox: Compile complete jar. Only needed (referenced) classes otherwise.");

		// keep a little space at the bottom
		new Composite(JNC.getContentComposite(), SWT.NONE).setLayoutData(new GridData(0, 40));

		// page settings
		updateData(); // will handle the next button
		JNC.getNextButton().addSelectionListener(this);
		JNC.getPreviousButton().addSelectionListener(this);
		lTitle.addDisposeListener(this);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == JNC.getNextButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_BASIC_SETTINGS);
		} else if(e.getSource() == JNC.getPreviousButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_CREATE_PROJECT);
		} else if(e.getSource() == bAddFiles)
		{
			FileDialog fileDialog = new FileDialog(JNC.getContentComposite().getShell(), SWT.OPEN|SWT.MULTI);
			if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
			fileDialog.setText("Add Files");
			fileDialog.setFilterExtensions(new String[] { "*.java", "*.class" });
			fileDialog.setFilterNames(new String[] { "Java source files (*.java)", "Java class files (*.class)" });
			fileDialog.open();

			String[] sa = fileDialog.getFileNames();
			if(sa.length == 0) return;
			AppController.curDir = new File(fileDialog.getFilterPath());

			File[] fa = new File[sa.length];
			for(int i=0; i<sa.length; i++) fa[i] = new File(AppController.curDir, sa[i]);
			addSource(0, fa);
		} else if(e.getSource() == bAddDirs)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(JNC.getContentComposite().getShell());
			if(AppController.curDir != null) dirDialog.setFilterPath(AppController.curDir.toString());
			dirDialog.setText("Add Directory");
			String ret = dirDialog.open();
			if(ret == null) return;

			File f = new File(ret);
			AppController.curDir = f;

			addSource(1, new File[] { f });
		} else if(e.getSource() == tJars)
		{
			if((e.detail & SWT.CHECK) == SWT.CHECK)
			{
				TableItem item = (TableItem)e.item;
				File f = new File(item.getText());
				AppController.getAppController().getCurrentProject().
						setCompileCompleteJar(f, item.getChecked());
			}
		} else if(e.getSource() == bAddJars)
		{
			FileDialog fileDialog = new FileDialog(JNC.getContentComposite().getShell(), SWT.OPEN|SWT.MULTI);
			if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
			fileDialog.setText("Add Archives");
			fileDialog.setFilterExtensions(new String[] { "*.jar" });
			fileDialog.setFilterNames(new String[] { "Java archive files (*.jar)" });
			fileDialog.open();

			String[] sa = fileDialog.getFileNames();
			if(sa.length == 0) return;
			AppController.curDir = new File(fileDialog.getFilterPath());

			File[] fa = new File[sa.length];
			for(int i=0; i<sa.length; i++) fa[i] = new File(AppController.curDir, sa[i]);
			addSource(2, fa);
		} else if(e.getSource() == bRemoveFiles)
		{
			removeSelected(0);
		} else if(e.getSource() == bRemoveDirs)
		{
			removeSelected(1);
		} else if(e.getSource() == bRemoveJars)
		{
			removeSelected(2);
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

		File[] faFiles = project.getFiles();
		for(int i=0; i<faFiles.length; i++) (new TableItem(tFiles, SWT.NONE)).setText(faFiles[i].toString());

		File[] faDirs = project.getDirectories();
		for(int i=0; i<faDirs.length; i++) (new TableItem(tDirs, SWT.NONE)).setText(faDirs[i].toString());

		for(File jarFile : project.getJars())
		{
			TableItem item = new TableItem(tJars, SWT.NONE);
			item.setText(jarFile.toString());
			item.setChecked(project.getCompileCompleteJar(jarFile));
		}

		JNC.getNextButton().setEnabled(tFiles.getItemCount() > 0 ||
				tDirs.getItemCount() > 0 || tJars.getItemCount() > 0);
	}

	/**
	 * mode = 0: files
	 * mode = 1: directories
	 * mode = 2: jars
	 */
	private void addSource(int mode, File[] files)
	{
		JNCProject project = AppController.getAppController().getCurrentProject();
		for(int i=0; i<files.length; i++)
		{
			if(mode == 0)
			{
				if(project.addFile(files[i]))
				{
					(new TableItem(tFiles, SWT.NONE)).setText(files[i].toString());
				}
			} else if(mode == 1)
			{
				if(project.addDirectory(files[i]))
				{
					(new TableItem(tDirs, SWT.NONE)).setText(files[i].toString());
				}
			} else if(mode == 2)
			{
				if(project.addJar(files[i], false))
				{
					(new TableItem(tJars, SWT.NONE)).setText(files[i].toString());
				}
			}
		}

		JNC.getNextButton().setEnabled(tFiles.getItemCount() > 0 ||
				tDirs.getItemCount() > 0 || tJars.getItemCount() > 0);
	}

	/**
	 * mode = 0: files
	 * mode = 1: directories
	 * mode = 2: jars
	 */
	private void removeSelected(int mode)
	{
		if(mode == 0)
		{
			int[] selIndices = tFiles.getSelectionIndices();
			for(int i=selIndices.length-1; i>=0; i--)
			{
				File f = new File(tFiles.getItem(selIndices[i]).getText());
				AppController.getAppController().getCurrentProject().removeFile(f);
				tFiles.remove(selIndices[i]);
			}
		} else if(mode == 1)
		{
			int[] selIndices = tDirs.getSelectionIndices();
			for(int i=selIndices.length-1; i>=0; i--)
			{
				File f = new File(tDirs.getItem(selIndices[i]).getText());
				AppController.getAppController().getCurrentProject().removeDirectory(f);
				tDirs.remove(selIndices[i]);
			}
		} else if(mode == 2)
		{
			int[] selIndices = tJars.getSelectionIndices();
			for(int i=selIndices.length-1; i>=0; i--)
			{
				File f = new File(tJars.getItem(selIndices[i]).getText());
				AppController.getAppController().getCurrentProject().removeJar(f);
				tJars.remove(selIndices[i]);
			}
		}

		JNC.getNextButton().setEnabled(tFiles.getItemCount() > 0 ||
				tDirs.getItemCount() > 0 || tJars.getItemCount() > 0);
	}
}
