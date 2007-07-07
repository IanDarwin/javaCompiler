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


public class ObjectStatisticsTab implements StubsGeneratorListener
{
	public ObjectStatisticsTab(TabFolder tabFolder)
	{
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Object Statistics");

		Composite parentComposite = new Composite(tabFolder, SWT.NONE);
		parentComposite.setLayout(LayoutUtilities.createGridLayout(2, 3));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabItem.setControl(parentComposite);
		
		StubsGenerator.getStubsGenerator().addListener(this);
	}


	// --------------- StubsGeneratorListener ---------------

	public void started()
	{
		// TODO Auto-generated method stub
	}

	public void actionDone(String msg)
	{
		// TODO Auto-generated method stub
	}

	public void processed(String objectName, int phaseProcessed, int phaseResult,
			String phaseResultMsg, int objectIndex, int totalCount)
	{
		// TODO Auto-generated method stub
	}


	public void done()
	{
		// TODO Auto-generated method stub
	}
}
