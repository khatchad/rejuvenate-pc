package uk.ac.lancs.comp.khatchad.rejuvenatepc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.drools.WorkingMemory;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.model.IElement;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionArc;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;

public class AnalyzePointcutPlugin extends PointcutRefactoringPlugin {

	@SuppressWarnings("unused")
	private static final String DATABASE_FILE_NAME = "rejuv-pc.xml";

	private PrintWriter benchmarkOut;

	private static PrintWriter getBenchmarkStatsWriter() throws IOException {
		final File aFile = new File(PointcutRefactoringPlugin.RESULT_PATH
				+ "benchmarks.csv");
		return Util.getPrintWriter(aFile, true);
	}

	protected void run(IProgressMonitor monitor) {
		//TODO: Don't know why I should have to call this manually.
		this.init(null);

		final Collection<AdviceElement> selectedAdvice = this
				.getSelectedAdvice();
		final Collection<IJavaProject> selectedProjectCol = this
				.getSelectedJavaProjects();

		if (!selectedAdvice.isEmpty() && !selectedProjectCol.isEmpty())
			throw new IllegalStateException(
					"For test runs, select *either* project or advice but not both.");

		if (!selectedAdvice.isEmpty())
			analyzeAdvice(selectedAdvice, monitor);

		else if (!selectedProjectCol.isEmpty()) {
			for (final IJavaProject proj : selectedProjectCol) {
				final long start = System.currentTimeMillis();

				Collection<? extends AdviceElement> toAnalyze = analyzeAdviceInProject(
						proj, monitor);

				int numShadows = 0;
				if (!toAnalyze.isEmpty())
					numShadows = this.getTotalNumberOfShadows(proj);

				final int secs = calculateTimeStatistics(start);

				printBenchmarkStatistics(proj, toAnalyze, numShadows, secs);
			}
		}
		this.closeConnections();
	}

	/**
	 * @param proj
	 * @param lMonitor
	 * @return
	 */
	private Collection<? extends AdviceElement> analyzeAdviceInProject(
			final IJavaProject proj, final IProgressMonitor lMonitor) {
		Collection<? extends AdviceElement> toAnalyze = null;
		try {
			toAnalyze = Util.extractValidAdviceElements(proj);
			if (!toAnalyze.isEmpty()) {
				this.analyze(toAnalyze, lMonitor);
			}
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
		return toAnalyze;
	}

	/**
	 * @return
	 */
	private PrintWriter generateBenchmarkStatsWriter() {
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
		return benchmarkOut;
	}

	/**
	 * @param start
	 * @return
	 */
	private int calculateTimeStatistics(final long start) {
		uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.TimeColletor collector = uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.TimeColletor
				.aspectOf();

		final long elapsed = System.currentTimeMillis()
				- (start + collector.getCollectedTime());
		collector.clear();
		final int secs = (int) elapsed / 1000;
		return secs;
	}

	/**
	 * @param proj
	 * @param toAnalyze
	 * @param numShadows
	 * @param secs
	 */
	private void printBenchmarkStatistics(final IJavaProject proj,
			Collection<? extends AdviceElement> toAnalyze, int numShadows,
			final int secs) {
		benchmarkOut.print(proj.getProject().getName() + "\t");
		benchmarkOut.print(toAnalyze.size() + "\t");
		benchmarkOut.print(numShadows + "\t");
		benchmarkOut.print(secs + "\t");
		benchmarkOut.println();
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
			Element adviceXMLElement = createAdviceXMLElement(advElem);

			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>>();
			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap = new LinkedHashMap<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>>();

			graph.enableElementsAccordingTo(advElem, monitor);

			Util.makeDotFile(graph, pointcutCount, Util.WORKSPACE_LOC
					+ advElem.getPath().toOSString() + "-");

			executeQueries(monitor, workingMemory, patternToResultMap,
					patternToEnabledElementMap);

			double totalConfidence = 0;
			for (final Pattern pattern : patternToResultMap.keySet())
				totalConfidence += calculatePatternStatistics(pointcutCount,
						advElem, adviceXMLElement, patternToResultMap,
						patternToEnabledElementMap, pattern);

			writeXMLFile(advElem, adviceXMLElement);
			pointcutCount++;
			monitor.worked(1);
			printAdviceResults(pointcutCount, advElem, patternToResultMap
					.keySet().size(), Util.flattenCollection(
					patternToResultMap.values()).size(), Util
					.flattenCollection(patternToEnabledElementMap.values())
					.size(), totalConfidence
					/ patternToResultMap.keySet().size(), Util
					.getAdvisedJavaElements(advElem).size());
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.PointcutRefactoringPlugin#closeConnections()
	 */
	@Override
	protected void closeConnections() {
		super.closeConnections();
		this.benchmarkOut.close();
	}

	@Override
	protected void openConnections() throws IOException {
		super.openConnections();
		benchmarkOut = generateBenchmarkStatsWriter();
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
		PrintWriter xmlOut = Util.getXMLFileWriter(advElem);
		serializer.output(doc, xmlOut);
		xmlOut.close();
	}
}