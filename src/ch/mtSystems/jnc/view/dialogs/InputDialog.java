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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


/**
 * A simple dialog to get some input from the user.
 */
public class InputDialog extends Dialog implements SelectionListener, ModifyListener
{
	private Shell shell;
	private Button bOk, bCancel;
	private Text inputText;

	private String title, message, initialText, ret;


	/**
	 * Creates the dialog.
	 * 
	 * @param parent The parent shell.
	 */
	public InputDialog(Shell parent)
	{
		super(parent);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == bOk) ret = inputText.getText();
		shell.dispose();
	}

	public void widgetDefaultSelected(SelectionEvent e)
	{
		if(inputText.getCharCount() > 0)
		{
			ret = inputText.getText();
			shell.dispose();
		}
	}


	// --------------- ModifyListener ---------------

	public void modifyText(ModifyEvent e)
	{
		bOk.setEnabled(inputText.getCharCount() > 0);
	}


	// --------------- public methods ---------------

	/**
	 * Sets the title of the dialog. Has to be called before open().
	 * 
	 * @param title The title of the dialog.
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * Sets the message for the user. Has to be called before open().
	 * 
	 * @param message The message for the user.
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}

	/**
	 * Sets an initial selection for the user. Has to be called before open().
	 * 
	 * @param initialText The initial text, so that the user might just click OK.
	 */
	public void setInitialText(String initialText)
	{
		this.initialText = initialText;
	}

	/**
	 * Opens the dialog.
	 */
	public String open()
	{
		shell = new Shell(getParent(), SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
		if(title != null) shell.setText(title);
		shell.setLayout(LayoutUtilities.createGridLayout(1, 3));

		Label lMessage = new Label(shell, SWT.NONE);
		if(message != null) lMessage.setText(message);

		inputText = new Text(shell, SWT.BORDER);
		inputText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputText.addSelectionListener(this);
		inputText.addModifyListener(this);

		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		GridLayout gl = LayoutUtilities.createGridLayout(2, 0);
		gl.horizontalSpacing = 15;
		buttonComposite.setLayout(gl);

		bOk = new Button(buttonComposite, SWT.NONE);
		bOk.setText("OK");
		bOk.addSelectionListener(this);

		bCancel = new Button(buttonComposite, SWT.NONE);
		bCancel.setText("Cancel");
		bCancel.addSelectionListener(this);

		if(initialText != null)
		{
			inputText.setText(initialText);
			inputText.selectAll();
		} else
		{
			bOk.setEnabled(false);
		}

		shell.pack();
		shell.open();

		Display d = Display.getDefault();
		while(!shell.isDisposed())
		{
			if(!d.readAndDispatch()) d.sleep();
		}
		d.update();

		return ret;
	}
}
