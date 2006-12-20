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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import ch.mtSystems.jnc.control.AppController;
import ch.mtSystems.jnc.model.JNCProject;
import ch.mtSystems.jnc.model.utilities.ClassUtilities;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class MainClassDialog extends Dialog implements SelectionListener
{
	private Shell shell;

	private Button bFromFiles, bFromDirectory, bFromJar;
	private List lClasses;

	private Composite listComposite;
	private Label lList;
	private Combo cList;

	private Button bOk, bCancel;

	private File outputDirSuggestion;
	private File choosenClassRessource;
	private String choosenClass;

	private boolean blockOnOpen = false;


	public MainClassDialog(Shell parent)
	{
		super(parent);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == bFromFiles && bFromFiles.getSelection())
		{
			chooseFromFiles();
		} else if(e.getSource() == bFromDirectory && bFromDirectory.getSelection())
		{
			chooseFromDirectory();
		} else if(e.getSource() == bFromJar && bFromJar.getSelection())
		{
			chooseFromJar();
		} else if(e.getSource() == cList)
		{
			listElementSelected();
		} else if(e.getSource() == lClasses)
		{
			classSelected();
		} else if(e.getSource() == bOk)
		{
			okPressed();
		} else if(e.getSource() == bCancel)
		{
			shell.dispose();
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) { }


	// --------------- public methods ---------------
	
	public void setBlockOnOpen(boolean block)
	{
		blockOnOpen = block;
	}

	public Object open()
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL|SWT.RESIZE);
		shell.setText("Please choose the main class");
		shell.setSize(400, 300);
		
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = 3;
		formLayout.marginHeight = 3;
		formLayout.spacing = 5;
		shell.setLayout(formLayout);

		buildContens(shell);
		shell.open();
		if(blockOnOpen) runEventLoop(shell);

		return (choosenClass == null) ? null : new Object[] {
				outputDirSuggestion, choosenClassRessource, choosenClass };
	}


	// --------------- private methods ---------------

	private void buildContens(Shell shell)
	{
		JNCProject project = AppController.getAppController().getCurrentProject();

		bFromFiles = new Button(shell, SWT.RADIO);
		bFromFiles.setText("Choose from files");
		bFromFiles.addSelectionListener(this);
		bFromFiles.setEnabled(project.getFiles().length > 0);

		bFromDirectory = new Button(shell, SWT.RADIO);
		bFromDirectory.setText("Choose from a directory");
		bFromDirectory.addSelectionListener(this);
		bFromDirectory.setEnabled(project.getDirectories().length > 0);

		bFromJar = new Button(shell, SWT.RADIO);
		bFromJar.setText("Choose from a jar");
		bFromJar.addSelectionListener(this);
		bFromJar.setEnabled(project.getJars().length > 0);

		Label sep1 = new Label(shell, SWT.SEPARATOR|SWT.HORIZONTAL);


		lClasses = new List(shell, SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
		lClasses.addSelectionListener(this);
		lClasses.addMouseListener(new MouseAdapter()
				{
					public void mouseDoubleClick(MouseEvent e)
					{
						if(lClasses.getSelectionIndex() > -1) okPressed();
					}
				});

		Label sep2 = new Label(shell, SWT.SEPARATOR|SWT.HORIZONTAL);

		Composite tmpComposite = new Composite(shell, SWT.NONE);
		tmpComposite.setLayout(LayoutUtilities.createGridLayout(1, 0));
		
		Composite buttonComposite = new Composite(tmpComposite, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

		GridLayout gridLayout = LayoutUtilities.createGridLayout(2, 0);
		gridLayout.horizontalSpacing = 15;
		buttonComposite.setLayout(gridLayout);

		bOk = new Button(buttonComposite, SWT.NONE);
		bOk.setText("Ok");
		bOk.addSelectionListener(this);
		bOk.setEnabled(false);

		bCancel = new Button(buttonComposite, SWT.NONE);
		bCancel.setText("Cancel");
		bCancel.addSelectionListener(this);


		bFromDirectory.setLayoutData(createFormData(new FormAttachment(bFromFiles),     null));
		bFromJar.      setLayoutData(createFormData(new FormAttachment(bFromDirectory), null));
		sep1.          setLayoutData(createFormData(new FormAttachment(bFromJar),       null));
		lClasses.      setLayoutData(createFormData(new FormAttachment(sep1),           new FormAttachment(sep2)));
		sep2.          setLayoutData(createFormData(null,                               new FormAttachment(tmpComposite)));
		tmpComposite.  setLayoutData(createFormData(null,                               new FormAttachment(100, 0)));


		if(bFromFiles.isEnabled() && !bFromDirectory.isEnabled() && !bFromJar.isEnabled())
		{
			bFromFiles.setSelection(true);
			chooseFromFiles();
		} else if(!bFromFiles.isEnabled() &&  bFromDirectory.isEnabled() && !bFromJar.isEnabled())
		{
			bFromDirectory.setSelection(true);
			chooseFromDirectory();
		} else if(!bFromFiles.isEnabled() && !bFromDirectory.isEnabled() &&  bFromJar.isEnabled())
		{
			bFromJar.setSelection(true);
			chooseFromJar();
		}
	}

	private void chooseFromFiles()
	{
		bFromDirectory.setSelection(false);
		bFromJar.setSelection(false);
		removeList();

		lClasses.removeAll();
		bOk.setEnabled(false);

		File[] fa = AppController.getAppController().getCurrentProject().getFiles();
		for(int i=0; i<fa.length; i++) lClasses.add(fa[i].toString());

		if(lClasses.getItemCount() == 1)
		{
			lClasses.select(0);
			classSelected();
		}
	}

	private void chooseFromDirectory()
	{
		bFromFiles.setSelection(false);
		bFromJar.setSelection(false);

		lClasses.removeAll();
		bOk.setEnabled(false);

		addList("Directory:", AppController.getAppController().getCurrentProject().getDirectories());
	}

	private void chooseFromJar()
	{
		bFromDirectory.setSelection(false);
		bFromFiles.setSelection(false);

		lClasses.removeAll();
		bOk.setEnabled(false);

		addList("Jar:", AppController.getAppController().getCurrentProject().getJars());
	}

	private void listElementSelected()
	{
		lClasses.removeAll();
		bOk.setEnabled(false);

		File f = new File(cList.getItem(cList.getSelectionIndex()));

			 if(bFromDirectory.getSelection()) addFilesFromDir(f);
		else if(bFromJar.getSelection())       addFilesFromJar(f);

		if(lClasses.getItemCount() == 1)
		{
			lClasses.select(0);
			classSelected();
		}
	}

	private void classSelected()
	{
		bOk.setEnabled(true);
	}

	private void addList(String text, File[] listContent)
	{
		removeList();
		
		listComposite = new Composite(shell, SWT.NONE);
		listComposite.setLayout(LayoutUtilities.createGridLayout(2, 0));

		FormData fdClassList = (FormData)lClasses.getLayoutData();
		listComposite.setLayoutData(createFormData(new FormAttachment(fdClassList.top.control), null));
		fdClassList.top.control = listComposite;

		lList = new Label(listComposite, SWT.NONE);
		lList.setText(text);

		cList = new Combo(listComposite, SWT.BORDER|SWT.READ_ONLY);
		cList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cList.addSelectionListener(this);
		for(int i=0; i<listContent.length; i++) cList.add(listContent[i].toString());

		shell.layout();

		if(cList.getItemCount() == 1)
		{
			cList.select(0);
			listElementSelected();
		}
	}

	private void removeList()
	{
		if(listComposite != null)
		{
			FormData fdList = (FormData)listComposite.getLayoutData();
			FormData fdClassList = (FormData)lClasses.getLayoutData();
			fdClassList.top.control = fdList.top.control;

			listComposite.dispose();
			listComposite = null;
			shell.layout();
		}
	}

	private void addFilesFromDir(File dir)
	{
		ArrayList<String> al = new ArrayList<String>();
		getFiles(dir, al);

		String[] sa = al.toArray(new String[0]);
		Arrays.sort(sa);

		lClasses.setItems(sa);
	}

	private void getFiles(File dir, ArrayList<String> al)
	{
		File[] fa = dir.listFiles();
		for(int i=0; i<fa.length; i++)
		{
			if(fa[i].isDirectory())
			{
				getFiles(fa[i], al);
			} else
			{
				String name = fa[i].getName();
				if(name.endsWith(".java") || name.endsWith(".class")) al.add(fa[i].toString());
			}
		}
	}

	private void addFilesFromJar(File jar)
	{
		try
		{
			JarFile jarFile = new JarFile(jar);
			Enumeration e = jarFile.entries();

			ArrayList<String> al = new ArrayList<String>();

			while(e.hasMoreElements())
			{
				JarEntry jarEntry = (JarEntry)e.nextElement();
				if(jarEntry.isDirectory()) continue;

				String name = jarEntry.getName();
				if(name.endsWith(".class")) al.add(name);
			}

			String[] sa = al.toArray(new String[0]);
			Arrays.sort(sa);

			lClasses.setItems(sa);
		} catch(IOException ex)
		{
			ex.printStackTrace();

			String title = "error on read";
			String msg = "An error occured while trying to read the jar file:\n" + ex.getMessage();

			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR|SWT.OK);
			messageBox.setText(title);
			messageBox.setMessage(msg);
			messageBox.open();
		}
	}

	private void okPressed()
	{
		try
		{
			setChoosenClass();
			shell.dispose();
		} catch(Exception ex)
		{
			ex.printStackTrace();

			String title = "error on read";
			String msg = "An error occured while trying to read the selected file:\n" + ex.getMessage();

			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR|SWT.OK);
			messageBox.setText(title);
			messageBox.setMessage(msg);
			messageBox.open();
		}
	}

	private void setChoosenClass() throws Exception
	{
		String sSelected = lClasses.getItem(lClasses.getSelectionIndex());

		choosenClassRessource = (bFromFiles.getSelection()) ?
				new File(sSelected) :
				new File(cList.getItem(cList.getSelectionIndex()));

		if(bFromFiles.getSelection() || bFromDirectory.getSelection())
		{
			choosenClass = ClassUtilities.getClassName(new File(sSelected));
		} else if(bFromJar.getSelection())
		{
			choosenClass = sSelected.substring(0, sSelected.lastIndexOf('.')).replaceAll("/", ".");
		}

		if(bFromFiles.getSelection())
		{
			// f.toString() -> Windows: foo\bar\FooBar.java, Linux: foo/bar/FooBar.java
			int endIndex = Math.max(
					sSelected.lastIndexOf(choosenClass.replaceAll("\\.", "\\\\")),
					sSelected.lastIndexOf(choosenClass.replaceAll("\\.", "/")));
			outputDirSuggestion = (endIndex > -1) ?
					new File(sSelected.substring(0, endIndex-1)) :
					new File(sSelected).getParentFile();
		} else if(bFromDirectory.getSelection())
		{
			outputDirSuggestion = choosenClassRessource;
		} else if(bFromJar.getSelection())
		{
			outputDirSuggestion = choosenClassRessource.getParentFile();
		}
	}

	private void runEventLoop(Shell loopShell)
	{
		Display d = Display.getDefault();
		while(!loopShell.isDisposed())
		{
			if(!d.readAndDispatch()) d.sleep();
		}
		d.update();
	}

	private FormData createFormData(FormAttachment top, FormAttachment bottom)
	{
		FormData formData = new FormData();
		
		if(top != null) formData.top = top;
		if(bottom != null) formData.bottom = bottom;
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);

		return formData;
	}
}
