/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.Constants;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.AJUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.DatabaseUtil;
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
				toAnalyze.addAll(AJUtil.extractValidAdviceElements(proj));
			}
			catch (JavaModelException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		monitor.beginTask("Analyzing advice.", toAnalyze.size());
//		System.out
//				.println("Key\tType\tBenchmark\tPackage\tClass\tEnclosing Method\tShadow\tAdvised By");
		for (AdviceElement advElem : toAnalyze) {
			try {
				analyze(advElem, new SubProgressMonitor(monitor, -1));
			}
			catch (JavaModelException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			monitor.worked(1);
		}
	}

	/**
	 * @param advElem
	 * @param monitor
	 * @throws JavaModelException
	 * @throws SQLException
	 * @throws ClassNotFoundException 
	 */
	private void analyze(AdviceElement advElem, IProgressMonitor monitor)
			throws JavaModelException, SQLException, ClassNotFoundException {

		String adviceKey = DatabaseUtil.getKey(advElem);
		DatabaseUtil.insertAdviceIntoDatabase(adviceKey);

		Set<IJavaElement> advisedJavaElements = AJUtil
				.getAdvisedJavaElements(advElem);
		monitor.beginTask("Retrieving advised Java Elements.",
				advisedJavaElements.size());

		for (IJavaElement javaElem : advisedJavaElements) {
			DatabaseUtil.insertShadowAndRelationshipIntoDatabase(adviceKey, javaElem,
					DatabaseUtil.AdviceShadowRelationship.ADVISES);
			monitor.worked(1);
		}
		monitor.done();
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}
}