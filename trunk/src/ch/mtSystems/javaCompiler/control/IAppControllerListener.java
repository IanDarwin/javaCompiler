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

package ch.mtSystems.javaCompiler.control;

import ch.mtSystems.javaCompiler.model.JavaCompilerProject;


public interface IAppControllerListener
{
	/**
	 * Called when a new project has been created or an existing project has been opened.
	 * @param project The opened/created project.
	 */
	public void projectChanged(JavaCompilerProject project);

	/**
	 * Called when the current project has been updated (e.g. source added, settings changed, ...)
	 */
	public void projectUpdated();

	/**
	 * Called when the current project has been saved.
	 */
	public void projectSaved();

	/**
	 * Called when a page has been loaded (intro, create project, settings or compile).
	 */
	public void pageLoaded(int page);
}
