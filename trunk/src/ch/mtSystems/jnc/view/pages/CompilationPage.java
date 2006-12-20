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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.mtSystems.jnc.control.AppController;
import ch.mtSystems.jnc.model.ICompilationProgressLogger;
import ch.mtSystems.jnc.model.NativeCompiler;
import ch.mtSystems.jnc.view.JNC;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class CompilationPage extends WizzardPage implements SelectionListener, DisposeListener, ICompilationProgressLogger
{
	private Text tLog;
	private Button bBeep, bCompile;


	public CompilationPage()
	{
		Label lTitle = new Label(JNC.getContentComposite(), SWT.NONE);
		FontData fd = lTitle.getFont().getFontData()[0];
		fd.setHeight(fd.getHeight()*2);
		lTitle.setFont(new Font(Display.getDefault(), fd));
		lTitle.setText("Step 4 of 4: Compilation");

		tLog = new Text(JNC.getContentComposite(), SWT.BORDER|SWT.READ_ONLY|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL);
		tLog.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		tLog.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite tmpComposite = new Composite(JNC.getContentComposite(), SWT.NONE);
		tmpComposite.setLayout(LayoutUtilities.createGridLayout(2, 0));
		tmpComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		bBeep = new Button(tmpComposite, SWT.CHECK);
		bBeep.setSelection(AppController.getAppController().getCurrentProject().getBeepWhenDone());
		bBeep.setText("Beep when done");
		bBeep.addSelectionListener(this);

		bCompile = new Button(tmpComposite, SWT.NONE);
		bCompile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.HORIZONTAL_ALIGN_END));
		bCompile.setText("Compile");
		bCompile.addSelectionListener(this);

		// keep a little space at the bottom
		new Composite(JNC.getContentComposite(), SWT.NONE).setLayoutData(new GridData(0, 40));

		// page settings
		JNC.getNextButton().setVisible(false);
		JNC.getPreviousButton().addSelectionListener(this);
		lTitle.addDisposeListener(this);
	}


	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent e)
	{
		if(e.getSource() == JNC.getPreviousButton())
		{
			AppController.getAppController().loadPage(AppController.PAGE_ADVANCED_SETTINGS);
		} else if(e.getSource() == bBeep)
		{
			AppController.getAppController().getCurrentProject().
					setBeepWhenDone(bBeep.getSelection());
		} else if(e.getSource() == bCompile)
		{
			compile(Display.getDefault());
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) { }


	// --------------- DisposeListener ---------------

	public void widgetDisposed(DisposeEvent e)
	{
		JNC.getNextButton().removeSelectionListener(this);
		JNC.getPreviousButton().removeSelectionListener(this);
	}


	// --------------- CompilationProgressLogger ---------------

	public void log(final String s, final boolean indent)
	{
		tLog.getDisplay().syncExec(new Runnable()
				{
					public void run()
					{
						if(tLog.getCharCount() > 0) tLog.append("\n");
						if(indent) tLog.append("\t");
						tLog.append(s);
					}
				});
	}

	// --------------- private methods ---------------

	private void compile(final Display d)
	{
		final NativeCompiler jc = new NativeCompiler(this, AppController.getAppController().getCurrentProject());

		tLog.setText("");
		JNC.getPreviousButton().setEnabled(false);
		bCompile.setEnabled(false);

		new Thread()
		{
			public void run()
			{
				try
				{
					if(jc.compile()) syncLog("\n\ndone", d);
					else             syncLog("\n\nfailed...", d);
				} catch(Exception ex)
				{
					ex.printStackTrace();
					syncLog("\n\nfailed:\n", d);
					syncLog(ex.toString(), d);
				}

				d.syncExec(new Runnable()
						{
							public void run()
							{
								bCompile.setEnabled(true);
								JNC.getPreviousButton().setEnabled(true);
							}
						});
			}
		}.start();
	}

	private void syncLog(final String s, Display d)
	{
		d.syncExec(new Runnable()
				{
					public void run() { tLog.append(s); }
				});
	}
}
