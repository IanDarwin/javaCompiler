/*
 *   JavaCompiler - A java to native compiler for Windows and Linux.
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

package ch.mtSystems.javaCompiler.view.utilities;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;


public class LayoutUtilities
{
	public static GridData createGridData(int style, int horizontalSpan, int verticalSpan,
			int verticalIndent, int horizontalIndent)
	{
		GridData gd = (style > -1) ? new GridData(style) : new GridData();
		if(horizontalSpan > -1) gd.horizontalSpan = horizontalSpan;
		if(verticalSpan > -1) gd.verticalSpan = verticalSpan;
		if(verticalIndent > -1) gd.verticalIndent = verticalIndent;
		if(horizontalIndent > -1) gd.horizontalIndent = horizontalIndent;
		return gd;
	}

	public static GridData createGridData(int style, int horizontalSpan, int verticalSpan)
	{
		GridData gd = (style > -1) ? new GridData(style) : new GridData();
		if(horizontalSpan > -1) gd.horizontalSpan = horizontalSpan;
		if(verticalSpan > -1) gd.verticalSpan = verticalSpan;
		return gd;
	}

	public static GridLayout createGridLayout(int numColumns, int margin)
	{
		GridLayout gl = new GridLayout(numColumns, false);
		gl.marginWidth = margin; gl.marginHeight = margin;
		return gl;
	}

	public static GridLayout createGridLayout(int numColumns, int margin, int horizontalSpacing)
	{
		GridLayout gl = new GridLayout(numColumns, false);
		gl.marginWidth = margin; gl.marginHeight = margin;
		gl.horizontalSpacing = horizontalSpacing;
		return gl;
	}
}
