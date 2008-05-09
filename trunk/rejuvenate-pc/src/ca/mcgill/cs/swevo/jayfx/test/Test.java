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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.drools.FactHandle;
import org.drools.QueryResult;
import org.drools.QueryResults;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import uk.ac.lancs.comp.khatchad.core.Path;
import uk.ac.lancs.comp.khatchad.core.WildcardElement;
import uk.ac.lancs.khatchad.IntentionEdge;
import uk.ac.lancs.khatchad.IntentionElement;
import uk.ac.lancs.khatchad.IntentionGraph;
import uk.ac.lancs.khatchad.IntentionNode;
import uk.ac.lancs.khatchad.IntentionPath;
import uk.ac.lancs.khatchad.IntentionPattern;
import uk.ac.lancs.khatchad.Util;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

public class Test implements IWorkbenchWindowActionDelegate {

	private static final String RESULT_PATH = new File(ResourcesPlugin
			.getWorkspace().getRoot().getLocation().toOSString()
			+ File.separator + "results").getPath() + File.separator;
	private IStructuredSelection aSelection;

	public void dispose() {
	}

	public void init(IWorkbenchWindow lWindow) {
		File resultFolder = new File(RESULT_PATH);
		if (!resultFolder.exists()) {
			resultFolder.mkdir();
		}
	}

	public void run(IAction action) {
		final IProgressMonitor lMonitor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getViewReferences()[0]
				.getView(true).getViewSite().getActionBars()
				.getStatusLineManager().getProgressMonitor();

		Collection<AdviceElement> selectedAdvice = this.getSelectedAdvice();
		Collection<IJavaProject> selectedProjectCol = this
				.getSelectedJavaProjects();

		if (!selectedAdvice.isEmpty() && !selectedProjectCol.isEmpty())
			throw new IllegalStateException(
					"For test runs, select *either* project or advice but not both.");

		if (!selectedAdvice.isEmpty()) {
			try {
				this.analyze(selectedAdvice, lMonitor);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		else if (!selectedProjectCol.isEmpty()) {
			PrintWriter benchmarkOut = null;
			try {
				benchmarkOut = getBenchmarkStatsWriter();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			benchmarkOut.println("Benchmark\t#Advice\tTime(s)");
			for (IJavaProject proj : selectedProjectCol) {
				final long start = System.currentTimeMillis();
				Collection<? extends AdviceElement> toAnalyze = null;
				try {
					toAnalyze = Util.extractValidAdviceElements(proj);
					this.analyze(toAnalyze, lMonitor);
				}
				catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				final long elapsed = System.currentTimeMillis() - start;
				final int secs = (int) elapsed / 1000;

				benchmarkOut.print(proj.getProject().getName() + "\t");
				benchmarkOut.print(toAnalyze.size() + "\t");
				benchmarkOut.print(secs + "\t");
				benchmarkOut.println();
			}
			benchmarkOut.close();
		}

		/*
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
		
		try {
		this.analyze(toAnalyze, lMonitor);
		} catch (final Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw new RuntimeException(e);
		}
		*/
		lMonitor.done();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
			this.aSelection = (IStructuredSelection) selection;

	}

	@SuppressWarnings( { "unchecked", "restriction" })
	private void analyze(Collection<? extends AdviceElement> adviceCol,
			IProgressMonitor lMonitor) throws Exception {
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

		PrintWriter patternOut = getPatternStatsWriter();
		patternOut
				.println("Benchmark\tAdvice#\tAdvice\tPattern\tSize\tPrecision\tConcreteness");

		lMonitor.beginTask("Enabling graph elements for each selected advice.",
				adviceCol.size());
		int pointcut_count = 0;

		for (final AdviceElement advElem : adviceCol) {

			Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap = new LinkedHashMap<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>>();

			Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap = new LinkedHashMap<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>>();

			//			System.out.println("Advice: " + advElem.readableName());

			graph.enableElementsAccordingTo(advElem, lMonitor);

			//			makeDotFile(graph, pointcut_count);

			//			System.out.println("Forward execution suggestions:");
			executeNodeQuery(new SubProgressMonitor(lMonitor, 1,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
					workingMemory, patternToResultMap,
					patternToEnabledElementMap,
					"forward suggested execution nodes");
			//			System.out.println("=+++=");
			//			System.out.println();

			//			System.out.println("Backward execution suggestion:");
			executeNodeQuery(new SubProgressMonitor(lMonitor, 1,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
					workingMemory, patternToResultMap,
					patternToEnabledElementMap,
					"backward suggested execution nodes");

			//			System.out.println("Forward call suggestions:");
			executeArcQuery("forward suggested X arcs", Relation.CALLS,
					workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			executeArcQuery("backward suggested X arcs", Relation.CALLS,
					workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			executeArcQuery("forward suggested X arcs", Relation.GETS,
					workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			executeArcQuery("backward suggested X arcs", Relation.GETS,
					workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			executeArcQuery("forward suggested X arcs", Relation.SETS,
					workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			executeArcQuery("backward suggested X arcs", Relation.SETS,
					workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			//			System.out.println("Backward call suggestions:");
			//			System.out.println("=+++=");
			//			System.out.println();

			//			PrintWriter suggestionOut = getSuggestionWriter();
			//			suggestionOut
			//					.println("Benchmark\tAdvice#\tAdvice\tPattern\tSuggestion");

			//			System.out.println("Results:");
			for (Path pattern : patternToResultMap.keySet()) {
				//				System.out.println("Pattern results for: " + pattern);
				//				System.out.println("\t" + "Suggested elements:");
				//				for (IntentionElement<IElement> resultNode : patternToResultMap
				//						.get(pattern)) {
				//					System.out.println("\t\t" + resultNode);
				//					suggestionOut.println(advElem.getJavaProject().getProject()
				//							.getName()
				//							+ "\t"
				//							+ pointcut_count
				//							+ "\t"
				//							+ advElem.readableName()
				//							+ "\t"
				//							+ pattern
				//							+ "\t"
				//							+ resultNode);
				//				}

				double precision = calculatePrecision(
						patternToEnabledElementMap.get(pattern),
						patternToResultMap.get(pattern));
				//				System.out.println("\tPrecision: " + precision);

				double concreteness = calculateConcreteness(pattern);
				//				System.out.println("\tConcreteness: " + concreteness);

				double confidence = calculateConfidence(precision,
						concreteness, .5);
				//				System.out.println("\tConfidence: " + confidence);

				//				System.out.println();

				patternOut.print(advElem.getJavaProject().getProject()
						.getName()
						+ "\t");
				patternOut.print(pointcut_count + "\t");
				patternOut.print(advElem.readableName() + "\t");
				patternOut.print(pattern + "\t");
				patternOut.print(pattern.size() + "\t");
				patternOut.print(precision + "\t");
				patternOut.print(concreteness + "\t");
				patternOut.println();
			}
			//			suggestionOut.close();
			pointcut_count++;
			lMonitor.worked(1);
		}
		patternOut.println();
		patternOut.close();

		//		System.out.println("Time (s): " + secs);

		//		PrintWriter benchmarkOut = getBenchmarkStatsWriter();
		//		benchmarkOut.println("Benchmark\tTime(s)");
		//		benchmarkOut.print(projectsToAnalyze.iterator().next().getProject()
		//				.getName()
		//				+ "\t");
		//		benchmarkOut.print(secs + "\t");
		//		benchmarkOut.println();
		//		benchmarkOut.close();

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

	/**
	 * @param relation
	 * @param string
	 * @param workingMemory
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 * @param lMonitor
	 */
	private static void executeArcQuery(
			String queryString,
			Relation relation,
			final WorkingMemory workingMemory,
			Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap,
			IProgressMonitor lMonitor) {

		final QueryResults suggestedArcs = workingMemory.getQueryResults(
				queryString, new Object[] { relation });

		lMonitor.beginTask("Executing query: "
				+ queryString.replace("X", relation.toString()) + ".",
				suggestedArcs.size());
		for (final Iterator it = suggestedArcs.iterator(); it.hasNext();) {
			final QueryResult result = (QueryResult) it.next();
			final IntentionEdge suggestedEdge = (IntentionEdge) result
					.get("$suggestedEdge");
			//				System.out.println("Suggested edge: " + suggestedEdge);

			IntentionElement enabledEdge = (IntentionElement) result
					.get("$enabledEdge");
			//				System.out.println("Enabled edge: " + enabledEdge);

			Path enabledPath = (Path) result.get("$enabledPath");
			//				System.out.println("Enabled path: " + enabledPath);
			//				System.out.println("Intersecting path: "
			//						+ result.get("$intersectingPath"));

			IntentionNode commonNode = (IntentionNode) result
					.get("$commonNode");
			Path pattern = enabledPath.extractPattern(commonNode);
			//				System.out.println("Pattern: " + pattern);

			if (!patternToResultMap.containsKey(pattern))
				patternToResultMap.put(pattern,
						new LinkedHashSet<IntentionElement<IElement>>());
			patternToResultMap.get(pattern).add(suggestedEdge);

			if (!patternToEnabledElementMap.containsKey(pattern))
				patternToEnabledElementMap.put(pattern,
						new LinkedHashSet<IntentionElement<IElement>>());
			patternToEnabledElementMap.get(pattern).add(enabledEdge);

			//				System.out.println("+===+");
			lMonitor.worked(1);
		}
		lMonitor.done();
	}

	/**
	 * @param lMonitor
	 * @param workingMemory
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 */
	private static void executeNodeQuery(
			IProgressMonitor lMonitor,
			final WorkingMemory workingMemory,
			Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap,
			String queryString) {

		final QueryResults suggestedNodes = workingMemory
				.getQueryResults(queryString);
		lMonitor.beginTask("Executing node query: " + queryString + ".",
				suggestedNodes.size());
		for (final Iterator it = suggestedNodes.iterator(); it.hasNext();) {
			final QueryResult result = (QueryResult) it.next();
			final IntentionNode suggestedNode = (IntentionNode) result
					.get("$suggestedNode");
			//				System.out.println("Suggested node: " + suggestedNode);

			IntentionNode enabledNode = (IntentionNode) result
					.get("$enabledNode");
			//				System.out.println("Enabled node: " + enabledNode);

			Path enabledPath = (Path) result.get("$enabledPath");
			//				System.out.println("Enabled path: " + enabledPath);
			//				System.out.println("Intersecting path: "
			//						+ result.get("$intersectingPath"));

			IntentionNode commonNode = (IntentionNode) result
					.get("$commonNode");
			Path pattern = enabledPath.extractPattern(commonNode);
			//				System.out.println("Pattern: " + pattern);

			if (!patternToResultMap.containsKey(pattern)) {
				patternToResultMap.put(pattern,
						new LinkedHashSet<IntentionElement<IElement>>());
			}
			patternToResultMap.get(pattern).add(suggestedNode);

			if (!patternToEnabledElementMap.containsKey(pattern))
				patternToEnabledElementMap.put(pattern,
						new LinkedHashSet<IntentionElement<IElement>>());
			patternToEnabledElementMap.get(pattern).add(enabledNode);

			//				System.out.println("+===+");
			//				System.out.println();
			lMonitor.worked(1);
		}
		lMonitor.done();
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private static PrintWriter getCSVWriter(File aFile, boolean append)
			throws IOException {
		final FileWriter resFileOut = new FileWriter(aFile, append);
		return new PrintWriter(resFileOut);
	}

	private static PrintWriter getPatternStatsWriter() throws IOException {
		final File aFile = new File(RESULT_PATH + "patterns.csv");
		return getCSVWriter(aFile, true);
	}

	private static PrintWriter getBenchmarkStatsWriter() throws IOException {
		final File aFile = new File(RESULT_PATH + "benchmarks.csv");
		return getCSVWriter(aFile, true);
	}

	private static PrintWriter getSuggestionWriter() throws IOException {
		final File aFile = new File(RESULT_PATH + "suggestion.csv");
		return getCSVWriter(aFile, true);
	}

	private static PrintWriter getAdviceStatusWriter() throws IOException {
		final File aFile = new File(RESULT_PATH + "advice.csv");
		return getCSVWriter(aFile, true);
	}

	/**
	 * @param precision
	 * @param concreteness
	 * @param d
	 * @return
	 */
	private static double calculateConfidence(double precision,
			double concreteness, double weight) {
		double result = precision * weight + (1 - concreteness) * (1 - weight);
		return Math.max(result, precision);
	}

	/**
	 * @param pattern
	 * @return
	 */
	private static double calculateConcreteness(
			Path<IntentionEdge<IElement>> pattern) {
		Collection<IntentionNode<IElement>> allNodes = pattern.getNodes();
		Collection<IntentionNode<IElement>> wildNodes = pattern
				.getWildcardNodes();
		return ((double) (allNodes.size() - wildNodes.size()))
				/ allNodes.size();
	}

	/**
	 * @param searchedFor
	 * @param set
	 * @return
	 */
	private static double calculatePrecision(
			Set<IntentionElement<IElement>> searchedFor,
			Set<IntentionElement<IElement>> found) {
		int totalElements = found.size();
		int lookingFor = searchedFor.size();
		return ((double) lookingFor) / totalElements;
	}

	/**
	 * @param graph
	 * @throws IOException
	 */
	private void makeDotFile(IntentionGraph<IntentionNode<IElement>> graph,
			int adviceNumer) throws IOException {
		File file = new File(RESULT_PATH + "adv" + adviceNumer + ".dot");
		makeDotFile(graph, file);
	}

	private void makeDotFile(IntentionGraph<IntentionNode<IElement>> graph)
			throws IOException {
		File file = new File(RESULT_PATH + "intention_graph.dot");
		makeDotFile(graph, file);
	}

	private void makeDotFile(IntentionGraph<IntentionNode<IElement>> graph,
			File aFile) throws IOException {
		FileWriter resFileOut = new FileWriter(aFile, false);
		PrintWriter resOut = new PrintWriter(resFileOut);
		resOut.println(graph.toDotFormat());
		resOut.close();
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