/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

/**
 * @author raffi
 *
 */
public class TimeCollector {
	
	private static long collectedTime;
	private static long start;
	
	public static void start() {
		start = System.currentTimeMillis();
	}
	
	public static void stop() {
		final long elapsed = System.currentTimeMillis() - start;
		collectedTime += elapsed;
	}
	
	public static long getCollectedTime() {
		return collectedTime;
	}

	public static void clear() {
		collectedTime = 0;
	}
}
