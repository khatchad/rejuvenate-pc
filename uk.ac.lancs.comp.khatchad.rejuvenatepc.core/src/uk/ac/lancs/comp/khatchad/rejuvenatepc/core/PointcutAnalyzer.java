package uk.ac.lancs.comp.khatchad.rejuvenatepc.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.drools.WorkingMemory;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionArc;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.AJUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.DatabaseUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.FileUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.GraphVizUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.XMLUtil;
import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;

public class PointcutAnalyzer extends PointcutProcessor {

	/**
	 * @param adviceCol
	 * @param monitor
	 * @param graph
	 * @param workingMemory
	 * @param patternOut
	 * @throws ConversionException
	 * @throws CoreException
	 * @throws IOException
	 */
	@SuppressWarnings("restriction")
	protected void analyzeAdviceCollection(
			final Collection<? extends AdviceElement> adviceCol,
			final IProgressMonitor monitor, final IntentionGraph graph,
			final WorkingMemory workingMemory) throws ConversionException,
			CoreException, IOException {

		monitor.beginTask("Enabling graph elements for each selected advice.",
				adviceCol.size());

		int pointcutCount = 0;
		for (final AdviceElement advElem : adviceCol) {

			try {
				DatabaseUtil.insertIntoDatabase(advElem);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			Element adviceXMLElement = createAdviceXMLElement(advElem);

			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>>();
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>>();

			graph.enableElementsAccordingTo(advElem, monitor);

			GraphVizUtil.makeDotFile(graph, pointcutCount,
					FileUtil.WORKSPACE_LOC + advElem.getPath().toOSString()
							+ "-");

			executeQueries(monitor, workingMemory, patternToResultMap,
					patternToEnabledElementMap);

			double totalConfidence = 0;
			for (final Pattern pattern : patternToResultMap.keySet())
				totalConfidence += calculatePatternStatistics(pointcutCount,
						advElem, adviceXMLElement, patternToResultMap,
						patternToEnabledElementMap, pattern, graph);

			writeXMLFile(advElem, adviceXMLElement);
			pointcutCount++;
			monitor.worked(1);
			// printAdviceResults(pointcutCount, advElem, patternToResultMap
			// .keySet().size(), Util.flattenCollection(
			// patternToResultMap.values()).size(), Util
			// .flattenCollection(patternToEnabledElementMap.values())
			// .size(), graph.getEnabledElements().size(), graph
			// .getAllElements().size(), totalConfidence
			// / patternToResultMap.keySet().size(), AJUtil
			// .getAdvisedJavaElements(advElem).size());
		}
	}

	/**
	 * @param advElem
	 * @param adviceXMLElement
	 * @throws IOException
	 * @throws CoreException
	 */
	protected void writeXMLFile(final AdviceElement advElem,
			Element adviceXMLElement) throws IOException, CoreException {
		DocType type = new DocType(this.getClass().getSimpleName());
		Document doc = new Document(adviceXMLElement, type);
		XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
		PrintWriter xmlOut = XMLUtil.getXMLFileWriter(advElem);
		serializer.output(doc, xmlOut);
		xmlOut.close();
		advElem.getJavaProject().getProject().refreshLocal(
				IResource.DEPTH_INFINITE, null);
	}
}