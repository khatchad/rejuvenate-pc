/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

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
		call(* getTotalNumberOfShadows(..)) ||
		call(* org.jdom..*.*(..));
	
	Object around() : toRemove() && !cflowbelow(toRemove()){
		final long start = System.currentTimeMillis();
		final Object ret = proceed();
		final long elapsed = System.currentTimeMillis() - start;
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