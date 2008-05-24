/**
 * 
 */
package uk.ac.lancs.comp.khatchad.core;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import java.io.*;

/**
 * @author raffi
 *
 */
public aspect TimeColletor {
	private long collectedTime;
	pointcut toRemove() : 
		call(* PrintWriter+.print(..)) ||
		call(* PrintWriter+.println(..)) ||
		call(* PrintWriter+.close(..)) ||
		call(CompilationUnit getCompilationUnit(ICompilationUnit)) ||
		call(PrintWriter+ get*StatsWriter(..)) ||
		call(* ASTParser+.createAST(..)) ||
		call(* calculate*(..)) ||
		call(* getTotalNumberOfShadows(..));
	
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