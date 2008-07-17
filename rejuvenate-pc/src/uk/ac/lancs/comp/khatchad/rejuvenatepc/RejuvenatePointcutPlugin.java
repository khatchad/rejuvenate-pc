/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc;

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

import org.aspectj.asm.AsmManager;
import org.drools.WorkingMemory;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
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

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionArc;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.model.Suggestion;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.AJUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.DatabaseUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.XMLUtil;
import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.ubc.cs.pointcutdoctor.core.virtual.VirtualShadowUtil;

/**
 * @author raffi
 * 
 */
public class RejuvenatePointcutPlugin extends PointcutRefactoringPlugin
		implements IStructuredContentProvider {

	private static RejuvenatePointcutPlugin instance;

	/**
	 * 
	 */
	private static final String PATH = "path";
	/**
	 * 
	 */
	private static final String PATTERN = "Pattern";

	private List<Suggestion<IJavaElement>> suggestionList = new ArrayList<Suggestion<IJavaElement>>();

	@Override
	protected void run(IProgressMonitor monitor) {
		final long start = System.currentTimeMillis();

		this.suggestionList.clear();
		final Collection<AdviceElement> selectedAdvice = this
				.getSelectedAdvice();

		if (!selectedAdvice.isEmpty())
			analyzeAdvice(selectedAdvice, monitor);

		final int secs = calculateTimeStatistics(start);
		System.err.println("Time (s): " + secs);
		monitor.done();
	}

	@Override
	public void run(IAction action) {
		final IProgressMonitor monitor = getProgressMonitor();
		this.run(monitor);
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.PointcutRefactoringPlugin#analyzeAdviceCollection(java.util.Collection, org.eclipse.core.runtime.IProgressMonitor, uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph, org.drools.WorkingMemory, java.io.PrintWriter)
	 */
	@SuppressWarnings("restriction")
	@Override
	protected void analyzeAdviceCollection(
			Collection<? extends AdviceElement> adviceCol,
			IProgressMonitor monitor, IntentionGraph graph,
			WorkingMemory workingMemory) throws ConversionException,
			CoreException, IOException, JDOMException {

		monitor.beginTask("Retrieving previously analyzed information.",
				adviceCol.size());

		int pointcutCount = 0;
		for (final AdviceElement advElem : adviceCol) {

			String adviceKey = DatabaseUtil.getKey(advElem);
			try {
				DatabaseUtil.insertAdviceIntoDatabase(adviceKey);
			}
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> derivedPatternToResultMap = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>>();
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> derivedPatternToEnabledElementMap = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>>();

			graph.enableAllElements(new SubProgressMonitor(monitor, -1));

			executeQueries(monitor, workingMemory, derivedPatternToResultMap,
					derivedPatternToEnabledElementMap);

			//retrieve analysis information.
			Document document = readXMLFile(advElem);

			//Retrieve the saved patterns.
			Map<Pattern<IntentionArc<IElement>>, Double> recoveredPatternToConfidenceMap = extractPatterns(document);

			//Intersect pattern sets.
			Set<Pattern<IntentionArc<IElement>>> survingPatternSet = obtainSurvingPatterns(
					derivedPatternToResultMap, recoveredPatternToConfidenceMap);

			//Make suggestions sorted by highest confidence.
			SortedMap<Double, Set<IJavaElement>> confidenceToSuggestedJavaElementSetMap = new TreeMap<Double, Set<IJavaElement>>(
					new Comparator<Double>() {
						public int compare(Double o1, Double o2) {
							return o1.compareTo(o2) * -1;
						}
					});

			Collection<IJavaElement> advisedElements = extractAdvisedElements(document);
			graph.enableElementsAccordingTo(advisedElements,
					new SubProgressMonitor(monitor, -1));

			//			System.out.println("Suggestion\tPattern\tConfidence");
			for (Pattern<IntentionArc<IElement>> pattern : survingPatternSet) {
				//Get the confidence.
				double confidence = recoveredPatternToConfidenceMap
						.get(pattern);

				if (!confidenceToSuggestedJavaElementSetMap
						.containsKey(confidence)) {
					confidenceToSuggestedJavaElementSetMap.put(confidence,
							new LinkedHashSet<IJavaElement>());
				}
				Set<IJavaElement> suggestedJavaElementSet = confidenceToSuggestedJavaElementSetMap
						.get(confidence);

				//Get the suggestions.
				for (IntentionElement<IElement> intentionElement : derivedPatternToResultMap
						.get(pattern)) {

					IJavaElement suggestedJavaElement = intentionElement
							.toJavaElement(graph.getDatabase());

					if (suggestedJavaElement != null) { //if no java element for this suggestion, disregard.

						//insert into database.
						try {
							DatabaseUtil
									.insertShadowAndRelationshipIntoDatabase(
											adviceKey,
											suggestedJavaElement,
											DatabaseUtil.AdviceShadowRelationship.HAS_BEEN_SUGGESTED_TO_ADVISE);
						}
						catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							throw new RuntimeException(e);
						}
						catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							throw new RuntimeException(e);
						}

						suggestedJavaElementSet.add(suggestedJavaElement);
						//						System.out.println(intentionElement.toPrettyString()
						//								+ "\t" + pattern + "\t" + confidence);
						Suggestion<IJavaElement> suggestion = new Suggestion<IJavaElement>(
								suggestedJavaElement, pattern, confidence);
						this.suggestionList.add(suggestion);
					}
				}
			}

			monitor.worked(1);
		}
	}

	/**
	 * @param derivedPatternToResultMap
	 * @param recoveredPatternToConfidenceMap
	 * @return
	 */
	private Set<Pattern<IntentionArc<IElement>>> obtainSurvingPatterns(
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> derivedPatternToResultMap,
			Map<Pattern<IntentionArc<IElement>>, Double> recoveredPatternToConfidenceMap) {
		Set<Pattern<IntentionArc<IElement>>> recoveredPatternSet = recoveredPatternToConfidenceMap
				.keySet();
		Set<Pattern<IntentionArc<IElement>>> derivedPatternSet = derivedPatternToResultMap
				.keySet();

		Set<Pattern<IntentionArc<IElement>>> survingPatternSet = new LinkedHashSet<Pattern<IntentionArc<IElement>>>(
				derivedPatternSet);
		survingPatternSet.retainAll(recoveredPatternSet);

		return survingPatternSet;
	}

	/**
	 * @param document
	 * @return
	 * @throws DataConversionException
	 */
	private Map<Pattern<IntentionArc<IElement>>, Double> extractPatterns(
			Document document) throws DataConversionException {
		Map<Pattern<IntentionArc<IElement>>, Double> ret = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Double>();

		Element root = document.getRootElement();
		for (Object patternObj : root.getChildren(PATTERN)) {
			Element patternElem = (Element) patternObj;
			Pattern<IntentionArc<IElement>> pattern = new Pattern<IntentionArc<IElement>>(
					patternElem);
			Attribute confidenceAttribute = patternElem
					.getAttribute("confidence");
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
		File advXMLFile = XMLUtil.getSavedXMLFile(advElem);
		return builder.build(advXMLFile);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return this.suggestionList.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * @return the suggestionList
	 */
	public List<Suggestion<IJavaElement>> getSuggestionList() {
		return this.suggestionList;
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