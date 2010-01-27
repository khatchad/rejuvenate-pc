/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.ui;

import static uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util.getDefaultConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.PointcutRejuvenator;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionArc;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.model.Suggestion;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.AJUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.DatabaseUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.XMLUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.util.PostMan;
import ca.mcgill.cs.swevo.jayfx.model.IElement;

/**
 * @author raffi
 * 
 */
public class RejuvenatePointcutPlugin extends PointcutRefactoringPlugin {	
	
	/**
	 * Number of times to run the experiment per advice.
	 */
	private static final int NUMBER_OF_RUNS = 3;

	private PointcutRejuvenator rejuvenator = new PointcutRejuvenator();

	private static RejuvenatePointcutPlugin instance;
	
	public RejuvenatePointcutPlugin() {
		this.instance = this;
	}

	@Override
	protected void run(IProgressMonitor monitor) {
		this.rejuvenator.clearSuggestionList();
		final Collection<AdviceElement> selectedAdvice = this
				.getSelectedAdvice();

		StringBuffer emailMessage = new StringBuffer();
		final String header = "Advice\tTime (s)";
		System.out.println(header);
		emailMessage.append(header + '\n');
		for (AdviceElement advElem : selectedAdvice) {
			double totalSecs = 0;
			for ( int i = 0; i < NUMBER_OF_RUNS; i++ ) {
				final long start = System.currentTimeMillis();
				this.rejuvenator.analyzeAdvice(Collections.singleton(advElem), monitor);
				final double secs = calculateTimeStatistics(start);
				final String row = advElem.getHandleIdentifier() + "\t" + secs;
				System.out.println(row);
				emailMessage.append(row + '\n');
				totalSecs += secs; 
			}
			final String average = "Average:" + "\t" + totalSecs / NUMBER_OF_RUNS;
			System.out.println(average);
			emailMessage.append(average + '\n');
		}
		
		PostMan.postMail("Rejuvenation complete.", emailMessage.toString(), "Raffi Khatchadourian <khatchad@cse.ohio-state.edu>", "Raffi Khatchadourian <khatchad@cse.ohio-state.edu>");
		monitor.done();
	}

	@Override
	public void run(IAction action) {
		this.instance = this;
		final IProgressMonitor monitor = getProgressMonitor();
		this.run(monitor);
	}

	/**
	 * @return the suggestionList
	 */
	public List<Suggestion<IJavaElement>> getSuggestionList() {
		return this.rejuvenator.getSuggestionList();
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.PointcutRefactoringPlugin#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		super.init(window);
		this.instance = this;
	}

	public static RejuvenatePointcutPlugin getInstance() {
		return instance;
	}
}