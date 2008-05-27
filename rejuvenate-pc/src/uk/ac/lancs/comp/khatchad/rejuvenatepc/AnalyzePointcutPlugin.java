package uk.ac.lancs.comp.khatchad.rejuvenatepc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
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
import java.util.Map.Entry;

import org.drools.FactHandle;
import org.drools.QueryResult;
import org.drools.QueryResults;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
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
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionEdge;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Path;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

public class AnalyzePointcutPlugin implements IWorkbenchWindowActionDelegate {

	private static final String RESULT_PATH = new File(ResourcesPlugin
			.getWorkspace().getRoot().getLocation().toOSString()
			+ File.separator + "results").getPath()
			+ File.separator;

	private static final String DATABASE_FILE_NAME = "rejuv-pc.dat";

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
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap,
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
			final Pattern pattern = enabledPath.extractPattern(commonNode);

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
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap,
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
			final Pattern pattern = enabledPath.extractPattern(commonNode);

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
		final File aFile = new File(AnalyzePointcutPlugin.RESULT_PATH
				+ "advice.csv");
		return AnalyzePointcutPlugin.getPrintWriter(aFile, true);
	}

	private static PrintWriter getBenchmarkStatsWriter() throws IOException {
		final File aFile = new File(AnalyzePointcutPlugin.RESULT_PATH
				+ "benchmarks.csv");
		return AnalyzePointcutPlugin.getPrintWriter(aFile, true);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private static PrintWriter getPrintWriter(final File aFile,
			final boolean append) throws IOException {
		final FileWriter resFileOut = new FileWriter(aFile, append);
		return new PrintWriter(resFileOut);
	}

	private static PrintWriter getPatternStatsWriter() throws IOException {
		final File aFile = new File(AnalyzePointcutPlugin.RESULT_PATH
				+ "patterns.csv");
		return AnalyzePointcutPlugin.getPrintWriter(aFile, true);
	}
	
	private static PrintWriter getXMLFileWriter() throws IOException {
		final File aFile = new File(Util.WORKSPACE_LOC,
				AnalyzePointcutPlugin.class.getSimpleName() + ".xml");
		return AnalyzePointcutPlugin.getPrintWriter(aFile, false);
	}

	@SuppressWarnings("unused")
	private static PrintWriter getSuggestionWriter() throws IOException {
		final File aFile = new File(AnalyzePointcutPlugin.RESULT_PATH
				+ "suggestion.csv");
		return AnalyzePointcutPlugin.getPrintWriter(aFile, true);
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
		final File resultFolder = new File(AnalyzePointcutPlugin.RESULT_PATH);
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
				benchmarkOut = AnalyzePointcutPlugin.getBenchmarkStatsWriter();
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
		final Reader source = new InputStreamReader(AnalyzePointcutPlugin.class
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

		//		ObjectContainer databaseConn = Util.getDatabaseConnection(new File(
		//				Util.WORKSPACE_LOC, DATABASE_FILE_NAME));

		final PrintWriter patternOut = AnalyzePointcutPlugin
				.getPatternStatsWriter();
		patternOut
				.println("Benchmark\tAdvice#\tAdvice\tPattern\tSize\tPrecision\tConcreteness\tConfidence");

		lMonitor.beginTask("Enabling graph elements for each selected advice.",
				adviceCol.size());
		int pointcut_count = 0;

		Element root = new Element(this.getClass().getSimpleName());
		for (final AdviceElement advElem : adviceCol) {

			Element adviceXMLElement = new Element(AdviceElement.class.getSimpleName());
			adviceXMLElement.setAttribute("id", advElem.getHandleIdentifier());

			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap = new LinkedHashMap<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>>();

			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap = new LinkedHashMap<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>>();

			graph.enableElementsAccordingTo(advElem, lMonitor);

			AnalyzePointcutPlugin.executeNodeQuery(new SubProgressMonitor(
					lMonitor, 1,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
					workingMemory, patternToResultMap,
					patternToEnabledElementMap,
					"forward suggested execution nodes");

			AnalyzePointcutPlugin.executeNodeQuery(new SubProgressMonitor(
					lMonitor, 1,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
					workingMemory, patternToResultMap,
					patternToEnabledElementMap,
					"backward suggested execution nodes");

			AnalyzePointcutPlugin.executeArcQuery("forward suggested X arcs",
					Relation.CALLS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			AnalyzePointcutPlugin.executeArcQuery("backward suggested X arcs",
					Relation.CALLS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			AnalyzePointcutPlugin.executeArcQuery("forward suggested X arcs",
					Relation.GETS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			AnalyzePointcutPlugin.executeArcQuery("backward suggested X arcs",
					Relation.GETS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			AnalyzePointcutPlugin.executeArcQuery("forward suggested X arcs",
					Relation.SETS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			AnalyzePointcutPlugin.executeArcQuery("backward suggested X arcs",
					Relation.SETS, workingMemory, patternToResultMap,
					patternToEnabledElementMap, new SubProgressMonitor(
							lMonitor, 1,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

			for (final Pattern pattern : patternToResultMap.keySet()) {

				double precision = Pattern.calculatePrecision(
						patternToEnabledElementMap.get(pattern),
						patternToResultMap.get(pattern));
				double concreteness = Pattern.calculateConcreteness(pattern);
				double confidence = Pattern.calculateConfidence(precision,
						concreteness);

				Element patternXMLElement = pattern.getXML();
				patternXMLElement.setAttribute("confidence", String
						.valueOf(confidence));
				adviceXMLElement.addContent(patternXMLElement);

				patternOut.print(advElem.getJavaProject().getProject()
						.getName()
						+ "\t");
				patternOut.print(pointcut_count + "\t");
				patternOut.print(advElem.readableName() + "\t");
				patternOut.print(pattern + "\t");
				patternOut.print(pattern.size() + "\t");
				patternOut.print(precision + "\t");
				patternOut.print(concreteness + "\t");
				patternOut.print(confidence + "\t");
				patternOut.println();
			}
			root.addContent(adviceXMLElement);
			pointcut_count++;
			lMonitor.worked(1);
		}
		
		DocType type = new DocType(this.getClass().getSimpleName());
		Document doc = new Document(root, type);
		XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
		serializer.output(doc, System.out);
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