/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;

/**
 * @author raffi
 *
 */
public class GraphVizUtil {
	private GraphVizUtil() {}

	@SuppressWarnings("unused")
	public static void makeDotFile(
			final IntentionGraph graph,
			final String resultPath) throws IOException {
		final File file = new File(resultPath + "intention_graph.dot");
		GraphVizUtil.makeDotFile(graph, file);
	}

	public static void makeDotFile(
			final IntentionGraph graph,
			final File aFile) throws IOException {
		final FileWriter resFileOut = new FileWriter(aFile, false);
		final PrintWriter resOut = new PrintWriter(resFileOut);
		resOut.println(graph.toDotFormat());
		resOut.close();
	}

	/**
	 * @param graph
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	public static void makeDotFile(
			final IntentionGraph graph,
			final int adviceNumer, final String resultPath) throws IOException {
		final File file = new File(resultPath + "adv" + adviceNumer + ".dot");
		GraphVizUtil.makeDotFile(graph, file);
	}
}
