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
