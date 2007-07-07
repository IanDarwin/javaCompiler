/*
 *   GcjStubber - A stub creator for GCJ (JNC).
 *   Copyright (C) 2007  Marco Trudel <mtrudel@gmx.ch>
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

package ch.mtSystems.gcjStubber.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import ch.mtSystems.gcjStubber.model.StubsGenerator;
import ch.mtSystems.gcjStubber.model.StubsGeneratorListener;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class ObjectStatisticsTab implements SelectionListener, StubsGeneratorListener
{
	private Table objectOverview;
	private Text objectView;
	private Label totalSavings;


	public ObjectStatisticsTab(TabFolder tabFolder)
	{
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Object Statistics");

		Composite parentComposite = new Composite(tabFolder, SWT.NONE);
		parentComposite.setLayout(LayoutUtilities.createGridLayout(3, 3));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabItem.setControl(parentComposite);
		
		// table
		objectOverview = new Table(parentComposite, SWT.BORDER);
		GridData tableGridData = LayoutUtilities.createGridData(GridData.FILL_VERTICAL, 2, 1);
		tableGridData.widthHint = 300;
		objectOverview.setLayoutData(tableGridData);
		objectOverview.setHeaderVisible(true);
		objectOverview.addSelectionListener(this);
		
		TableColumn objectColumn = new TableColumn(objectOverview, SWT.LEFT);
		objectColumn.setText("Object");
		objectColumn.setWidth(95);
		
		TableColumn handledColumn = new TableColumn(objectOverview, SWT.LEFT);
		handledColumn.setText("Handled In");
		handledColumn.setWidth(95);
		
		TableColumn savingsColumn = new TableColumn(objectOverview, SWT.RIGHT);
		savingsColumn.setText("Savings (bytes)");
		savingsColumn.setWidth(95);
		
		// object log
		objectView = new Text(parentComposite, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL|SWT.READ_ONLY);
		objectView.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_BOTH, 1, 2));
		
		// total savings
		(new Label(parentComposite, SWT.NONE)).setText("Savings Total:");
		totalSavings = new Label(parentComposite, SWT.RIGHT);
		totalSavings.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		
		StubsGenerator.getStubsGenerator().addListener(this);
	}

	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent event)
	{
		String text = (String)event.item.getData();
		objectView.setText((text != null) ? text : "");
	}

	public void widgetDefaultSelected(SelectionEvent event) { }


	// --------------- StubsGeneratorListener ---------------

	public void started()
	{
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				objectOverview.removeAll();
				objectView.setText("");
				
				totalSavings.setText("0");
				totalSavings.setData(0);
			}
		});
	}

	public void actionDone(String msg) { }

	public void processed(final String objectName, final int phaseProcessed, final int phaseResult,
			final String phaseResultMsg, final int savings, final int objectIndex, int totalCount)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				String objectColumnText = "(" + objectIndex + ") " + objectName;

				if(phaseProcessed == 0 || phaseProcessed == 1) // Skipped
				{
					TableItem tableItem = new TableItem(objectOverview, SWT.NONE);
					tableItem.setText(0, objectColumnText);
					tableItem.setText(1, "Skipped");
				} else if(phaseProcessed == 2 || phaseProcessed == 3 || phaseProcessed == 4) // Phase 1, 2 or 3
				{
					TableItem tableItem;
					if(phaseProcessed == 2)
					{
						tableItem = new TableItem(objectOverview, SWT.NONE);
						tableItem.setText(0, objectColumnText);
					} else
					{
						for(TableItem curItem : objectOverview.getItems())
						{
							if(curItem.getText(0).equals(objectColumnText))
							{
								tableItem = curItem;
								break;
							}
						}
						throw new IllegalArgumentException("TableItem \"" + objectColumnText + "\" not found!");
					}
					
					if(phaseResult == 0) // Ok
					{
						tableItem.setText(1, "Phase " + (phaseProcessed - 1));
						tableItem.setText(2, ""+savings);
						increaseSavings(savings);
					} else if(phaseResult == 1 || phaseResult == 2) // Failed | Problem
					{
						StringBuffer sb = new StringBuffer();
						if(phaseProcessed > 2)
						{
							sb.append((String)tableItem.getData());
							sb.append("\n\n");
						}
						sb.append("Phase ");
						sb.append(phaseProcessed - 1);
						sb.append("; ");
						sb.append((phaseResult == 1) ? "Failed" : "Problem");
						sb.append(":\n");
						sb.append(phaseResultMsg);

						tableItem.setData(sb.toString());
						
						if(phaseProcessed == 4) tableItem.setText(1, "Not Handled");
					}
				}
			}
		});
	}

	public void done() { }


	// --------------- private methods ---------------
	
	private void increaseSavings(int bytes)
	{
		int cur = ((Integer)totalSavings.getData()) + bytes;
		totalSavings.setData(cur);

		if(cur < 1024)
		{
			totalSavings.setText(cur + "bytes");
			return;
		}

		cur /= 1024;
		if(cur < 1024)
		{
			totalSavings.setText(cur + "kb");
			return;
		}

		cur /= 1024;
		totalSavings.setText(cur + "mb");
	}
}
