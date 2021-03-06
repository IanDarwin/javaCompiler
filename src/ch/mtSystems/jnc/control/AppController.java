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

package ch.mtSystems.jnc.control;

import java.io.File;
import java.util.Vector;

import org.eclipse.swt.widgets.Control;

import ch.mtSystems.jnc.model.JNCProject;
import ch.mtSystems.jnc.view.JNC;
import ch.mtSystems.jnc.view.pages.AdvancedSettingsPage;
import ch.mtSystems.jnc.view.pages.CompilationPage;
import ch.mtSystems.jnc.view.pages.BasicSettingsPage;
import ch.mtSystems.jnc.view.pages.SourcePage;
import ch.mtSystems.jnc.view.pages.CreateProjectPage;
import ch.mtSystems.jnc.view.pages.IntroductionPage;
import ch.mtSystems.jnc.view.pages.WizzardPage;


public class AppController implements IAppControllerListener
{
	public static int uniqueID = 0;
	public static final int PAGE_INTRODUCTION = ++uniqueID;
	public static final int PAGE_CREATE_PROJECT = ++uniqueID;
	public static final int PAGE_SOURCE = ++uniqueID;
	public static final int PAGE_BASIC_SETTINGS = ++uniqueID;
	public static final int PAGE_ADVANCED_SETTINGS = ++uniqueID;
	public static final int PAGE_COMPILATION = ++uniqueID;


	public static File curDir; // used for file and directory dialogs
	private int curPage = -1;
	private JNCProject currentProject;
	private Vector<IAppControllerListener> vListeners = new Vector<IAppControllerListener>();


	// --------------- IAppControllerListener ---------------

	public void projectChanged(JNCProject project) { }

	public void projectUpdated()
	{
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectUpdated();
	}

	public void projectSaved()
	{
		for(int i=0; i<vListeners.size(); i++) vListeners.get(i).projectSaved();
	}


	// --------------- public methods ---------------

	public JNCProject getCurrentProject()
	{
		return currentProject;
	}

	public void setCurrentProject(JNCProject project)
	{
		if(currentProject != null) currentProject.removeProjectListener(this);
		currentProject = project;
		currentProject.addProjectListener(this);

		for(int i=0; i<vListeners.size(); i++)
		{
			vListeners.get(i).projectChanged(project);
		}
	}

	public WizzardPage loadPage(int page)
	{
		Control[] ca = JNC.getContentComposite().getChildren();
		for(int i=0; i<ca.length; i++) ca[i].dispose();

		WizzardPage wp;
		     if(page == PAGE_CREATE_PROJECT)    wp = new CreateProjectPage();
		else if(page == PAGE_SOURCE)            wp = new SourcePage();
		else if(page == PAGE_BASIC_SETTINGS)    wp = new BasicSettingsPage();
		else if(page == PAGE_ADVANCED_SETTINGS) wp = new AdvancedSettingsPage();
		else if(page == PAGE_COMPILATION)       wp = new CompilationPage();
		else                                    wp = new IntroductionPage();

		curPage = page;
		JNC.getContentComposite().layout();
		
		return wp;
	}

	public int getCurrentPage()
	{
		return curPage;
	}


	public void addAppControllerListener(IAppControllerListener acl) { vListeners.add(acl); }
	public void removeAppControllerListener(IAppControllerListener acl) { vListeners.remove(acl); }


	// --------------- singleton pattern ---------------

	private AppController() { }
	private static AppController appController = new AppController();
	public static AppController getAppController() { return appController; }
}
