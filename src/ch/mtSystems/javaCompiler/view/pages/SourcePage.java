package ch.mtSystems.javaCompiler.view.pages;

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
import org.eclipse.swt.widgets.List;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.model.JavaCompilerProject;
import ch.mtSystems.javaCompiler.view.JavaCompilerGui;


public class SourcePage implements SelectionListener, DisposeListener
{
	private static Image imgOpen = new Image(Display.getCurrent(), "ressources/open.png");
	private static Image imgRemove = new Image(Display.getCurrent(), "ressources/remove.png");


	private List lFiles, lDirs, lJars;
	private Button bAddFiles, bRemoveFiles, bAddDirectories, bRemoveDirectories, bAddJars, bRemoveJars;


	public SourcePage()
	{
		Label lTitle = new Label(JavaCompilerGui.getContentComposite(), SWT.NONE);
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getCurrent(), fd));
		lTitle.setText("Step 1 of 3: source");

		// files
		Group groupFiles = new Group(JavaCompilerGui.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupFiles.setLayout(new GridLayout(3, false));
		groupFiles.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupFiles.setText("files (.java and .class)");

		lFiles = new List(groupFiles, SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL|SWT.MULTI);
		lFiles.setLayoutData(new GridData(GridData.FILL_BOTH));

		bAddFiles = new Button(groupFiles, SWT.NONE);
		bAddFiles.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
		bAddFiles.setImage(imgOpen);
		bAddFiles.addSelectionListener(this);

		bRemoveFiles = new Button(groupFiles, SWT.NONE);
		bRemoveFiles.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
		bRemoveFiles.setImage(imgRemove);
		bRemoveFiles.addSelectionListener(this);

		// directories
		Group groupDirs = new Group(JavaCompilerGui.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupDirs.setLayout(new GridLayout(3, false));
		groupDirs.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupDirs.setText("directories (with subdirs)");

		lDirs = new List(groupDirs, SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL|SWT.MULTI);
		lDirs.setLayoutData(new GridData(GridData.FILL_BOTH));

		bAddDirectories = new Button(groupDirs, SWT.NONE);
		bAddDirectories.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
		bAddDirectories.setImage(imgOpen);
		bAddDirectories.addSelectionListener(this);

		bRemoveDirectories = new Button(groupDirs, SWT.NONE);
		bRemoveDirectories.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
		bRemoveDirectories.setImage(imgRemove);
		bRemoveDirectories.addSelectionListener(this);

		// jars
		Group groupJars = new Group(JavaCompilerGui.getContentComposite(), SWT.SHADOW_ETCHED_IN);
		groupJars.setLayout(new GridLayout(3, false));
		groupJars.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupJars.setText("archives (.jar)");

		lJars = new List(groupJars, SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL|SWT.MULTI);
		lJars.setLayoutData(new GridData(GridData.FILL_BOTH));

		bAddJars = new Button(groupJars, SWT.NONE);
		bAddJars.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
		bAddJars.setImage(imgOpen);
		bAddJars.addSelectionListener(this);

		bRemoveJars = new Button(groupJars, SWT.NONE);
		bRemoveJars.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
		bRemoveJars.setImage(imgRemove);
		bRemoveJars.addSelectionListener(this);

		// keep a little space at the bottom
		new Composite(JavaCompilerGui.getContentComposite(), SWT.NONE).setLayoutData(new GridData(0, 40));

		// page settings
		updateData(); // will handle the next button
		JavaCompilerGui.getNextButton().addSelectionListener(this);
		JavaCompilerGui.getPreviousButton().addSelectionListener(this);
		JavaCompilerGui.setTitle("JavaCompiler v" + JavaCompilerGui.VERSION + " - 1/3: source");

		lTitle.addDisposeListener(this);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == JavaCompilerGui.getNextButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_SETTINGS);
		} else if(e.getSource() == JavaCompilerGui.getPreviousButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_CREATE_PROJECT);
		} else if(e.getSource() == bAddFiles)
		{
			FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN|SWT.MULTI);
			if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
			fileDialog.setText("add files");
			fileDialog.setFilterExtensions(new String[] { "*.java", "*.class" });
			fileDialog.setFilterNames(new String[] { "Java source files (*.java)", "Java class files (*.class)" });
			fileDialog.open();

			String[] sa = fileDialog.getFileNames();
			if(sa.length == 0) return;
			AppController.curDir = new File(fileDialog.getFilterPath());

			File[] fa = new File[sa.length];
			for(int i=0; i<sa.length; i++) fa[i] = new File(AppController.curDir, sa[i]);
			addSource(0, fa);
		} else if(e.getSource() == bAddDirectories)
		{
			DirectoryDialog dirDialog = new DirectoryDialog(Display.getCurrent().getActiveShell());
			if(AppController.curDir != null) dirDialog.setFilterPath(AppController.curDir.toString());
			dirDialog.setText("add directory");
			String ret = dirDialog.open();
			if(ret == null) return;

			File f = new File(ret);
			AppController.curDir = f;

			addSource(1, new File[] { f });
		} else if(e.getSource() == bAddJars)
		{
			FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN|SWT.MULTI);
			if(AppController.curDir != null) fileDialog.setFilterPath(AppController.curDir.toString());
			fileDialog.setText("add archives");
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
		} else if(e.getSource() == bRemoveDirectories)
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
		JavaCompilerGui.getNextButton().removeSelectionListener(this);
		JavaCompilerGui.getPreviousButton().removeSelectionListener(this);
	}


	// --------------- private methods ---------------

	private void updateData()
	{
		JavaCompilerProject project = AppController.getAppController().getCurrentProject();

		File[] faFiles = project.getFiles();
		for(int i=0; i<faFiles.length; i++) lFiles.add(faFiles[i].toString());

		File[] faDirs = project.getDirectories();
		for(int i=0; i<faDirs.length; i++) lDirs.add(faDirs[i].toString());

		File[] faJars = project.getJars();
		for(int i=0; i<faJars.length; i++) lJars.add(faJars[i].toString());

		JavaCompilerGui.getNextButton().setEnabled(lFiles.getItemCount() > 0 ||
				lDirs.getItemCount() > 0 || lJars.getItemCount() > 0);
	}

	/**
	 * mode = 0: files
	 * mode = 1: directories
	 * mode = 2: jars
	 */
	private void addSource(int mode, File[] files)
	{
		JavaCompilerProject project = AppController.getAppController().getCurrentProject();
		for(int i=0; i<files.length; i++)
		{
			if(mode == 0)
			{
				if(lFiles.indexOf(files[i].toString()) > -1) continue;

				project.addFile(files[i]);
				lFiles.add(files[i].toString());
			} else if(mode == 1)
			{
				if(lDirs.indexOf(files[i].toString()) > -1) continue;

				project.addDirectory(files[i]);
				lDirs.add(files[i].toString());
			} else if(mode == 2)
			{
				if(lJars.indexOf(files[i].toString()) > -1) continue;

				project.addJar(files[i]);
				lJars.add(files[i].toString());
			}
		}

		JavaCompilerGui.getNextButton().setEnabled(lFiles.getItemCount() > 0 ||
				lDirs.getItemCount() > 0 || lJars.getItemCount() > 0);
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
			int[] selIndices = lFiles.getSelectionIndices();
			for(int i=selIndices.length-1; i>=0; i--)
			{
				File f = new File(lFiles.getItem(selIndices[i]));
				AppController.getAppController().getCurrentProject().removeFile(f);
				lFiles.remove(selIndices[i]);
			}
		} else if(mode == 1)
		{
			int[] selIndices = lDirs.getSelectionIndices();
			for(int i=selIndices.length-1; i>=0; i--)
			{
				File f = new File(lDirs.getItem(selIndices[i]));
				AppController.getAppController().getCurrentProject().removeDirectory(f);
				lDirs.remove(selIndices[i]);
			}
		} else if(mode == 2)
		{
			int[] selIndices = lJars.getSelectionIndices();
			for(int i=selIndices.length-1; i>=0; i--)
			{
				File f = new File(lJars.getItem(selIndices[i]));
				AppController.getAppController().getCurrentProject().removeJar(f);
				lJars.remove(selIndices[i]);
			}
		}

		JavaCompilerGui.getNextButton().setEnabled(lFiles.getItemCount() > 0 ||
				lDirs.getItemCount() > 0 || lJars.getItemCount() > 0);
	}
}
