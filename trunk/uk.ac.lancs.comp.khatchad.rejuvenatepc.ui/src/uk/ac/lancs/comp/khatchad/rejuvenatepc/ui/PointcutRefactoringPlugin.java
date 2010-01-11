/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.ui;

import java.io.File;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import ca.mcgill.cs.swevo.jayfx.model.IElement;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionArc;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Path;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;

/**
 * @author raffi
 * 
 */
public abstract class PointcutRefactoringPlugin extends Plugin {	
	
	protected static final String RESULT_PATH = new File(ResourcesPlugin
			.getWorkspace().getRoot().getLocation().toOSString()
			+ File.separator + "results").getPath()
			+ File.separator;

	private static PrintWriter getPatternStatsWriter() throws IOException {
		final File aFile = new File(PointcutRefactoringPlugin.RESULT_PATH + "patterns.csv");
		return FileUtil.getPrintWriter(aFile, true);
	}

	private static PrintWriter getEnabledElementStatsWriter()
			throws IOException {
		final File aFile = new File(PointcutRefactoringPlugin.RESULT_PATH + "enabled.csv");
		return FileUtil.getPrintWriter(aFile, true);
	}

	private static PrintWriter getSuggestionStatsWriter() throws IOException {
		final File aFile = new File(PointcutRefactoringPlugin.RESULT_PATH
				+ "suggestions.csv");
		return FileUtil.getPrintWriter(aFile, true);
	}

	private static PrintWriter getAdviceStatsWriter() throws IOException {
		final File aFile = new File(PointcutRefactoringPlugin.RESULT_PATH + "advice.csv");
		return FileUtil.getPrintWriter(aFile, true);
	}

	private PrintWriter patternOut;
	private PrintWriter enabledOut;
	private PrintWriter suggestionOut;
	private PrintWriter adviceOut;

	private static PrintWriter generateSuggestionStatsWriter()
			throws IOException {
		final PrintWriter ret = getSuggestionStatsWriter();
		TimeCollector.start();
		ret.println("Benchmark\tAdvice#\tAdvice\tPattern\tElement\t");
		TimeCollector.stop();
		return ret;
	}

	private static PrintWriter generateEnabledStatsWriter() throws IOException {
		final PrintWriter patternOut = getEnabledElementStatsWriter();
		TimeCollector.start();
		patternOut.println("Benchmark\tAdvice#\tAdvice\tPattern\tElement\t");
		TimeCollector.stop();
		return patternOut;
	}

	public void dispose() {
		closeConnections();
	}

	/**
	 * 
	 */
	protected void closeConnections() {
		TimeCollector.start();
		this.patternOut.close();
		this.suggestionOut.close();
		this.enabledOut.close();
		this.adviceOut.close();
		TimeCollector.stop();
	}

	/**
	 * Make the result folder on startup.
	 */
	public void init(final IWorkbenchWindow lWindow) {
		final File resultFolder = new File(PointcutRefactoringPlugin.RESULT_PATH);
		if (!resultFolder.exists())
			resultFolder.mkdir();

		try {
			openConnections();
		}
		catch (IOException e) {
			//TODO: More robustness here.
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * @throws IOException
	 */
	protected void openConnections() throws IOException {
		patternOut = generatePatternStatsWriter();
		enabledOut = generateEnabledStatsWriter();
		suggestionOut = generateSuggestionStatsWriter();
		adviceOut = generateAdviceStatsWriter();
	}

	/**
	 * @param pattern
	 * @param pointcutCount
	 * @param advElem
	 * @param set
	 */
	private void printSuggestedElementResults(Pattern pattern,
			int pointcutCount, AdviceElement advElem,
			Set<IntentionElement<IElement>> set) {

		for (IntentionElement elem : set) {
			this.suggestionOut.print(advElem.getJavaProject().getProject()
					.getName()
					+ "\t");
			this.suggestionOut.print(pointcutCount + "\t");
			this.suggestionOut.print(advElem.readableName() + "\t");
			this.suggestionOut.print(pattern + "\t");
			this.suggestionOut.print(elem.getLongDescription());
			this.suggestionOut.println();
		}
	}

	private static PrintWriter generateAdviceStatsWriter() throws IOException {
		final PrintWriter ret = getAdviceStatsWriter();
		TimeCollector.start();
		ret
				.println("Benchmark\tAdvice#\tAdvice\t#Shadows\t#Patterns\t#Results\t#Enabled\t#OverallEnabled\tOverallElements\tConfidence\t");
		TimeCollector.stop();
		return ret;
	}

	@SuppressWarnings("restriction")
	protected void printAdviceResults(int pointcutCount, AdviceElement advElem,
			int numOfPatterns, int numOfResults, int numOfResultsThatAreEnabled,
			int numOfEnabledElementsOverall, int numOfElementsOverall,
			double averageConfidence, int numOfShadows) {
		this.adviceOut.print(advElem.getJavaProject().getProject().getName()
				+ "\t");
		this.adviceOut.print(pointcutCount + "\t");
		this.adviceOut.print(advElem.readableName() + "\t");
		this.adviceOut.print(numOfShadows + "\t");
		this.adviceOut.print(numOfPatterns + "\t");
		this.adviceOut.print(numOfResults + "\t");
		this.adviceOut.print(numOfResultsThatAreEnabled + "\t");
		this.adviceOut.print(numOfEnabledElementsOverall + "\t");
		this.adviceOut.print(numOfElementsOverall + "\t");
		this.adviceOut.print(averageConfidence + "\t");

		this.adviceOut.println();
	}

	/**
	 * 
	 */
	public PointcutRefactoringPlugin() {
		super();
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private static PrintWriter generatePatternStatsWriter() throws IOException {
		final PrintWriter patternOut = getPatternStatsWriter();
		TimeCollector.start();
		patternOut
				.println("Benchmark\tAdvice#\tAdvice\tPattern\t#Results\t#Enabled\tSize\tPrecision\tConcreteness\tConfidence");
		TimeCollector.stop();
		return patternOut;
	}

	/**
	 * @param pointcutCount
	 * @param advElem
	 * @param pattern
	 * @param precision
	 * @param concreteness
	 * @param confidence
	 * @param confidence2
	 */
	private void printPatternResults(int pointcutCount,
			final AdviceElement advElem, final Pattern pattern,
			int numOfResults, int numOfEnabled, double precision,
			double concreteness, double confidence) {
		patternOut
				.print(advElem.getJavaProject().getProject().getName() + "\t");
		patternOut.print(pointcutCount + "\t");
		patternOut.print(advElem.readableName() + "\t");
		patternOut.print(pattern + "\t");
		patternOut.print(numOfResults + "\t");
		patternOut.print(numOfEnabled + "\t");
		patternOut.print(pattern.size() + "\t");
		patternOut.print(precision + "\t");
		patternOut.print(concreteness + "\t");
		patternOut.print(confidence + "\t");
		patternOut.println();
	}

	/**
	 * @param pattern
	 * @param pointcut_count
	 * @param advElem
	 * @param set
	 * @throws IOException
	 */
	private void printEnabledElementResults(Pattern pattern, int pointcutCount,
			AdviceElement advElem, Set<IntentionElement<IElement>> set)
			throws IOException {

		for (IntentionElement elem : set) {
			this.enabledOut.print(advElem.getJavaProject().getProject()
					.getName()
					+ "\t");
			this.enabledOut.print(pointcutCount + "\t");
			this.enabledOut.print(advElem.readableName() + "\t");
			this.enabledOut.print(pattern + "\t");
			this.enabledOut.print(elem.getLongDescription());
			this.enabledOut.println();
		}
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
	//	protected void buildPatternMaps(
	//			final IProgressMonitor lMonitor,
	//			final IntentionGraph<IntentionNode<IElement>> graph,
	//			final WorkingMemory workingMemory,
	//			final AdviceElement advElem,
	//			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToResultMap,
	//			final Map<Pattern<IntentionArc<IElement>>, Set<IntentionElement<IElement>>> patternToEnabledElementMap)
	//			throws ConversionException, CoreException {
	//		graph.enableElementsAccordingTo(advElem, lMonitor);
	//		executeQueries(lMonitor, workingMemory, patternToResultMap,
	//				patternToEnabledElementMap);
	//	}
	
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
	 * @param start
	 * @return
	 */
	protected int calculateTimeStatistics(final long start) {
		long end = System.currentTimeMillis();
		
		long collectedTime = TimeCollector.getCollectedTime();
		long newStart = start + collectedTime;
		final long elapsed = end - newStart;	
		
		TimeCollector.clear();
		final int secs = (int) elapsed / 1000;
		return secs;
	}
}