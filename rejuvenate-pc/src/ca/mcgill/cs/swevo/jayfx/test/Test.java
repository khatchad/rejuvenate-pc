package ca.mcgill.cs.swevo.jayfx.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.drools.FactHandle;
import org.drools.QueryResult;
import org.drools.QueryResults;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import uk.ac.lancs.comp.khatchad.core.Path;
import uk.ac.lancs.khatchad.IntentionElement;
import uk.ac.lancs.khatchad.IntentionGraph;
import uk.ac.lancs.khatchad.IntentionNode;
import uk.ac.lancs.khatchad.IntentionPath;
import uk.ac.lancs.khatchad.IntentionPattern;
import uk.ac.lancs.khatchad.Util;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.model.IElement;

public class Test implements IWorkbenchWindowActionDelegate {

	private static final String RESULT_PATH = "/Users/raffi/Documents/Results/";
	private IStructuredSelection aSelection;

	public void dispose() {
	}

	public void init(IWorkbenchWindow lWindow) {
	}

	public void run(IAction action) {
		final Collection<AdviceElement> toAnalyze = new ArrayList<AdviceElement>();
		toAnalyze.addAll(this.getSelectedAdvice());

		for (final IJavaProject proj : this.getSelectedJavaProjects())
			try {
				toAnalyze.addAll(Util.extractAdviceElements(proj));
			} catch (final JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}

		final IProgressMonitor lMonitor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getViewReferences()[0]
				.getView(true).getViewSite().getActionBars()
				.getStatusLineManager().getProgressMonitor();

		try {
			this.analyze(toAnalyze, lMonitor);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		lMonitor.done();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
			this.aSelection = (IStructuredSelection) selection;

	}

	@SuppressWarnings("unchecked")
	private void analyze(Collection<AdviceElement> adviceCol,
			IProgressMonitor lMonitor) throws Exception {
		final long start = System.currentTimeMillis();
		final JayFX lDB = new JayFX();

		final Collection<IProject> projectsToAnalyze = Util
				.getProjects(adviceCol);

		lDB.initialize(projectsToAnalyze, lMonitor, true);
		final IntentionGraph<IntentionNode<IElement>> graph = new IntentionGraph<IntentionNode<IElement>>(
				lDB, lMonitor);

		lMonitor.subTask("Loading up the rulebase.");
		final Reader source = new InputStreamReader(Test.class
				.getResourceAsStream("/rules/NodeRules.drl"));
		final RuleBase ruleBase = Util.readRule(source);
		final WorkingMemory workingMemory = ruleBase.newStatefulSession();

		final Set<IntentionElement<IElement>> elemCol = graph.flatten();
		lMonitor.beginTask("Inserting facts.", elemCol.size());
		for (final IntentionElement elem : elemCol) {
			workingMemory.insert(elem, true);
			lMonitor.worked(1);
		}

		lMonitor.subTask("Firing rules.");
		workingMemory.fireAllRules();

		final QueryResults pathsToReverse = workingMemory
				.getQueryResults("all paths");
		lMonitor.beginTask("Reversing all paths.", pathsToReverse.size());
		for (final Iterator it = pathsToReverse.iterator(); it.hasNext();) {
			final QueryResult result = (QueryResult) it.next();
			final Path path = (Path) result.get("$path");
			Collections.reverse(path);
			final FactHandle handle = workingMemory.getFactHandle(path);
			workingMemory.update(handle, path);
			lMonitor.worked(1);
		}

		lMonitor.beginTask("Enabling graph elements for each selected advice.",
				adviceCol.size());
		int pointcut_count = 0;

		for (final AdviceElement advElem : adviceCol) {
			System.out.println("Advice: " + advElem.readableName());
			
			graph.enableElementsAccordingTo(advElem, lMonitor);
			
			makeDotFile(graph, pointcut_count);

			System.out.println("Forward execution suggestions:");
			final QueryResults forwardSuggestedExecutionNodes = workingMemory
					.getQueryResults("forward suggested execution nodes");
			lMonitor.beginTask("Suggesting forward execution nodes.", forwardSuggestedExecutionNodes.size());
			final Set<IntentionNode<IElement>> forwardSuggestedNodeCollection = new LinkedHashSet<IntentionNode<IElement>>();
			for (final Iterator it = forwardSuggestedExecutionNodes.iterator(); it
					.hasNext();) {
				final QueryResult result = (QueryResult) it.next();
				final IntentionNode node = (IntentionNode) result
						.get("$suggestedNode");
				System.out.println("Suggested node: " + node);
				System.out.println("Enabled node: " + result.get("$enabledNode"));
				
				Path enabledPath = (Path)result.get("$enabledPath");
				System.out.println("Enabled path: " + enabledPath);
				System.out.println("Intersecting path: " + result.get("$intersectingPath"));
				
				IntentionNode commonNode = (IntentionNode)result.get("$commonNode");
				Path pattern = enabledPath.extractPattern(commonNode);
				System.out.println("Pattern: " + pattern);
				System.out.println("+===+");
				System.out.println();
//				forwardSuggestedNodeCollection.add(node);
				lMonitor.worked(1);
			}
			System.out.println("=+++=");
			System.out.println();
//			for (final Object node : forwardSuggestedNodeCollection)
//				System.out.println(node);
			
			System.out.println("Backward execution suggestion:");
			final QueryResults backwardSuggestedExecutionNodes = workingMemory
					.getQueryResults("backward suggested execution nodes");
			lMonitor.beginTask("Suggesting backward execution nodes.", backwardSuggestedExecutionNodes.size());
			final Set<IntentionNode<IElement>> backwardSuggestedNodeCollection = new LinkedHashSet<IntentionNode<IElement>>();
			for (final Iterator it = backwardSuggestedExecutionNodes.iterator(); it
					.hasNext();) {
				final QueryResult result = (QueryResult) it.next();
				final IntentionNode node = (IntentionNode) result
						.get("$suggestedNode");
				System.out.println("Suggested node: " + node);
				System.out.println("Enabled node: " + result.get("$enabledNode"));
				
				Path enabledPath = (Path)result.get("$enabledPath");
				System.out.println("Enabled path: " + enabledPath);
				System.out.println("Intersecting path: " + result.get("$intersectingPath"));
				
				IntentionNode commonNode = (IntentionNode)result.get("$commonNode");
				Path pattern = enabledPath.extractPattern(commonNode);
				System.out.println("Pattern: " + pattern);
				System.out.println("+===+");
//				backwardSuggestedNodeCollection.add(node);
				lMonitor.worked(1);
			}
			System.out.println("=+++=");
			System.out.println();
//			for (final Object node : backwardSuggestedNodeCollection)
//				System.out.println(node);

			pointcut_count++;
			lMonitor.worked(1);
			System.out.println("+++");
			/** ***************************************************************************************************** */
			/*
			//Enumerate all paths.
			final Set<IntentionPath> allPaths = Util.enumeratePaths(graph);

			// Group paths by their length. 
			final SortedMap<Integer, Set<IntentionPath>> lengthToSetOfPathsMap = new TreeMap<Integer, Set<IntentionPath>>();
			for (final IntentionPath path : allPaths)
				if (lengthToSetOfPathsMap.containsKey(path.getNumberOfNodes()))
					lengthToSetOfPathsMap.get(path.getNumberOfNodes())
							.add(path);
				else {
					final Set<IntentionPath> newEntry = new HashSet<IntentionPath>();
					newEntry.add(path);
					lengthToSetOfPathsMap
							.put(path.getNumberOfNodes(), newEntry);
				}

			final File textFile = new File(RESULT_PATH
					+ projectsToAnalyze.iterator().next().getName() + "_advice"
					+ pointcut_count + '-' + advElem.getElementName() + ".txt");
			final FileWriter txtFileOut = new FileWriter(textFile, false);
			final PrintWriter txtOut = new PrintWriter(txtFileOut);
			for (final int key : lengthToSetOfPathsMap.keySet()) {
				txtOut.println("All paths of length " + key + ":");
				final Set<IntentionPath> paths = lengthToSetOfPathsMap.get(key);
				for (final IntentionPath path : paths)
					txtOut.println(path);
				txtOut.println();
			}

			//find all enabled paths?  
			final SortedMap<Integer, Set<IntentionPath>> lengthToSetOfEnabledPathsMap = new TreeMap<Integer, Set<IntentionPath>>(
					lengthToSetOfPathsMap);

			for (final Set<IntentionPath> pathSet : lengthToSetOfEnabledPathsMap
					.values())
				for (final Iterator<IntentionPath> pit = pathSet.iterator(); pit
						.hasNext();) {
					final IntentionPath path = pit.next();
					if (!path.isEnabled())
						pit.remove();
				}
			for (final int key : lengthToSetOfEnabledPathsMap.keySet()) {
				txtOut.println("All enabled paths of length " + key + ":");
				final Set<IntentionPath> paths = lengthToSetOfEnabledPathsMap
						.get(key);
				for (final IntentionPath path : paths)
					txtOut.println(path);
				txtOut.println();
			}

			// Make the patterns: 
			final SortedMap<Integer, Set<IntentionPattern>> lengthToSetOfPatternsMap = new TreeMap<Integer, Set<IntentionPattern>>();
			for (final Set<IntentionPath> pathSet : lengthToSetOfEnabledPathsMap
					.values())
				for (final IntentionPath path : pathSet) {
					final int key = path.getNumberOfNodes();
					if (lengthToSetOfPatternsMap.containsKey(key)) {
						final IntentionPattern pattern = new IntentionPattern(
								path);
						lengthToSetOfPatternsMap.get(key).add(pattern);
						lengthToSetOfPatternsMap.get(key).addAll(
								pattern.getMoreAbstractPatterns());
					} else {
						final Set<IntentionPattern> newEntry = new HashSet<IntentionPattern>();
						final IntentionPattern newPattern = new IntentionPattern(
								path);
						newEntry.add(newPattern);
						newEntry.addAll(newPattern.getMoreAbstractPatterns());
						lengthToSetOfPatternsMap.put(key, newEntry);
					}
				}

			for (final int key : lengthToSetOfPatternsMap.keySet()) {
				txtOut.println("All patters of length " + key + ":");
				final Set<IntentionPattern> patterns = lengthToSetOfPatternsMap
						.get(key);
				for (final IntentionPattern pattern : patterns)
					txtOut.println(pattern);
				txtOut.println();
			}

			// Match patterns.  
			for (final Set<IntentionPattern> patternSet : lengthToSetOfPatternsMap
					.values())
				for (final IntentionPattern pattern : patternSet) {
					txtOut.println("Pattern " + pattern
							+ " matches the following paths:");
					for (final IntentionPath path : allPaths)
						if (pattern.matches(path))
							txtOut.println("\t" + path);
					txtOut.println("Applying pattern " + pattern
							+ " results in the following elements:");
					for (final IntentionElement<IElement> elem : pattern
							.apply(allPaths))
						txtOut.println("\t" + elem);
					final double precision = pattern.getPrecision(allPaths);
					txtOut.println("The precision of pattern " + pattern
							+ " is " + precision);
					txtOut.println();
				}
			final File resFile = new File(RESULT_PATH
					+ projectsToAnalyze.iterator().next().getName() + "_advice"
					+ pointcut_count + "-" + advElem + ".csv");

			final FileWriter resFileOut = new FileWriter(resFile, false);
			final PrintWriter resOut = new PrintWriter(resFileOut);
			resOut.println("Pattern\tPrecision\tConcreteness\tConfidence");

			for (final Set<IntentionPattern> patternSet : lengthToSetOfPatternsMap
					.values())
				for (final IntentionPattern pattern : patternSet)
					resOut.println(pattern
							+ "\t"
							+ pattern.getPrecision(allPaths)
							+ "\t"
							+ pattern.getConcreteness()
							+ "\t"
							+ this.calculateConfidence(pattern
									.getPrecision(allPaths), pattern
									.getConcreteness(), 0.75));

			resOut.close();
			txtOut.close();
			*/
		}

		final long elapsed = System.currentTimeMillis() - start;
		final int secs = (int) elapsed / 1000;
		System.out.println("Time (s): " + secs);
		System.out.println();
	}

	/**
	 * @param graph
	 * @throws IOException 
	 */
	private void makeDotFile(IntentionGraph<IntentionNode<IElement>> graph, int adviceNumer) throws IOException {
		File file = new File(RESULT_PATH + "adv" + adviceNumer + ".dot");
		FileWriter resFileOut = new FileWriter(file, false);
		PrintWriter resOut = new PrintWriter(resFileOut);
		resOut.println(graph.toDotFormat());
		resOut.close();
	}

	private double calculateConfidence(double precision, double concreteness,
			double d) {
		return 0;
	}

	private Collection<AdviceElement> getSelectedAdvice() {
		final Collection<AdviceElement> ret = new ArrayList<AdviceElement>();
		final Iterator i = this.aSelection.iterator();
		while (i.hasNext()) {
			final Object lNext = i.next();
			if (lNext instanceof AdviceElement)
				ret.add((AdviceElement) lNext);
		}

		return ret;
	}

	private Collection<IJavaProject> getSelectedJavaProjects() {
		final Collection<IJavaProject> ret = new ArrayList<IJavaProject>();
		final Iterator i = this.aSelection.iterator();
		while (i.hasNext()) {
			final Object lNext = i.next();
			if (lNext instanceof IJavaProject)
				ret.add((IJavaProject) lNext);
		}

		return ret;
	}
}