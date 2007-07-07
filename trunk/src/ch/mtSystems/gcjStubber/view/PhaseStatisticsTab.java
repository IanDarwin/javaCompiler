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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import ch.mtSystems.gcjStubber.model.StubsGenerator;
import ch.mtSystems.gcjStubber.model.StubsGeneratorListener;
import ch.mtSystems.jnc.view.utilities.LayoutUtilities;


public class PhaseStatisticsTab implements StubsGeneratorListener
{
	private Label phaseStatsNoJavaClasses, phaseStatsNotPulledIn;
	private Label phaseStats1ok, phaseStats1failed, phaseStats1problems;
	private Label phaseStats2ok, phaseStats2failed, phaseStats2problems;
	private Label phaseStats3ok, phaseStats3failed, phaseStats3problems;


	public PhaseStatisticsTab(TabFolder tabFolder)
	{
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Phase Statistics");

		Composite parentComposite = new Composite(tabFolder, SWT.NONE);
		parentComposite.setLayout(LayoutUtilities.createGridLayout(2, 3));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabItem.setControl(parentComposite);
		
		GridData gdWidth = new GridData();
		gdWidth.widthHint = 50;
		
		// Skipped
		Label skipped = new Label(parentComposite, SWT.NONE);
		skipped.setText("Skipped");
		skipped.setLayoutData(LayoutUtilities.createGridData(2, 1));

		new Label(parentComposite, SWT.NONE).setText("- No Java Classes:");
		phaseStatsNoJavaClasses = new Label(parentComposite, SWT.RIGHT);
		phaseStatsNoJavaClasses.setLayoutData(gdWidth);

		new Label(parentComposite, SWT.NONE).setText("- Won't be pulled in:");
		phaseStatsNotPulledIn = new Label(parentComposite, SWT.RIGHT);
		phaseStatsNotPulledIn.setLayoutData(gdWidth);
		
		// Phase 1
		Label phase1 = new Label(parentComposite, SWT.NONE);
		phase1.setText("Phase 1");
		phase1.setLayoutData(LayoutUtilities.createGridData(SWT.NONE, 2, 1, 12, 0));

		new Label(parentComposite, SWT.NONE).setText("- Ok:");
		phaseStats1ok = new Label(parentComposite, SWT.RIGHT);
		phaseStats1ok.setLayoutData(gdWidth);
		new Label(parentComposite, SWT.NONE).setText("- Failed:");
		phaseStats1failed = new Label(parentComposite, SWT.RIGHT);
		phaseStats1failed.setLayoutData(gdWidth);
		new Label(parentComposite, SWT.NONE).setText("- Problems:");
		phaseStats1problems = new Label(parentComposite, SWT.RIGHT);
		phaseStats1problems.setLayoutData(gdWidth);

		// Phase 2
		Label phase2 = new Label(parentComposite, SWT.NONE);
		phase2.setText("Phase 2");
		phase2.setLayoutData(LayoutUtilities.createGridData(SWT.NONE, 2, 1, 12, 0));

		new Label(parentComposite, SWT.NONE).setText("- Ok:");
		phaseStats2ok = new Label(parentComposite, SWT.RIGHT);
		phaseStats2ok.setLayoutData(gdWidth);
		new Label(parentComposite, SWT.NONE).setText("- Failed:");
		phaseStats2failed = new Label(parentComposite, SWT.RIGHT);
		phaseStats2failed.setLayoutData(gdWidth);
		new Label(parentComposite, SWT.NONE).setText("- Problems:");
		phaseStats2problems = new Label(parentComposite, SWT.RIGHT);
		phaseStats2problems.setLayoutData(gdWidth);

		// Phase 3
		Label phase3 = new Label(parentComposite, SWT.NONE);
		phase3.setText("Phase 3");
		phase3.setLayoutData(LayoutUtilities.createGridData(SWT.NONE, 2, 1, 12, 0));

		new Label(parentComposite, SWT.NONE).setText("- Ok:");
		phaseStats3ok = new Label(parentComposite, SWT.RIGHT);
		phaseStats3ok.setLayoutData(gdWidth);
		new Label(parentComposite, SWT.NONE).setText("- Failed:");
		phaseStats3failed = new Label(parentComposite, SWT.RIGHT);
		phaseStats3failed.setLayoutData(gdWidth);
		new Label(parentComposite, SWT.NONE).setText("- Problems:");
		phaseStats3problems = new Label(parentComposite, SWT.RIGHT);
		phaseStats3problems.setLayoutData(gdWidth);
		
		StubsGenerator.getStubsGenerator().addListener(this);
	}


	// --------------- StubsGeneratorListener ---------------

	public void started()
	{		Display.getDefault().syncExec(new Runnable()
	{
		public void run()
		{
			setCount(phaseStatsNoJavaClasses, 0);
			setCount(phaseStatsNotPulledIn, 0);
			setCount(phaseStats1ok, 0);
			setCount(phaseStats1failed, 0);
			setCount(phaseStats1problems, 0);
			setCount(phaseStats2ok, 0);
			setCount(phaseStats2failed, 0);
			setCount(phaseStats2problems, 0);
			setCount(phaseStats3ok, 0);
			setCount(phaseStats3failed, 0);
			setCount(phaseStats3problems, 0);
		}
	});
	}

	public void actionDone(String msg) { }

	public void processed(String objectName, final int phaseProcessed, final int phaseResult,
			String phaseResultMsg, int objectIndex, int totalCount)
	{
		Display.getDefault().syncExec(new Runnable()
		{
			public void run()
			{
				if(phaseProcessed == 0) // Skipped (No Java Classes)
				{
					increment(phaseStatsNoJavaClasses);
				} else if(phaseProcessed == 1) // Skipped (Won't be pulled in)
				{
					increment(phaseStatsNotPulledIn);
				} else if(phaseProcessed == 2) // Phase 1
				{
					     if(phaseResult == 0) increment(phaseStats1ok);
					else if(phaseResult == 1) increment(phaseStats1failed);
					else if(phaseResult == 2) increment(phaseStats1problems);
				} else if(phaseProcessed == 3) // Phase 2
				{
				         if(phaseResult == 0) increment(phaseStats2ok);
					else if(phaseResult == 1) increment(phaseStats2failed);
					else if(phaseResult == 2) increment(phaseStats2problems);
				} else if(phaseProcessed == 4) // Phase 3
				{
				         if(phaseResult == 0) increment(phaseStats3ok);
					else if(phaseResult == 1) increment(phaseStats3failed);
					else if(phaseResult == 2) increment(phaseStats3problems);
				}				
			}
		});
	}

	public void done() { }


	// --------------- private methods ---------------
	
	private void setCount(Label l, int count)
	{
		l.setData(count);
		l.setText(""+count);
	}
	
	private void increment(Label l)
	{
		int count = (Integer)l.getData();
		setCount(l, count+1);
	}
}
