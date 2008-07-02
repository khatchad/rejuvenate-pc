/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;

/**
 * @author raffi
 *
 */
public class ShadowAnalyzerPlugin extends Plugin {

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.Plugin#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void run(IProgressMonitor monitor) {
		Collection<AdviceElement> toAnalyze = new LinkedHashSet<AdviceElement>();

		toAnalyze.addAll(this.getSelectedAdvice());

		for (IJavaProject proj : this.getSelectedJavaProjects()) {
			try {
				toAnalyze.addAll(Util.extractValidAdviceElements(proj));
			}
			catch (JavaModelException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
		}	
		
		monitor.beginTask("Analyzing advice.", toAnalyze.size());
		for (AdviceElement advElem : toAnalyze) {
			try {
				analyze(advElem, new SubProgressMonitor(monitor, -1));
			}
			catch (JavaModelException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
			monitor.worked(1);
		}
	}

	/**
	 * @param advElem
	 * @param monitor
	 * @throws JavaModelException 
	 */
	private void analyze(AdviceElement advElem, IProgressMonitor monitor) throws JavaModelException {
		System.out.println(advElem);
		Set<IJavaElement> advisedJavaElements = Util.getAdvisedJavaElements(advElem);
		monitor.beginTask("Retrieving advised Java Elements.", advisedJavaElements.size());
		for (IJavaElement javaElem : advisedJavaElements)
			System.out.println(javaElem);
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}
}