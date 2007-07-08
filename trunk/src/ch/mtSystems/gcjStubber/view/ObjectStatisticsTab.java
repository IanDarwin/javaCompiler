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
import ch.mtSystems.gcjStubber.view.utilities.FixedSash;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class ObjectStatisticsTab implements SelectionListener, StubsGeneratorListener
{
	private Table objectsTable;
	private Text logText;
	private Label savingsLabel;


	public ObjectStatisticsTab(TabFolder tabFolder)
	{
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Object Statistics");

		Composite parentComposite = new Composite(tabFolder, SWT.NONE);
		parentComposite.setLayout(LayoutUtilities.createGridLayout(2, 3));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabItem.setControl(parentComposite);
		
		// Sash
		Composite leftComposite = new Composite(parentComposite, SWT.NONE);
		leftComposite.setLayout(LayoutUtilities.createGridLayout(2, 3));
		leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite rightComposite = new Composite(parentComposite, SWT.NONE);
		rightComposite.setLayout(LayoutUtilities.createGridLayout(1, 3));
		rightComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		new FixedSash(leftComposite, rightComposite, parentComposite, true, 300);
		
		// table
		objectsTable = new Table(leftComposite, SWT.BORDER|SWT.FULL_SELECTION);
		objectsTable.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_BOTH, 2, 1));
		objectsTable.setHeaderVisible(true);
		objectsTable.addSelectionListener(this);
		
		TableColumn objectColumn = new TableColumn(objectsTable, SWT.LEFT);
		objectColumn.setText("Object");
		objectColumn.pack();
		objectColumn.addSelectionListener(this);
		
		TableColumn handledColumn = new TableColumn(objectsTable, SWT.LEFT);
		handledColumn.setText("Handled In");
		handledColumn.pack();
		handledColumn.addSelectionListener(this);
		
		TableColumn savingsColumn = new TableColumn(objectsTable, SWT.RIGHT);
		savingsColumn.setText("Savings (bytes)");
		savingsColumn.pack();
		savingsColumn.addSelectionListener(this);

		// object log
		logText = new Text(rightComposite, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.H_SCROLL|SWT.READ_ONLY);
		logText.setLayoutData(LayoutUtilities.createGridData(GridData.FILL_BOTH, 1, 2));

		// total savings
		(new Label(leftComposite, SWT.NONE)).setText("Savings Total:");
		savingsLabel = new Label(leftComposite, SWT.RIGHT);
		savingsLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		StubsGenerator.getStubsGenerator().addListener(this);
	}

	// --------------- SelectionListener ---------------

	public void widgetSelected(SelectionEvent event)
	{
		if(event.getSource() == objectsTable)
		{
			String text = (String)event.item.getData();
			logText.setText((text != null) ? text : "");
		} else if(event.getSource() instanceof TableColumn)
		{
			orderBy(objectsTable.indexOf((TableColumn)event.getSource()));
		}
	}

	public void widgetDefaultSelected(SelectionEvent event) { }


	// --------------- StubsGeneratorListener ---------------

	public void started()
	{
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				objectsTable.removeAll();
				logText.setText("");
				
				savingsLabel.setText("0");
				savingsLabel.setData(0);
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
					TableItem tableItem = new TableItem(objectsTable, SWT.NONE);
					tableItem.setText(0, objectColumnText);
					tableItem.setText(1, "Skipped");
				} else if(phaseProcessed == 2 || phaseProcessed == 3 || phaseProcessed == 4) // Phase 1, 2 or 3
				{
					TableItem tableItem = null;
					if(phaseProcessed == 2)
					{
						tableItem = new TableItem(objectsTable, SWT.NONE);
						tableItem.setText(0, objectColumnText);
					} else
					{
						for(TableItem curItem : objectsTable.getItems())
						{
							if(curItem.getText(0).equals(objectColumnText))
							{
								tableItem = curItem;
								break;
							}
						}
						if(tableItem == null) throw new IllegalArgumentException("TableItem \"" + objectColumnText + "\" not found!");
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
		int cur = ((Integer)savingsLabel.getData()) + bytes;
		savingsLabel.setData(cur);

		if(cur < 1024)
		{
			savingsLabel.setText(cur + "bytes");
			return;
		}

		cur /= 1024;
		if(cur < 1024)
		{
			savingsLabel.setText(cur + "kb");
			return;
		}

		cur /= 1024;
		savingsLabel.setText(cur + "mb");
	}
	
	private void orderBy(int columnIndex)
	{
		int n = objectsTable.getItemCount();

		for(int i=0; i < n-1; i++)
		{
			for(int j=n-1; j > i; j--)
			{
				TableItem cur = objectsTable.getItem(j-1);
				TableItem next = objectsTable.getItem(j);
				
				int curIndex = getIndex(cur.getText(0));
				int nextIndex = getIndex(next.getText(0));
	
				if(columnIndex == 0)
				{
					if(curIndex > nextIndex) swap(cur, next);
				} else if(columnIndex == 1)
				{
					int comp = cur.getText(1).compareTo(next.getText(1));
					if(comp > 0 || (comp == 0 && curIndex > nextIndex)) swap(cur, next);
				} else if(columnIndex == 2)
				{
					int curSize = (cur.getText(2).length() > 0) ? Integer.parseInt(cur.getText(2)) : Integer.MIN_VALUE;
					int nextSize = (next.getText(2).length() > 0) ? Integer.parseInt(next.getText(2)) : Integer.MIN_VALUE;
					if(curSize < nextSize || (curSize == nextSize && curIndex > nextIndex)) swap(cur, next);
				}
			}
		}
	}
	
	private int getIndex(String objectColumnText)
	{
		return Integer.parseInt(objectColumnText.substring(1, objectColumnText.indexOf(')')));
	}
	
	private void swap(TableItem a, TableItem b)
	{
		String aText0 = a.getText(0);
		String aText1 = a.getText(1);
		String aText2 = a.getText(2);
		Object aData = a.getData();
		
		a.setText(0, b.getText(0));
		a.setText(1, b.getText(1));
		a.setText(2, b.getText(2));
		a.setData(b.getData());
		
		b.setText(0, aText0);
		b.setText(1, aText1);
		b.setText(2, aText2);
		b.setData(aData);
	}
}
