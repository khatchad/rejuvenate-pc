/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.drools.WorkingMemory;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionEdge;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;
import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;

/**
 * @author raffi
 * 
 */
public class RejuvenatePointcutPlugin extends PointcutPlugin {

	/**
	 * 
	 */
	private static final String PATH = "path";
	/**
	 * 
	 */
	private static final String PATTERN = "Pattern";

	public void run(IAction action) {
		final IProgressMonitor monitor = getProgressMonitor();
		final Collection<AdviceElement> selectedAdvice = this
				.getSelectedAdvice();

		if (!selectedAdvice.isEmpty())
			analyzeAdvice(monitor, selectedAdvice);

		monitor.done();
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.PointcutPlugin#analyzeAdviceCollection(java.util.Collection, org.eclipse.core.runtime.IProgressMonitor, uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph, org.drools.WorkingMemory, java.io.PrintWriter)
	 */
	@SuppressWarnings("restriction")
	@Override
	protected void analyzeAdviceCollection(
			Collection<? extends AdviceElement> adviceCol,
			IProgressMonitor monitor,
			IntentionGraph<IntentionNode<IElement>> graph,
			WorkingMemory workingMemory) throws ConversionException,
			CoreException, IOException, JDOMException {

		monitor.beginTask("Retrieving previously analyzed information.",
				adviceCol.size());

		int pointcutCount = 0;
		for (final AdviceElement advElem : adviceCol) {
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap = new LinkedHashMap<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>>();
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap = new LinkedHashMap<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>>();

			//retrieve analysis information.
			Document document = readXMLFile(advElem);
			Collection<IJavaElement> advisedElements = extractAdvisedElements(document);

			graph.enableElementsAccordingTo(advisedElements,
					new SubProgressMonitor(monitor, -1));
			Util.makeDotFile(graph, pointcutCount, Util.WORKSPACE_LOC
					+ advElem.getPath().toOSString() + "-");

			buildPatternMaps(monitor, graph, workingMemory, advElem,
					patternToResultMap, patternToEnabledElementMap);

			//Intersect pattern sets.
			Map<Pattern<IntentionEdge<IElement>>, Double> extractedPatternToConfidenceMap = extractPatterns(document);
			Set<Pattern<IntentionEdge<IElement>>> derivedPatternSet = patternToResultMap
					.keySet();
			//			Set<Pattern<IntentionEdge<IElement>>> retrievedPatternSet = new LinkedHashSet<Pattern<IntentionEdge<IElement>>>(
			//					extractedPatternSet);
			//			retrievedPatternSet.retainAll(derivedPatternSet);

			//TODO: Need to make suggestions.
			//			for (Pattern<IntentionEdge<IElement>> pattern : retrievedPatternSet) {
			//TODO: Use the maps here.
			//			}

			//			SortedMap<IJavaElement, Double> suggestions = null;// obtainSuggestions()

			monitor.worked(1);
		}
	}

	/**
	 * @param document
	 * @return
	 * @throws DataConversionException 
	 */
	private Map<Pattern<IntentionEdge<IElement>>, Double> extractPatterns(
			Document document) throws DataConversionException {
		Map<Pattern<IntentionEdge<IElement>>, Double> ret = new LinkedHashMap<Pattern<IntentionEdge<IElement>>, Double>();
		
		Element root = document.getRootElement();
		for (Object patternObj : root.getChildren(PATTERN)) {
			Element patternElem = (Element) patternObj;
			Pattern<IntentionEdge<IElement>> pattern = new Pattern<IntentionEdge<IElement>>(patternElem);
			Attribute confidenceAttribute = patternElem.getAttribute("confidence");
			double confidence = confidenceAttribute.getDoubleValue();
			ret.put(pattern, confidence);
		}
		return ret;
	}

	/**
	 * @param document
	 * @return
	 */
	private Collection<IJavaElement> extractAdvisedElements(Document document) {
		// TODO Auto-generated method stub
		Collection<IJavaElement> ret = new LinkedHashSet<IJavaElement>();
		Element root = document.getRootElement();
		Element advisedElements = root.getChild("advisedElements");
		for (Object obj : advisedElements.getChildren()) {
			Element elem = (Element) obj;
			String id = elem.getAttributeValue("id");
			IJavaElement jElem = AspectJCore.create(id);
			ret.add(jElem);
		}
		return ret;
	}

	/**
	 * @param advElem
	 * @return
	 * @throws IOException
	 * @throws JDOMException
	 */
	private Document readXMLFile(AdviceElement advElem) throws JDOMException,
			IOException {
		org.jdom.input.SAXBuilder builder = new SAXBuilder();
		File advXMLFile = Util.getSavedXMLFile(advElem);
		return builder.build(advXMLFile);
	}
}