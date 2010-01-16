package uk.ac.lancs.comp.khatchad.rejuvenatepc.ui;

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

import javax.mail.MessagingException;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ca.mcgill.cs.swevo.jayfx.model.IElement;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.PointcutAnalyzer;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionArc;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.AJUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.DatabaseUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.FileUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.GraphVizUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.TimeCollector;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.XMLUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.util.PostMan;

public class AnalyzePointcutPlugin extends PointcutRefactoringPlugin {

	private PointcutAnalyzer analyzer = new PointcutAnalyzer();

	private PrintWriter benchmarkOut;

	private static PrintWriter getBenchmarkStatsWriter() throws IOException {
		final File aFile = new File(PointcutRefactoringPlugin.RESULT_PATH
				+ "benchmarks.csv");
		return FileUtil.getPrintWriter(aFile, true);
	}

	protected void run(IProgressMonitor monitor) {

		try {
			// TODO: Don't know why I should have to call this manually.
			this.init(null);

			final Collection<AdviceElement> selectedAdvice = this
					.getSelectedAdvice();
			final Collection<IJavaProject> selectedProjectCol = this
					.getSelectedJavaProjects();

			if (!selectedAdvice.isEmpty() && !selectedProjectCol.isEmpty())
				throw new IllegalStateException(
						"For test runs, select *either* project or advice but not both.");

			if (!selectedAdvice.isEmpty())
				analyzer.analyzeAdvice(selectedAdvice, monitor);

			else if (!selectedProjectCol.isEmpty()) {
				for (final IJavaProject proj : selectedProjectCol) {
					final long start = System.currentTimeMillis();

					Collection<? extends AdviceElement> analyzedAdvice = analyzeAdviceInProject(
							proj, monitor);

					int numShadows = 0;
					if (!analyzedAdvice.isEmpty())
						numShadows = this.getTotalNumberOfShadows(proj);

					final int secs = calculateTimeStatistics(start);

					printBenchmarkStatistics(proj, analyzedAdvice, numShadows,
							secs);
				}
			}
		}
		finally {
			this.closeConnections();
			PostMan.postMail("Done", "Done", "khatchad@cse.ohio-state.edu",
					"khatchad@cse.ohio-state.edu");
			if (System.getProperty("os.name").equalsIgnoreCase("Mac OS X"))
				try {
					Runtime.getRuntime().exec(
							"/usr/local/bin/growlnotify -n Eclipse -a Eclipse -m "
									+ this.getClass().getSimpleName()
									+ " is done");
				}
				catch (final IOException e) {
					System.err.println("Can't send notification.");
				}
		}
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
			toAnalyze = AJUtil.extractValidAdviceElements(proj);
			if (!toAnalyze.isEmpty()) {
				this.analyzer.analyze(toAnalyze, lMonitor);
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
		TimeCollector.start();
		benchmarkOut.println("Benchmark\t#Advice\t#Shadows\tTime(s)");
		TimeCollector.stop();
		return benchmarkOut;
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
		TimeCollector.start();
		final List<AJRelationship> relationshipList = AJModel
				.getInstance()
				.getAllRelationships(
						proj.getProject(),
						new AJRelationshipType[] { AJRelationshipManager.ADVISES });
		int ret = relationshipList.size();
		TimeCollector.stop();
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.PointcutRefactoringPlugin#closeConnections()
	 */
	@Override
	protected void closeConnections() {
		super.closeConnections();
		TimeCollector.start();
		this.benchmarkOut.close();
		TimeCollector.stop();
	}

	@Override
	protected void openConnections() throws IOException {
		super.openConnections();
		benchmarkOut = generateBenchmarkStatsWriter();
	}
}