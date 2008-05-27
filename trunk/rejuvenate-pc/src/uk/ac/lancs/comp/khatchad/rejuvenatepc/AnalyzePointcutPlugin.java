package uk.ac.lancs.comp.khatchad.rejuvenatepc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;

public class AnalyzePointcutPlugin extends PointcutPlugin {

	private static final String DATABASE_FILE_NAME = "rejuv-pc.dat";

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

	@SuppressWarnings("unused")
	private static PrintWriter getSuggestionWriter() throws IOException {
		final File aFile = new File(AnalyzePointcutPlugin.RESULT_PATH
				+ "suggestion.csv");
		return AnalyzePointcutPlugin.getPrintWriter(aFile, true);
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
			analyzeAdvice(lMonitor, selectedAdvice);
		
		else if (!selectedProjectCol.isEmpty()) {
			PrintWriter benchmarkOut = generateBenchmarkStatsWriter();
			
			for (final IJavaProject proj : selectedProjectCol) {
				final long start = System.currentTimeMillis();
				
				Collection<? extends AdviceElement> toAnalyze = analyzeAdviceInProject(
						lMonitor, proj);

				int numShadows = 0;
				if (!toAnalyze.isEmpty())
					numShadows = this.getTotalNumberOfShadows(proj);

				final int secs = calculateTimeStatistics(start);

				printBenchmarkStatistics(benchmarkOut, proj, toAnalyze,
						numShadows, secs);
			}
			benchmarkOut.close();
		}

		lMonitor.done();
	}

	/**
	 * @param lMonitor
	 * @param proj
	 * @return
	 */
	private Collection<? extends AdviceElement> analyzeAdviceInProject(
			final IProgressMonitor lMonitor, final IJavaProject proj) {
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
	 * @param benchmarkOut
	 * @param proj
	 * @param toAnalyze
	 * @param numShadows
	 * @param secs
	 */
	private void printBenchmarkStatistics(PrintWriter benchmarkOut,
			final IJavaProject proj,
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
}