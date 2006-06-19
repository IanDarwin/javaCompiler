package ch.mtSystems.javaCompiler.view;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.model.utilities.FileUtilities;
import ch.mtSystems.javaCompiler.model.utilities.GuiSettingsMemory;
import ch.mtSystems.javaCompiler.view.dialogs.SettingsDialog;
import ch.mtSystems.javaCompiler.view.utilities.LayoutUtilities;


public class JavaCompilerGui implements SelectionListener
{
	public final static String VERSION = "0.7a";

	private static Shell shell;
	private SashForm sash;
	private static Composite contentComposite;
	private static Button bPrevious, bNext, bHelp;
	private static Text tHelp;

	private ToolItem tiSettings, tiSave;
	private Menu popupMenu;
	private MenuItem miSave, miSaveAs;


	public JavaCompilerGui()
	{
		shell = new Shell(new Display());
		shell.setLayout(new GridLayout());

		sash = new SashForm(shell, SWT.VERTICAL);
		sash.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cSashNorth = new Composite(sash, SWT.NONE);
		cSashNorth.setLayout(LayoutUtilities.createGridLayout(1, 0));
		cSashNorth.setLayoutData(new GridData(GridData.FILL_BOTH));

		contentComposite = new Composite(cSashNorth, SWT.NONE);
		contentComposite.setLayout(LayoutUtilities.createGridLayout(1, 0));
		contentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		new Label(cSashNorth, SWT.SEPARATOR|SWT.HORIZONTAL).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createButtonComposite(cSashNorth);
	}


	// --------------- public static methods ---------------

	public static void setTitle(String title)
	{
		shell.setText(title);
	}

	public static void updateHelpPage(int page)
	{
		if(tHelp == null) return;

		if(page == AppController.PAGE_INTRODUCTION)
		{
			tHelp.setText(FileUtilities.readTextFile(new File("ressources/helpIntroPage.txt")));
		} else if(page == AppController.PAGE_CREATE_PROJECT)
		{
			tHelp.setText(FileUtilities.readTextFile(new File("ressources/helpCreateProjectPage.txt")));
		} else if(page == AppController.PAGE_SOURCE)
		{
			tHelp.setText(FileUtilities.readTextFile(new File("ressources/helpSourcePage.txt")));
		} else if(page == AppController.PAGE_SETTINGS)
		{
			tHelp.setText(FileUtilities.readTextFile(new File("ressources/helpSettingsPage.txt")));
		} else if(page == AppController.PAGE_COMPILATION)
		{
			tHelp.setText(FileUtilities.readTextFile(new File("ressources/helpCompilePage.txt")));
		}
	}

	public static Composite getContentComposite() { return contentComposite; }
	public static Button getPreviousButton() { return bPrevious; }
	public static Button getNextButton() { return bNext; }


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == tiSettings)
		{
			(new SettingsDialog()).open();
		} else if(e.getSource() == tiSave)
		{
			if(e.detail == SWT.ARROW)
			{
				Rectangle rect = tiSave.getBounds();
				Point pt = new Point(rect.x, rect.y + rect.height);
				pt = tiSave.getParent().toDisplay(pt);

				popupMenu.setLocation(pt.x, pt.y);
				popupMenu.setVisible(true);
			} else
			{
				saveProject();
			}
		} else if(e.getSource() == miSave)
		{
			saveProject();
		} else if(e.getSource() == miSaveAs)
		{
			saveProjectAs();
		} else if(e.getSource() == bHelp)
		{
			if(tHelp == null)
			{
				tHelp = new Text(sash, SWT.MULTI|SWT.BORDER|SWT.READ_ONLY|SWT.WRAP|SWT.V_SCROLL);
				tHelp.setLayoutData(new GridData(GridData.FILL_BOTH));
				tHelp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
				bHelp.setText("hide help");
				updateHelpPage(AppController.getAppController().getCurrentPage());

				shell.setSize(500, 650);
				sash.setWeights(new int[] { 466, 147 });
			} else
			{
				tHelp.dispose();
				tHelp = null;
				bHelp.setText("show help");
				shell.setSize(500, 500);
			}

			sash.layout();
		}
	}


	public void widgetDefaultSelected(SelectionEvent e) { }


	// --------------- private methods ---------------


	private void createButtonComposite(Composite parent)
	{
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(LayoutUtilities.createGridLayout(5, 0));
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ToolBar toolBar = new ToolBar(buttonComposite, SWT.FLAT);

		tiSettings = new ToolItem(toolBar, SWT.NONE);
		tiSettings.setImage(new Image(Display.getCurrent(), "ressources/settings.png"));
		tiSettings.setToolTipText("settings");
		tiSettings.addSelectionListener(this);

		new ToolItem(toolBar, SWT.SEPARATOR);

		tiSave = new ToolItem(toolBar, SWT.DROP_DOWN);
		tiSave.setImage(new Image(Display.getCurrent(), "ressources/save.png"));
		tiSave.setToolTipText("save");
		tiSave.addSelectionListener(this);

		popupMenu = new Menu(toolBar.getShell(), SWT.POP_UP);
		miSave = new MenuItem(popupMenu, SWT.PUSH);
		miSave.setText("save");
		miSave.addSelectionListener(this);
		miSaveAs = new MenuItem(popupMenu, SWT.PUSH);
		miSaveAs.setText("save as...");
		miSaveAs.addSelectionListener(this);

		Label lFileName = new Label(buttonComposite, SWT.NONE);
		lFileName.setText("not yet saved");

		GridData gdSettings = new GridData();
		gdSettings.grabExcessHorizontalSpace = true;
		lFileName.setLayoutData(gdSettings);

		bPrevious = new Button(buttonComposite, SWT.NONE);
		bPrevious.setText("< previous");

		bNext = new Button(buttonComposite, SWT.NONE);
		bNext.setText("next >");

		bHelp = new Button(buttonComposite, SWT.NONE);
		bHelp.setText("show help");
		bHelp.addSelectionListener(this);
	}

	private void saveProject()
	{
		System.out.println("save");
	}

	private void saveProjectAs()
	{
		System.out.println("save as");
	}


	// --------------- our mighty main paw ---------------

	public static void main(String[] args)
	{
		new JavaCompilerGui();
		shell.setSize(500, 500);
		shell.open();

		// swt bug; needs to be called after open. if earlier, radiobuttons are magically selected
		AppController.getAppController().loadPage(GuiSettingsMemory.getSettingsMemory().skipIntro() ?
				AppController.PAGE_CREATE_PROJECT : AppController.PAGE_INTRODUCTION);

		while(!shell.isDisposed())
		{
			if(!shell.getDisplay().readAndDispatch()) shell.getDisplay().sleep();
		}
	}
}
