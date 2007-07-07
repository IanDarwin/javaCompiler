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

package ch.mtSystems.gcjStubber.view.utilities;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;


/**
 * This class provides a sash-view that splits the parent into a left and right
 * side and let only one of the sides grow when the window is resized.
 */
public class FixedSash implements Listener, MouseListener
{
	private final static int limit = 50;


	private Composite parent;
	private Control leftControl, sash, rightControl;
	private FormData fdLeft, fdSash, fdRight;
	private boolean fixLeftWidth;
	
	private Vector<ControlListener> vListeners = new Vector<ControlListener>();
	private int lastFixedControlWidth;


	/**
	 * Creates the sash-view in the parent with the given arguments.
	 * 
	 * @param leftControl - The control that will be positioned on the left side.
	 * @param rightControl - The control that will be positioned on the right side.
	 * @param parent - The parent of the sash-view.
	 * @param fixLeftControl - Wether to fix the left control or the right control.
	 * @param initialWidth - The initial width of the fixed control.
	 */
	public FixedSash(Control leftControl, Control rightControl,
			Composite parent, boolean fixLeftControl, int initialWidth)
	{
		this.leftControl = leftControl;
		this.rightControl = rightControl;
		this.parent = parent;
		this.fixLeftWidth = fixLeftControl;

		sash = new Sash(parent, SWT.VERTICAL);
		parent.setLayout(new FormLayout());

		fdLeft = new FormData();
		fdLeft.left = new FormAttachment(0, 0);
		fdLeft.right = new FormAttachment(sash, 0);
		fdLeft.top = new FormAttachment(0, 0);
		fdLeft.bottom = new FormAttachment(100, 0);
		leftControl.setLayoutData(fdLeft);

		fdSash = new FormData();

		if(fixLeftControl) fdSash.left = new FormAttachment(0, initialWidth);
		else               fdSash.right = new FormAttachment(100, -initialWidth);

		fdSash.top = new FormAttachment(0, 0);
		fdSash.bottom = new FormAttachment(100, 0);
		sash.setLayoutData(fdSash);

		fdRight = new FormData();
		fdRight.left = new FormAttachment(sash, 0);
		fdRight.right = new FormAttachment(100, 0);
		fdRight.top = new FormAttachment(0, 0);
		fdRight.bottom = new FormAttachment(100, 0);
		rightControl.setLayoutData(fdRight);

		sash.addListener(SWT.Selection, this);
		sash.addMouseListener(this);
	}


	// --------------- Listener ---------------

	public void handleEvent(Event e)
	{
		Rectangle sashRect = sash.getBounds();
		Rectangle shellRect = parent.getClientArea();
		int maxRight = shellRect.width - sashRect.width - limit;
		int newPos = Math.max(Math.min(e.x, maxRight), limit);
		if(newPos == sashRect.x) return;

		if(fixLeftWidth) fdSash.left = new FormAttachment(0, newPos);
		else             fdSash.right = new FormAttachment(100, newPos-shellRect.width);
		parent.layout();
	}


	// --------------- MouseListener ---------------

	public void mouseDown(MouseEvent e)
	{
		lastFixedControlWidth = (fixLeftWidth) ? fdSash.left.offset : fdSash.right.offset;
	}

	public void mouseUp(MouseEvent e)
	{
		int newWidth = (fixLeftWidth) ? fdSash.left.offset : fdSash.right.offset;
		if(newWidth != lastFixedControlWidth)
		{
			Event event = new Event();
			event.widget = sash;
			ControlEvent controlEvent = new ControlEvent(event);
			controlEvent.data = newWidth;
			
			for(int i=0; i<vListeners.size(); i++)
			{
				vListeners.get(i).controlResized(controlEvent);
			}
		}
	}

	public void mouseDoubleClick(MouseEvent e) { }		

	
	// --------------- public methods ---------------

	/**
	 * Maximizes one of the controls and hides the other.
	 * 
	 * @param controlPosition - Which control to maximize. Either SWT.LEFT, SWT.RIGHT or
	 *                          SWT.NONE to restore the not-maximized state.
	 */
	public void setMaximizedControl(int controlPosition)
	{
		if(controlPosition != SWT.LEFT && controlPosition != SWT.RIGHT && controlPosition != SWT.NONE)
		{
			throw new IllegalArgumentException("Invalid controlPosition value.");
		}

		if(controlPosition == SWT.LEFT)
		{
			sash.setVisible(false);
			rightControl.setVisible(false);
			fdLeft.right = new FormAttachment(100, 0);
		} else if(controlPosition == SWT.RIGHT)
		{
			sash.setVisible(false);
			leftControl.setVisible(false);
			fdRight.left = new FormAttachment(0, 0);
		} else
		{
			if(!rightControl.getVisible())
			{
				sash.setVisible(true);
				rightControl.setVisible(true);
				fdLeft.right = new FormAttachment(sash, 0);
			} else if(!leftControl.getVisible())
			{
				sash.setVisible(true);
				leftControl.setVisible(true);
				fdRight.left = new FormAttachment(sash, 0);
			}
		}

		parent.layout();
	}
	
	public void addControlListener(ControlListener listener) { vListeners.add(listener); }
	public void removeControlListener(ControlListener listener) { vListeners.remove(listener); }
}
