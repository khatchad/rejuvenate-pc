/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.drools.WorkingMemory;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionEdge;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;
import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;

/**
 * @author raffi
 * 
 */
public class RejuvenatePointcutPlugin extends PointcutPlugin {

	public void run(IAction action) {
		final IProgressMonitor monitor = getProgressMonitor();
		final Collection<AdviceElement> selectedAdvice = this
				.getSelectedAdvice();

		if (!selectedAdvice.isEmpty())
			analyzeAdvice(monitor, selectedAdvice);
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.PointcutPlugin#analyzeAdviceCollection(java.util.Collection, org.eclipse.core.runtime.IProgressMonitor, uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph, org.drools.WorkingMemory, java.io.PrintWriter)
	 */
	@Override
	protected void analyzeAdviceCollection(
			Collection<? extends AdviceElement> adviceCol,
			IProgressMonitor monitor,
			IntentionGraph<IntentionNode<IElement>> graph,
			WorkingMemory workingMemory, PrintWriter patternOut)
			throws ConversionException, CoreException, IOException {

		monitor.beginTask("Enabling graph elements for each selected advice.",
				adviceCol.size());

		for (final AdviceElement advElem : adviceCol) {
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap = new LinkedHashMap<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>>();
			final Map<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap = new LinkedHashMap<Pattern<IntentionEdge<IElement>>, Set<IntentionElement<IElement>>>();
			
			//retrieve analysis information.
			Document document = null; //readXMLFile(advElem);
			System.out.println(document);

//			graph.enableElementsAccordingTo(advElem, monitor);
//			
//			buildPatternMaps(monitor, graph, workingMemory, advElem,
//					patternToResultMap, patternToEnabledElementMap);
			
			//TODO: Intersect patterns.

			//TODO: Need to make suggestions.
//			SortedMap<IJavaElement, Double> suggestions = null;// obtainSuggestions()

			monitor.worked(1);
		}
	}
}