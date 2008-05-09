/**
 * 
 */
package uk.ac.lancs.comp.khatchad.core;

/**
 * @author raffi
 *
 */
public aspect TimeColletor {
	private long collectedTime;
	pointcut toRemove() : 
		call(* java.io.PrintWriter+.print(..)) ||
		call(* java.io.PrintWriter+.println(..)) ||
		call(* java.io.PrintWriter+.close(..));
	
	Object around() : toRemove() {
		long start = System.currentTimeMillis();
		Object ret = proceed();
		long elapsed = System.currentTimeMillis() - start;
		this.collectedTime += elapsed;
		return ret;
	}
	
	public long getCollectedTime()
	{
		return this.collectedTime;
	}
	
	public void clear()
	{
		this.collectedTime = 0;
	}
}