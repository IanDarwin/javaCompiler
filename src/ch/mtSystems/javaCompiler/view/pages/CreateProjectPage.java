package ch.mtSystems.javaCompiler.view.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.model.projects.AwtSwingProject;
import ch.mtSystems.javaCompiler.model.projects.ConsoleApplicationProject;
import ch.mtSystems.javaCompiler.model.projects.JFaceProject;
import ch.mtSystems.javaCompiler.model.projects.SwtProject;
import ch.mtSystems.javaCompiler.view.JavaCompilerGui;
import ch.mtSystems.javaCompiler.view.utilities.LayoutUtilities;


public class CreateProjectPage implements SelectionListener, DisposeListener
{
	private Button bConsoleApp, bSwtApp, bJFaceApp, bAwtSwingApp, bKeepCurrent;

	public CreateProjectPage()
	{
		Label lTitle = new Label(JavaCompilerGui.getContentComposite(), SWT.NONE);
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getCurrent(), fd));
		lTitle.setText("Start: create a project");

		bConsoleApp = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
		bConsoleApp.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, 10, 15));
		bConsoleApp.setText("unmanaged application");
		bConsoleApp.addSelectionListener(this);

		bSwtApp = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
		bSwtApp.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
		bSwtApp.setText("managed SWT application");
		bSwtApp.addSelectionListener(this);

		bJFaceApp = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
		bJFaceApp.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
		bJFaceApp.setText("managed JFace application");
		bJFaceApp.addSelectionListener(this);

		bAwtSwingApp = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
		bAwtSwingApp.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
		bAwtSwingApp.setText("managed AWT or Swing application");
		bAwtSwingApp.addSelectionListener(this);

		if(AppController.getAppController().getCurrentProject() != null)
		{
			bKeepCurrent = new Button(JavaCompilerGui.getContentComposite(), SWT.RADIO);
			bKeepCurrent.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, -1, 15));
			bKeepCurrent.setText("keep current project");
			bKeepCurrent.setSelection(true);
		}

		// page settings
		JavaCompilerGui.getNextButton().setEnabled(AppController.getAppController().getCurrentProject() != null);
		JavaCompilerGui.getNextButton().addSelectionListener(this);
		JavaCompilerGui.getPreviousButton().setVisible(true);
		JavaCompilerGui.getPreviousButton().addSelectionListener(this);
		JavaCompilerGui.setTitle("JavaCompiler v" + JavaCompilerGui.VERSION + " - create a project");

		lTitle.addDisposeListener(this);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == JavaCompilerGui.getNextButton())
		{
			AppController ap = AppController.getAppController();

			if(ap.getCurrentProject() != null &&
			   (bConsoleApp.getSelection() == true || bSwtApp.getSelection() == true ||
				bJFaceApp.getSelection() == true   || bAwtSwingApp.getSelection() == true))
			{
				String title = "discard current project?";
				String msg = "You have currently another project that will be discarded.\nContinue?";

				MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_QUESTION|SWT.YES|SWT.NO);
				messageBox.setText(title);
				messageBox.setMessage(msg);
				if(messageBox.open() != SWT.YES) return;
			}

			if(bConsoleApp.getSelection() == true)
			{
				ap.setCurrentProject(new ConsoleApplicationProject());
			} else if(bSwtApp.getSelection() == true)
			{
				ap.setCurrentProject(new SwtProject());
			} else if(bJFaceApp.getSelection() == true)
			{
				ap.setCurrentProject(new JFaceProject());
			} else if(bAwtSwingApp.getSelection() == true)
			{
				ap.setCurrentProject(new AwtSwingProject());
			}

			ap.loadPage(AppController.PAGE_SOURCE);
		} else if(e.getSource() == JavaCompilerGui.getPreviousButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_INTRODUCTION);
		} else // a radiobutton has been selected
		{
			JavaCompilerGui.getNextButton().setEnabled(true);
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
