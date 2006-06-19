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
