/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;

/**
 * @author raffi
 * 
 */
public abstract class Plugin implements IWorkbenchWindowActionDelegate {

	/**
	 * The selected item on the workbench.
	 */
	private IStructuredSelection aSelection;

	/**
	 * 
	 */
	public Plugin() {
		super();
	}

	public void selectionChanged(final IAction action,
			final ISelection selection) {
		if (selection instanceof IStructuredSelection)
			this.aSelection = (IStructuredSelection) selection;
	}

	@SuppressWarnings("unchecked")
	protected Collection<AdviceElement> getSelectedAdvice() {
		final Collection<AdviceElement> ret = new ArrayList<AdviceElement>();
		final Iterator i = this.aSelection.iterator();
		while (i.hasNext()) {
			final Object lNext = i.next();
			if (lNext instanceof AdviceElement)
				ret.add((AdviceElement) lNext);
		}

		return ret;
	}

	@SuppressWarnings("unchecked")
	protected Collection<IJavaProject> getSelectedJavaProjects() {
		final Collection<IJavaProject> ret = new ArrayList<IJavaProject>();
		final Iterator i = this.aSelection.iterator();
		while (i.hasNext()) {
			final Object lNext = i.next();
			if (lNext instanceof IProject) {
				IProject proj = (IProject) lNext;
				IJavaProject jProj = JavaCore.create(proj);
				if (jProj != null)
					ret.add(jProj);
			}
			else if (lNext instanceof IJavaProject) {
				IJavaProject jProj = (IJavaProject)lNext;
				ret.add(jProj);
			}
		}

		return ret;
	}

	protected abstract void run(IProgressMonitor monitor);

	/**
	 * The main method invoked when the plug-in is clicked.
	 */
	public void run(final IAction action) {
		Job job = new Job("Analyzing pointcut expressions") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Plugin.this.run(monitor);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.LONG);
		job.schedule();
	}
}