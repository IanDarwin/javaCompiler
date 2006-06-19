package ch.mtSystems.javaCompiler.view.pages;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.mtSystems.javaCompiler.control.AppController;
import ch.mtSystems.javaCompiler.model.utilities.FileUtilities;
import ch.mtSystems.javaCompiler.model.utilities.GuiSettingsMemory;
import ch.mtSystems.javaCompiler.view.JavaCompilerGui;
import ch.mtSystems.javaCompiler.view.utilities.LayoutUtilities;


public class IntroductionPage implements SelectionListener, DisposeListener
{
	private static Image imgLogo = new Image(Display.getCurrent(), "ressources/logo.jpg");


	private Button bSkipIntro;


	public IntroductionPage()
	{
		Composite introductionComposite = new Composite(JavaCompilerGui.getContentComposite(), SWT.NONE);
		introductionComposite.setLayout(LayoutUtilities.createGridLayout(2, 0));
		introductionComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		(new Label(introductionComposite, SWT.NONE)).setImage(imgLogo);

		final Composite textComposite = new Composite(introductionComposite, SWT.NONE);
		textComposite.setLayout(LayoutUtilities.createGridLayout(2, 0));
		textComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lTitle = new Label(textComposite, SWT.NONE);
		lTitle.setLayoutData(LayoutUtilities.createGridData(-1, 2, 1, -1, -1));
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getCurrent(), fd));
		lTitle.setText("JavaCompiler");

		Label lMotto = new Label(textComposite, SWT.WRAP);
		final GridData data = LayoutUtilities.createGridData(-1, 2, 1, -1, -1);
		lMotto.setLayoutData(data);
		lMotto.setText("Develop with Java 1.5, deploy native executables for windows, linux and mac!");

		textComposite.addControlListener(new ControlAdapter()
			{
				public void controlResized(ControlEvent e)
				{
					data.widthHint = textComposite.getClientArea().width;
					textComposite.getParent().layout(true);
				}
			});

		addText(textComposite, "version:", 5);
		addText(textComposite, JavaCompilerGui.VERSION, 5);
		addText(textComposite, "homepage:", -1);
		addText(textComposite, "http://javaCompiler.sourceforge.net", -1);
		addText(textComposite, "programmer:", -1);
		addText(textComposite, "Marco Trudel (mtrudel@gmx.ch)", -1);

		Text introText = new Text(introductionComposite, SWT.MULTI|SWT.BORDER|SWT.READ_ONLY|SWT.WRAP|SWT.V_SCROLL);
		introText.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_BOTH, 2, 1, -1, -1));
		introText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		introText.setText(FileUtilities.readTextFile(new File("ressources/description.txt")));

		bSkipIntro = new Button(introductionComposite, SWT.CHECK);
		bSkipIntro.setLayoutData(LayoutUtilities.createGridData(-1, 2, 1, -1, -1));
		bSkipIntro.setText("skip this Intro page when starting JavaCompiler");
		bSkipIntro.setSelection(GuiSettingsMemory.getSettingsMemory().skipIntro());
		bSkipIntro.addSelectionListener(this);

		// page settings
		JavaCompilerGui.getNextButton().setEnabled(true);
		JavaCompilerGui.getNextButton().addSelectionListener(this);
		JavaCompilerGui.getPreviousButton().setVisible(false);
		JavaCompilerGui.setTitle("JavaCompiler v" + JavaCompilerGui.VERSION + " - Welcome");

		lTitle.addDisposeListener(this);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == bSkipIntro)
		{
			GuiSettingsMemory.getSettingsMemory().setSkipIntro(bSkipIntro.getSelection());
		} else if(e.getSource() == JavaCompilerGui.getNextButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_CREATE_PROJECT);
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

	private void addText(Composite introductionComposite, String text, int verticalIndent)
	{
		Label l = new Label(introductionComposite, SWT.NONE);
		if(verticalIndent > -1) l.setLayoutData(LayoutUtilities.createGridData(-1, -1, -1, verticalIndent, -1));
		l.setText(text);
	}
}
