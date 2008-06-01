/**
 * 
 */
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.drools.FactHandle;
import org.drools.QueryResult;
import org.drools.QueryResults;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.jdom.Attribute;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionEdge;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Path;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;
import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.JayFXException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public abstract class PointcutPlugin implements IWorkbenchWindowActionDelegate {

	protected static final String RESULT_PATH = new File(ResourcesPlugin
			.getWorkspace().getRoot().getLocation().toOSString()
			+ File.separator + "results").getPath()
			+ File.separator;

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

			final IntentionEdge enabledEdge = (IntentionEdge) result
					.get("$enabledEdge");

			final Path enabledPath = (Path) result.get("$enabledPath");

			final IntentionNode commonNode = (IntentionNode) result
					.get("$commonNode");
			final Pattern pattern = enabledPath.extractPattern(commonNode,
					enabledEdge);

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
			final Pattern pattern = enabledPath.extractPattern(commonNode,
					enabledNode);

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

	/**
	 * @return
	 * @throws IOException
	 */
	protected static PrintWriter getPrintWriter(final File aFile,
			final boolean append) throws IOException {
		final FileWriter resFileOut = new FileWriter(aFile, append);
		return new PrintWriter(resFileOut);
	}

	private static PrintWriter getPatternStatsWriter() throws IOException {
		final File aFile = new File(PointcutPlugin.RESULT_PATH + "patterns.csv");
		return AnalyzePointcutPlugin.getPrintWriter(aFile, true);
	}

	@SuppressWarnings("restriction")
	private static PrintWriter getXMLFileWriter(AdviceElement advElem)
			throws IOException {
		StringBuilder fileNameBuilder = new StringBuilder(advElem.getPath()
				.toOSString());
		fileNameBuilder.append("#" + advElem.toDebugString());
		fileNameBuilder.append(".rejuv-pc.xml");

		final File aFile = new File(Util.WORKSPACE_LOC, fileNameBuilder
				.toString());
		return AnalyzePointcutPlugin.getPrintWriter(aFile, false);
	}

	/**
	 * The selected item on the workbench.
	 */
	private IStructuredSelection aSelection;

	/**
	 * @return
	 * @throws IOException
	 */
	private static PrintWriter generatePatternStatsWriter() throws IOException {
		final PrintWriter patternOut = getPatternStatsWriter();
		patternOut
				.println("Benchmark\tAdvice#\tAdvice\tPattern\tSize\tPrecision\tConcreteness\tConfidence");
		return patternOut;
	}

	public void dispose() {
	}

	/**
	 * Make the result folder on startup.
	 */
	public void init(final IWorkbenchWindow lWindow) {
		final File resultFolder = new File(PointcutPlugin.RESULT_PATH);
		if (!resultFolder.exists())
			resultFolder.mkdir();
	}

	/**
	 * @param lMonitor
	 * @param selectedAdvice
	 */
	protected void analyzeAdvice(final IProgressMonitor lMonitor,
			final Collection<AdviceElement> selectedAdvice) {
		try {
			this.analyze(selectedAdvice, lMonitor);
		}
		catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void selectionChanged(final IAction action,
			final ISelection selection) {
		if (selection instanceof IStructuredSelection)
			this.aSelection = (IStructuredSelection) selection;

	}

	@SuppressWarnings( { "unchecked", "restriction" })
	protected void analyze(final Collection<? extends AdviceElement> adviceCol,
			final IProgressMonitor lMonitor) throws Exception {
		final IntentionGraph<IntentionNode<IElement>> graph = generateIntentionGraph(
				adviceCol, lMonitor);

		final WorkingMemory workingMemory = generateRulesBase(lMonitor, graph);

		final PrintWriter patternOut = generatePatternStatsWriter();

		analyzeAdviceCollection(adviceCol, lMonitor, graph, workingMemory,
				patternOut);

		patternOut.close();
	}

	protected abstract void analyzeAdviceCollection(
			final Collection<? extends AdviceElement> adviceCol,
			final IProgressMonitor lMonitor,
			final IntentionGraph<IntentionNode<IElement>> graph,
			final WorkingMemory workingMemory, final PrintWriter patternOut)
			throws ConversionException, CoreException, IOException;

	/**
	 * @param lMonitor
	 * @param graph
	 * @return
	 * @throws Exception
	 */
	private WorkingMemory generateRulesBase(final IProgressMonitor lMonitor,
			final IntentionGraph<IntentionNode<IElement>> graph)
			throws Exception {
		final WorkingMemory workingMemory = loadRulesBase(lMonitor, graph);
		fireRules(lMonitor, workingMemory);
		return workingMemory;
	}

	/**
	 * @param patternOut
	 * @param pointcut_count
	 * @param advElem
	 * @param adviceXMLElement
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 * @param pattern
	 */
	protected void calculatePatternStatistics(
			final PrintWriter patternOut,
			int pointcut_count,
			final AdviceElement advElem,
			Element adviceXMLElement,
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap,
			final Pattern pattern) {

		double precision = Pattern.calculatePrecision(
				patternToEnabledElementMap.get(pattern), patternToResultMap
						.get(pattern));
		double concreteness = Pattern.calculateConcreteness(pattern);
		double confidence = Pattern
				.calculateConfidence(precision, concreteness);

		Element patternXMLElement = getPatternXMLElement(pattern, confidence);

		//enabled elements.
		Element enabledElementsXMLElement = getXML(patternToEnabledElementMap
				.get(pattern), "enabledElements");
		patternXMLElement.addContent(enabledElementsXMLElement);

		//suggestions.
		Element suggestedElementsXML = getXML(patternToResultMap.get(pattern),
				"suggestedElements");
		patternXMLElement.addContent(suggestedElementsXML);

		adviceXMLElement.addContent(patternXMLElement);

		printPatternResults(patternOut, pointcut_count, advElem, pattern,
				precision, concreteness, confidence);
	}

	/**
	 * @param patternToEnabledElementMap
	 * @param pattern
	 * @return
	 */
	private Element getXML(final Set<IntentionElement<IElement>> set,
			String elementName) {
		Element ret = new Element(elementName);
		for (IntentionElement<IElement> enabledElement : set) {
			if (enabledElement instanceof IntentionEdge)
				ret.addContent(((IntentionEdge) enabledElement)
						.getXMLWithTargetNode());
			else
				ret.addContent(enabledElement.getXML());
		}
		return ret;
	}

	/**
	 * 
	 */
	public PointcutPlugin() {
		super();
	}

	/**
	 * @param advElem
	 * @param adviceXMLElement
	 * @throws IOException
	 */
	protected void writeXMLFile(final AdviceElement advElem,
			Element adviceXMLElement) throws IOException {
		DocType type = new DocType(this.getClass().getSimpleName());
		Document doc = new Document(adviceXMLElement, type);
		XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
		PrintWriter xmlOut = getXMLFileWriter(advElem);
		serializer.output(doc, xmlOut);
		xmlOut.close();
	}

	/**
	 * @param patternOut
	 * @param pointcut_count
	 * @param advElem
	 * @param pattern
	 * @param precision
	 * @param concreteness
	 * @param confidence
	 */
	private void printPatternResults(final PrintWriter patternOut,
			int pointcut_count, final AdviceElement advElem,
			final Pattern pattern, double precision, double concreteness,
			double confidence) {
		patternOut
				.print(advElem.getJavaProject().getProject().getName() + "\t");
		patternOut.print(pointcut_count + "\t");
		patternOut.print(advElem.readableName() + "\t");
		patternOut.print(pattern + "\t");
		patternOut.print(pattern.size() + "\t");
		patternOut.print(precision + "\t");
		patternOut.print(concreteness + "\t");
		patternOut.print(confidence + "\t");
		patternOut.println();
	}
	
	private void printSuggestionResults(PrintWriter out) {
		//TODO
	}

	/**
	 * @param adviceXMLElement
	 * @param pattern
	 * @param confidence
	 */
	private static Element getPatternXMLElement(final Pattern pattern,
			double confidence) {
		Element patternXMLElement = pattern.getXML();
		patternXMLElement
				.setAttribute("confidence", String.valueOf(confidence));
		return patternXMLElement;
	}

	/**
	 * @param advElem
	 * @return
	 * @throws JavaModelException
	 */
	protected static Element createAdviceXMLElement(final AdviceElement advElem)
			throws JavaModelException {
		Element adviceXMLElement = new Element(AdviceElement.class
				.getSimpleName());
		Element ret = Util.getXML(advElem);
		Element advisedElementXML = getAdvisedJavaElements(adviceXMLElement,
				Util.getAdvisedJavaElements(advElem));
		ret.addContent(advisedElementXML);
		return ret;
	}

	/**
	 * @param lMonitor
	 * @param graph
	 * @param workingMemory
	 * @param advElem
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 * @throws ConversionException
	 * @throws CoreException
	 */
	protected void buildPatternMaps(
			final IProgressMonitor lMonitor,
			final IntentionGraph<IntentionNode<IElement>> graph,
			final WorkingMemory workingMemory,
			final AdviceElement advElem,
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap)
			throws ConversionException, CoreException {
		graph.enableElementsAccordingTo(advElem, lMonitor);
		executeQueries(lMonitor, workingMemory, patternToResultMap,
				patternToEnabledElementMap);
	}

	/**
	 * @param lMonitor
	 * @param workingMemory
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 */
	private void executeQueries(
			final IProgressMonitor lMonitor,
			final WorkingMemory workingMemory,
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap) {
		executeNodeQuery(new SubProgressMonitor(lMonitor, 1,
				SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
				workingMemory, patternToResultMap, patternToEnabledElementMap,
				"forward suggested execution nodes");

		executeNodeQuery(new SubProgressMonitor(lMonitor, 1,
				SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK),
				workingMemory, patternToResultMap, patternToEnabledElementMap,
				"backward suggested execution nodes");

		executeArcQuery("forward suggested X arcs", Relation.CALLS,
				workingMemory, patternToResultMap, patternToEnabledElementMap,
				new SubProgressMonitor(lMonitor, 1,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

		executeArcQuery("backward suggested X arcs", Relation.CALLS,
				workingMemory, patternToResultMap, patternToEnabledElementMap,
				new SubProgressMonitor(lMonitor, 1,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

		executeArcQuery("forward suggested X arcs", Relation.GETS,
				workingMemory, patternToResultMap, patternToEnabledElementMap,
				new SubProgressMonitor(lMonitor, 1,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

		executeArcQuery("backward suggested X arcs", Relation.GETS,
				workingMemory, patternToResultMap, patternToEnabledElementMap,
				new SubProgressMonitor(lMonitor, 1,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

		executeArcQuery("forward suggested X arcs", Relation.SETS,
				workingMemory, patternToResultMap, patternToEnabledElementMap,
				new SubProgressMonitor(lMonitor, 1,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));

		executeArcQuery("backward suggested X arcs", Relation.SETS,
				workingMemory, patternToResultMap, patternToEnabledElementMap,
				new SubProgressMonitor(lMonitor, 1,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
	}

	/**
	 * @param lMonitor
	 * @param workingMemory
	 */
	private void fireRules(final IProgressMonitor lMonitor,
			final WorkingMemory workingMemory) {
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
	}

	/**
	 * @param lMonitor
	 * @param graph
	 * @return
	 * @throws Exception
	 */
	private WorkingMemory loadRulesBase(final IProgressMonitor lMonitor,
			final IntentionGraph<IntentionNode<IElement>> graph)
			throws Exception {
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
		return workingMemory;
	}

	/**
	 * @param adviceCol
	 * @param lMonitor
	 * @return
	 * @throws JayFXException
	 * @throws ConversionException
	 * @throws JavaModelException
	 * @throws Exception
	 */
	protected IntentionGraph<IntentionNode<IElement>> generateIntentionGraph(
			final Collection<? extends AdviceElement> adviceCol,
			final IProgressMonitor lMonitor) throws JayFXException,
			ConversionException, JavaModelException, Exception {
		final JayFX lDB = new JayFX();

		final Collection<IProject> projectsToAnalyze = Util
				.getProjects(adviceCol);

		lDB.initialize(projectsToAnalyze, lMonitor, true);
		final IntentionGraph<IntentionNode<IElement>> graph = new IntentionGraph<IntentionNode<IElement>>(
				lDB, lMonitor);
		return graph;
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
			if (lNext instanceof IJavaProject)
				ret.add((IJavaProject) lNext);
		}

		return ret;
	}

	/**
	 * @return
	 */
	protected IProgressMonitor getProgressMonitor() {
		final IProgressMonitor lMonitor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getViewReferences()[0]
				.getView(true).getViewSite().getActionBars()
				.getStatusLineManager().getProgressMonitor();
		return lMonitor;
	}

	/**
	 * @param adviceXMLElement
	 * @param advisedJavaElements
	 */
	private static Element getAdvisedJavaElements(Element adviceXMLElement,
			Set<IJavaElement> advisedJavaElements) {
		Element ret = new Element("advisedElements");
		for (IJavaElement jElem : advisedJavaElements) {
			Element xmlElem = Util.getXML(jElem);
			ret.addContent(xmlElem);
		}
		return ret;
	}

}