package uk.ac.lancs.comp.khatchad.rejuvenatepc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.FactHandle;
import org.drools.QueryResult;
import org.drools.QueryResults;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionEdge;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Path;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

public class AnalyzePointcut implements IWorkbenchWindowActionDelegate {

	/**
	 * Where to store benchmark results.
	 */
	private static final String RESULT_PATH = new File(ResourcesPlugin
			.getWorkspace().getRoot().getLocation().toOSString()
			+ File.separator + "results").getPath()
			+ File.separator;

	/**
	 * @param pattern
	 * @return
	 */
	private static double calculateConcreteness(
			final Path<IntentionEdge<IElement>> pattern) {
		final Collection<IntentionNode<IElement>> allNodes = pattern.getNodes();
		final Collection<IntentionNode<IElement>> wildNodes = pattern
				.getWildcardNodes();
		return (double) (allNodes.size() - wildNodes.size()) / allNodes.size();
	}

	/**
	 * @param precision
	 * @param concreteness
	 * @param d
	 * @return
	 */
	@SuppressWarnings("unused")
	private static double calculateConfidence(final double precision,
			final double concreteness, final double weight) {
		final double result = precision * weight + (1 - concreteness)
				* (1 - weight);
		return Math.max(result, precision);
	}

	/**
	 * @param searchedFor
	 * @param set
	 * @return
	 */
	private static double calculatePrecision(
			final Set<IntentionElement<IElement>> searchedFor,
			final Set<IntentionElement<IElement>> found) {
		final int totalElements = found.size();
		final int lookingFor = searchedFor.size();
		return (double) lookingFor / totalElements;
	}

	/**
	 * @param relation
	 * @param string
	 * @param workingMemory
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 * @param lMonitor
	 */
	@SuppressWarnings("unchecked")
	private static void executeArcQuery(
			final String queryString,
			final Relation relation,
			final WorkingMemory workingMemory,
			final Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap,
			final IProgressMonitor lMonitor) {

		final QueryResults suggestedArcs = workingMemory.getQueryResults(
				queryString, new Object[] { relation });

		lMonitor.beginTask("Executing query: "
				+ queryString.replace("X", relation.toString()) + ".",
				suggestedArcs.size());
		for (final Iterator it = suggestedArcs.iterator(); it.hasNext();) {
			final QueryResult result = (QueryResult) it.next();
			final IntentionEdge suggestedEdge = (IntentionEdge) result
					.get("$suggestedEdge");

			final IntentionElement enabledEdge = (IntentionElement) result
					.get("$enabledEdge");

			final Path enabledPath = (Path) result.get("$enabledPath");

			final IntentionNode commonNode = (IntentionNode) result
					.get("$commonNode");
			final Path pattern = enabledPath.extractPattern(commonNode);

			if (!patternToResultMap.containsKey(pattern))
				patternToResultMap.put(pattern,
						new LinkedHashSet<IntentionElement<IElement>>());
			patternToResultMap.get(pattern).add(suggestedEdge);

			if (!patternToEnabledElementMap.containsKey(pattern))
				patternToEnabledElementMap.put(pattern,
						new LinkedHashSet<IntentionElement<IElement>>());
			patternToEnabledElementMap.get(pattern).add(enabledEdge);

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
	@SuppressWarnings("unchecked")
	private static void executeNodeQuery(
			final IProgressMonitor lMonitor,
			final WorkingMemory workingMemory,
			final Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap,
			final String queryString) {

		final QueryResults suggestedNodes = workingMemory
				.getQueryResults(queryString);
		lMonitor.beginTask("Executing node query: " + queryString + ".",
				suggestedNodes.size());
		for (final Iterator it = suggestedNodes.iterator(); it.hasNext();) {
			final QueryResult result = (QueryResult) it.next();
			final IntentionNode suggestedNode = (IntentionNode) result
					.get("$suggestedNode");

			final IntentionNode enabledNode = (IntentionNode) result
					.get("$enabledNode");

			final Path enabledPath = (Path) result.get("$enabledPath");

			final IntentionNode commonNode = (IntentionNode) result
					.get("$commonNode");
			final Path pattern = enabledPath.extractPattern(commonNode);

			if (!patternToResultMap.containsKey(pattern))
				patternToResultMap.put(pattern,
						new LinkedHashSet<IntentionElement<IElement>>());
			patternToResultMap.get(pattern).add(suggestedNode);

			if (!patternToEnabledElementMap.containsKey(pattern))
				patternToEnabledElementMap.put(pattern,
						new LinkedHashSet<IntentionElement<IElement>>());
			patternToEnabledElementMap.get(pattern).add(enabledNode);

			lMonitor.worked(1);
		}
		lMonitor.done();
	}

	@SuppressWarnings("unused")
	private static PrintWriter getAdviceStatusWriter() throws IOException {
		final File aFile = new File(AnalyzePointcut.RESULT_PATH + "advice.csv");
		return AnalyzePointcut.getCSVWriter(aFile, true);
	}

	private static PrintWriter getBenchmarkStatsWriter() throws IOException {
		final File aFile = new File(AnalyzePointcut.RESULT_PATH
				+ "benchmarks.csv");
		return AnalyzePointcut.getCSVWriter(aFile, true);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private static PrintWriter getCSVWriter(final File aFile,
			final boolean append) throws IOException {
		final FileWriter resFileOut = new FileWriter(aFile, append);
		return new PrintWriter(resFileOut);
	}

	private static PrintWriter getPatternStatsWriter() throws IOException {
		final File aFile = new File(AnalyzePointcut.RESULT_PATH
				+ "patterns.csv");
		return AnalyzePointcut.getCSVWriter(aFile, true);
	}

	@SuppressWarnings("unused")
	private static PrintWriter getSuggestionWriter() throws IOException {
		final File aFile = new File(AnalyzePointcut.RESULT_PATH
				+ "suggestion.csv");
		return AnalyzePointcut.getCSVWriter(aFile, true);
	}

	/**
	 * The selected item on the workbench.
	 */
	private IStructuredSelection aSelection;

	public void dispose() {
	}

	/**
	 * Make the result folder on startup.
	 */
	public void init(final IWorkbenchWindow lWindow) {
		final File resultFolder = new File(AnalyzePointcut.RESULT_PATH);
		if (!resultFolder.exists())
			resultFolder.mkdir();
	}

	/**
	 * The main method invoked when the plug-in is clicked.
	 */
	public void run(final IAction action) {
		final IProgressMonitor lMonitor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getViewReferences()[0]
				.getView(true).getViewSite().getActionBars()
				.getStatusLineManager().getProgressMonitor();

		final Collection<AdviceElement> selectedAdvice = this
				.getSelectedAdvice();
		final Collection<IJavaProject> selectedProjectCol = this
				.getSelectedJavaProjects();

		if (!selectedAdvice.isEmpty() && !selectedProjectCol.isEmpty())
			throw new IllegalStateException(
					"For test runs, select *either* project or advice but not both.");

		if (!selectedAdvice.isEmpty())
			try {
				this.analyze(selectedAdvice, lMonitor);
			}
			catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		else if (!selectedProjectCol.isEmpty()) {
			PrintWriter benchmarkOut = null;
			try {
				benchmarkOut = AnalyzePointcut.getBenchmarkStatsWriter();
			}
			catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			benchmarkOut.println("Benchmark\t#Advice\t#Shadows\tTime(s)");
			for (final IJavaProject proj : selectedProjectCol) {
				final long start = System.currentTimeMillis();
				Collection<? extends AdviceElement> toAnalyze = null;
				try {
					toAnalyze = Util.extractValidAdviceElements(proj);
					if (!toAnalyze.isEmpty())
						this.analyze(toAnalyze, lMonitor);
				}
				catch (final JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				catch (final Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new RuntimeException(e);
				}

				int numShadows = 0;
				if (!toAnalyze.isEmpty())
					numShadows = this.getTotalNumberOfShadows(proj);

				uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.TimeColletor collector = uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.TimeColletor
						.aspectOf();

				final long elapsed = System.currentTimeMillis()
						- (start + collector.getCollectedTime());
				collector.clear();
				final int secs = (int) elapsed / 1000;

				benchmarkOut.print(proj.getProject().getName() + "\t");
				benchmarkOut.print(toAnalyze.size() + "\t");
				benchmarkOut.print(numShadows + "\t");
				benchmarkOut.print(secs + "\t");
				benchmarkOut.println();
			}
			benchmarkOut.close();
		}

		lMonitor.done();
	}

	public void selectionChanged(final IAction action,
			final ISelection selection) {
		if (selection instanceof IStructuredSelection)
			this.aSelection = (IStructuredSelection) selection;

	}

	@SuppressWarnings( { "unchecked", "restriction" })
	private void analyze(final Collection<? extends AdviceElement> adviceCol,
			final IProgressMonitor lMonitor) throws Exception {
		final JayFX lDB = new JayFX();

		final Collection<IProject> projectsToAnalyze = Util
				.getProjects(adviceCol);

		lDB.initialize(projectsToAnalyze, lMonitor, true);
		final IntentionGraph<IntentionNode<IElement>> graph = new IntentionGraph<IntentionNode<IElement>>(
				lDB, lMonitor);

		lMonitor.subTask("Loading up the rulebase.");
		final Reader source = new InputStreamReader(AnalyzePointcut.class
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

		final PrintWriter patternOut = AnalyzePointcut.getPatternStatsWriter();
		patternOut
				.println("Benchmark\tAdvice#\tAdvice\tPattern\tSize\tPrecision\tConcreteness");

		lMonitor.beginTask("Enabling graph elements for each selected advice.",
				adviceCol.size());
		int pointcut_count = 0;

		for (final AdviceElement advElem : adviceCol) {

			final Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap = new LinkedHashMap<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>>();

			final Map<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap = new LinkedHashMap<Path<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>>();

			graph.enableElementsAccordingTo(advElem, lMonitor);

			AnalyzePointcut.executeNodeQuery(new SubProgressMonitor(lMonitor,
					1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
					workingMemory, patternToResultMap,
					patternToEnabledElementMap,
					"forward suggested execution nodes");

			AnalyzePointcut.executeNodeQuery(new SubProgressMonitor(lMonitor,
					1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
					workingMemory, patternToResultMap,
					patternToEnabledElementMap,
					"backward suggested execution nodes");

			AnalyzePointcut.executeArcQuery("forward suggested X arcs",
					Relation.CALLS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			AnalyzePointcut.executeArcQuery("backward suggested X arcs",
					Relation.CALLS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			AnalyzePointcut.executeArcQuery("forward suggested X arcs",
					Relation.GETS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			AnalyzePointcut.executeArcQuery("backward suggested X arcs",
					Relation.GETS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			AnalyzePointcut.executeArcQuery("forward suggested X arcs",
					Relation.SETS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			AnalyzePointcut.executeArcQuery("backward suggested X arcs",
					Relation.SETS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			for (final Path pattern : patternToResultMap.keySet()) {
				final double precision = AnalyzePointcut.calculatePrecision(
						patternToEnabledElementMap.get(pattern),
						patternToResultMap.get(pattern));

				final double concreteness = AnalyzePointcut
						.calculateConcreteness(pattern);

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
			pointcut_count++;
			lMonitor.worked(1);
		}
		patternOut.close();
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	private int getTotalNumberOfShadows(final IJavaProject proj) {
		final List<AJRelationship> relationshipList = AJModel
				.getInstance()
				.getAllRelationships(
						proj.getProject(),
						new AJRelationshipType[] { AJRelationshipManager.ADVISES });
		return relationshipList.size();
	}
}