package uk.ac.lancs.comp.khatchad.rejuvenatepc.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
import org.eclipse.jdt.core.JavaModelException;
import org.jdom.Element;
import org.jdom.JDOMException;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionArc;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Path;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.AJUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.FileUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.XMLUtil;
import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.JayFXException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

public abstract class PointcutProcessor {
	
	/**
	 * 
	 */
	private static final String SUGGESTEDLEMENTS = "suggestedlements";
	/**
	 * 
	 */
	private static final String ENABLED_ELEMENTS = "enabledElements";
	/**
	 * 
	 */
	private static final String RULES_FILE = "/rules/NodeRules.drl";
	/**
	 * 
	 */
	private static final String ADVISED_ELEMENTS = "advisedElements";
	/**
	 * 
	 */
	private static final String CONFIDENCE = "confidence";
	
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
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap,
			final IProgressMonitor lMonitor) {

		final QueryResults suggestedArcs = workingMemory.getQueryResults(
				queryString, new Object[] { relation });

		lMonitor.beginTask("Executing query: "
				+ queryString.replace("X", relation.toString()) + ".",
				suggestedArcs.size());
		for (final Iterator it = suggestedArcs.iterator(); it.hasNext();) {
			final QueryResult result = (QueryResult) it.next();
			final IntentionArc suggestedArc = (IntentionArc) result
					.get("$suggestedArc");

			final IntentionArc enabledArc = (IntentionArc) result
					.get("$enabledArc");

			final Path enabledPath = (Path) result.get("$enabledPath");

			final IntentionNode commonNode = (IntentionNode) result
					.get("$commonNode");
			final Pattern pattern = enabledPath.extractPattern(commonNode,
					enabledArc);

			if (!patternToResultMap.containsKey(pattern))
				patternToResultMap.put(pattern,
						new LinkedHashSet<IntentionElement<IElement>>());
			patternToResultMap.get(pattern).add(suggestedArc);

			if (!patternToEnabledElementMap.containsKey(pattern))
				patternToEnabledElementMap.put(pattern,
						new LinkedHashSet<IntentionElement<IElement>>());
			patternToEnabledElementMap.get(pattern).add(enabledArc);

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
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap,
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
	 * @param selectedAdvice
	 * @param lMonitor
	 */
	public void analyzeAdvice(final Collection<AdviceElement> selectedAdvice,
			final IProgressMonitor lMonitor) {
		try {
			this.analyze(selectedAdvice, lMonitor);
		}
		catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings( { "unchecked", "restriction" })
	public void analyze(final Collection<? extends AdviceElement> adviceCol,
			final IProgressMonitor lMonitor) throws Exception {
		final IntentionGraph graph = generateIntentionGraph(adviceCol, lMonitor);

		final WorkingMemory workingMemory = generateRulesBase(lMonitor, graph);

		analyzeAdviceCollection(adviceCol, lMonitor, graph, workingMemory);
	}

	protected abstract void analyzeAdviceCollection(
			final Collection<? extends AdviceElement> adviceCol,
			final IProgressMonitor lMonitor, final IntentionGraph graph,
			final WorkingMemory workingMemory) throws ConversionException,
			CoreException, IOException, JDOMException;
	
	/**
	 * @param lMonitor
	 * @param graph
	 * @return
	 * @throws Exception 
	 * @throws Exception
	 */
	private WorkingMemory generateRulesBase(final IProgressMonitor lMonitor,
			final IntentionGraph graph) throws Exception {
		final WorkingMemory workingMemory = loadRulesBase(lMonitor, graph);
		fireRules(lMonitor, workingMemory);
		return workingMemory;
	}
	
	/**
	 * @param pointcutCount
	 * @param advElem
	 * @param adviceXMLElement
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 * @param pattern
	 * @return
	 * @throws IOException
	 * @throws JavaModelException 
	 */
	protected double calculatePatternStatistics(
			int pointcutCount,
			final AdviceElement advElem,
			Element adviceXMLElement,
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap,
			final Pattern pattern, IntentionGraph graph) throws IOException, JavaModelException {

		double precision = Pattern.calculatePrecision(
				patternToEnabledElementMap.get(pattern), patternToResultMap
						.get(pattern));

		double coverage = patternToEnabledElementMap.get(pattern).size() / graph.getEnabledElements().size();

		double concreteness = Pattern.calculateConcreteness(pattern);
		
		double confidence = Pattern
				.calculateConfidence(precision, coverage, concreteness);

		Element patternXMLElement = getPatternXMLElement(pattern, confidence);

		//enabled elements.
		Element enabledElementsXMLElement = getXML(patternToEnabledElementMap
				.get(pattern), ENABLED_ELEMENTS);
		patternXMLElement.addContent(enabledElementsXMLElement);

//		printEnabledElementResults(pattern, pointcutCount, advElem,
//				patternToEnabledElementMap.get(pattern));

		//suggestions.
		Element suggestedElementsXML = getXML(patternToResultMap.get(pattern),
				SUGGESTEDLEMENTS);
		patternXMLElement.addContent(suggestedElementsXML);

//		printSuggestedElementResults(pattern, pointcutCount, advElem,
//				patternToResultMap.get(pattern));

		adviceXMLElement.addContent(patternXMLElement);

//		printPatternResults(pointcutCount, advElem, pattern, patternToResultMap
//				.get(pattern).size(), patternToEnabledElementMap.get(pattern)
//				.size(), precision, concreteness, confidence);

		return confidence;
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
			if (enabledElement instanceof IntentionArc)
				ret.addContent(((IntentionArc) enabledElement).getXML());
			else
				ret.addContent(enabledElement.getXML());
		}
		return ret;
	}
	
	/**
	 * @param adviceXMLElement
	 * @param pattern
	 * @param confidence
	 */
	private static Element getPatternXMLElement(final Pattern pattern,
			double confidence) {
		Element patternXMLElement = pattern.getXML();
		patternXMLElement.setAttribute(CONFIDENCE, String.valueOf(confidence));
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
		Element ret = XMLUtil.getXML(advElem);
		Element advisedElementXML = getAdvisedJavaElementsXMLElement(
				adviceXMLElement, AJUtil.getAdvisedJavaElements(advElem));
		ret.addContent(advisedElementXML);
		return ret;
	}
	
	/**
	 * @param lMonitor
	 * @param workingMemory
	 * @param patternToResultMap
	 * @param patternToEnabledElementMap
	 */
	protected void executeQueries(
			final IProgressMonitor lMonitor,
			final WorkingMemory workingMemory,
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap) {
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
	 * @throws Exception
	 */
	private WorkingMemory loadRulesBase(final IProgressMonitor lMonitor,
			final IntentionGraph graph) throws Exception {
		lMonitor.subTask("Loading up the rulebase.");
		final Reader source = new InputStreamReader(PointcutProcessor.class
				.getResourceAsStream(RULES_FILE));
		final RuleBase ruleBase = FileUtil.readRule(source);
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
	protected IntentionGraph generateIntentionGraph(
			final Collection<? extends AdviceElement> adviceCol,
			final IProgressMonitor lMonitor) throws JayFXException,
			ConversionException, JavaModelException {
		final JayFX lDB = new JayFX();

		final Collection<IProject> projectsToAnalyze = AJUtil
				.getProjects(adviceCol);

		lDB.initialize(projectsToAnalyze, lMonitor, true);
		final IntentionGraph graph = new IntentionGraph(lDB, lMonitor);
		return graph;
	}
	
	/**
	 * @param adviceXMLElement
	 * @param advisedJavaElements
	 */
	private static Element getAdvisedJavaElementsXMLElement(
			Element adviceXMLElement, Set<IJavaElement> advisedJavaElements) {
		Element ret = new Element(ADVISED_ELEMENTS);
		for (IJavaElement jElem : advisedJavaElements) {
			Element xmlElem = XMLUtil.getXML(jElem);
			ret.addContent(xmlElem);
		}
		return ret;
	}
}
