package ch.mtSystems.javaCompiler.control;

import java.io.File;
import java.util.Vector;

import org.eclipse.swt.widgets.Control;

import ch.mtSystems.javaCompiler.model.JavaCompilerProject;
import ch.mtSystems.javaCompiler.view.JavaCompilerGui;
import ch.mtSystems.javaCompiler.view.pages.CompilationPage;
import ch.mtSystems.javaCompiler.view.pages.SettingsPage;
import ch.mtSystems.javaCompiler.view.pages.SourcePage;
import ch.mtSystems.javaCompiler.view.pages.CreateProjectPage;
import ch.mtSystems.javaCompiler.view.pages.IntroductionPage;


public class AppController
{
	public static int uniqueID = 0;
	public static final int PAGE_INTRODUCTION = ++uniqueID;
	public static final int PAGE_CREATE_PROJECT = ++uniqueID;
	public static final int PAGE_SOURCE = ++uniqueID;
	public static final int PAGE_SETTINGS = ++uniqueID;
	public static final int PAGE_COMPILATION = ++uniqueID;


	public static File curDir; // used for file and directory dialogs
	private int curPage = -1;
	private JavaCompilerProject currentProject;
	private Vector<IAppControllerListener> vListeners = new Vector<IAppControllerListener>();


	// --------------- public methods ---------------

	public JavaCompilerProject getCurrentProject()
	{
		return currentProject;
	}

	public void setCurrentProject(JavaCompilerProject project)
	{
		currentProject = project;

		for(int i=0; i<vListeners.size(); i++)
		{
			vListeners.get(i).projectChanged(project);
		}
	}

	public void loadPage(int page)
	{
		Control[] ca = JavaCompilerGui.getContentComposite().getChildren();
		for(int i=0; i<ca.length; i++) ca[i].dispose();

			 if(page == PAGE_INTRODUCTION)   new IntroductionPage();
		else if(page == PAGE_CREATE_PROJECT) new CreateProjectPage();
		else if(page == PAGE_SOURCE)         new SourcePage();
		else if(page == PAGE_SETTINGS)       new SettingsPage();
		else if(page == PAGE_COMPILATION)    new CompilationPage();

		curPage = page;
		JavaCompilerGui.getContentComposite().layout();

		for(int i=0; i<vListeners.size(); i++)
		{
			vListeners.get(i).pageLoaded(page);
		}
	}

	public int getCurrentPage()
	{
		return curPage;
	}

	public void fireProjectChanged()
	{
		for(int i=0; i<vListeners.size(); i++)
		{
			vListeners.get(i).projectUpdated();
		}
	}

	public void fireProjectSaved()
	{
		for(int i=0; i<vListeners.size(); i++)
		{
			vListeners.get(i).projectSaved();
		}
	}

	public void addAppControllerListener(IAppControllerListener acl) { vListeners.add(acl); }
	public void removeAppControllerListener(IAppControllerListener acl) { vListeners.remove(acl); }


	// --------------- singleton pattern ---------------

	private AppController() { }
	private static AppController appController = new AppController();
	public static AppController getAppController() { return appController; }
}
