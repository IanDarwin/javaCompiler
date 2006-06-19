package ch.mtSystems. javaCompiler.view.dialogs;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.model.JavaCompilerProject;
import ch.mtSystems.javaCompiler.model.utilities.ClassUtilities;
import ch.mtSystems.javaCompiler.view.utilities.LayoutUtilities;


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


	public MainClassDialog()
	{
		super(Display.getCurrent().getActiveShell());
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

	public Object open()
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL|SWT.RESIZE);
		shell.setText("please choose the main class");
		shell.setSize(400, 300);
		shell.setLayout(new GridLayout());

		buildContens(shell);

		shell.open();
		Display display = parent.getDisplay();
		while(!shell.isDisposed())
		{
			if(!display.readAndDispatch()) display.sleep();
		}

		return (choosenClass == null) ? null : new Object[] {
				outputDirSuggestion, choosenClassRessource, choosenClass };
	}


	// --------------- private methods ---------------

	private void buildContens(Shell shell)
	{
		JavaCompilerProject project = AppController.getAppController().getCurrentProject();

		bFromFiles = new Button(shell, SWT.RADIO);
		bFromFiles.setText("choose from files");
		bFromFiles.addSelectionListener(this);
		bFromFiles.setEnabled(project.getFiles().length > 0);

		bFromDirectory = new Button(shell, SWT.RADIO);
		bFromDirectory.setText("choose from a directory");
		bFromDirectory.addSelectionListener(this);
		bFromDirectory.setEnabled(project.getDirectories().length > 0);

		bFromJar = new Button(shell, SWT.RADIO);
		bFromJar.setText("choose from a jar");
		bFromJar.addSelectionListener(this);
		bFromJar.setEnabled(project.getJars().length > 0);

		new Label(shell, SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		listComposite = new Composite(shell, SWT.NONE);
		listComposite.setLayout(LayoutUtilities.createGridLayout(2, 0));
		listComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lClasses = new List(shell, SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
		lClasses.setLayoutData(new GridData(GridData.FILL_BOTH));
		lClasses.addSelectionListener(this);
		lClasses.addMouseListener(new MouseAdapter()
				{
					public void mouseDoubleClick(MouseEvent e)
					{
						if(lClasses.getSelectionIndex() > -1) okPressed();
					}
				});

		new Label(shell, SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 15;
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayout(gridLayout);
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		bOk = new Button(buttonComposite, SWT.NONE);
		bOk.setText("Ok");
		bOk.addSelectionListener(this);
		bOk.setEnabled(false);

		bCancel = new Button(buttonComposite, SWT.NONE);
		bCancel.setText("Cancel");
		bCancel.addSelectionListener(this);

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

		addList("directory:", AppController.getAppController().getCurrentProject().getDirectories());
	}

	private void chooseFromJar()
	{
		bFromDirectory.setSelection(false);
		bFromFiles.setSelection(false);

		lClasses.removeAll();
		bOk.setEnabled(false);

		addList("jar:", AppController.getAppController().getCurrentProject().getJars());
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
		Control[] ca = listComposite.getChildren();
		for(int i=0; i<ca.length; i++) ca[i].dispose();

		shell.layout();
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

			MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR|SWT.OK);
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

			MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_ERROR|SWT.OK);
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
			if(sSelected.endsWith(".class"))
			{
				choosenClass = ClassUtilities.getFromClass(new File(sSelected));
			} else if(sSelected.endsWith(".java"))
			{
				choosenClass = ClassUtilities.getFromSource(new File(sSelected));
			}
		} else if(bFromJar.getSelection())
		{
			choosenClass = sSelected.substring(0, sSelected.lastIndexOf('.')).replaceAll("/", ".");
		}

		if(bFromFiles.getSelection())
		{
			int endIndex = sSelected.indexOf(choosenClass.replaceAll("\\.", "\\\\"));
			outputDirSuggestion = new File(sSelected.substring(0, endIndex));
		} else if(bFromDirectory.getSelection())
		{
			outputDirSuggestion = choosenClassRessource;
		} else if(bFromJar.getSelection())
		{
			outputDirSuggestion = choosenClassRessource.getParentFile();
		}
	}
}
