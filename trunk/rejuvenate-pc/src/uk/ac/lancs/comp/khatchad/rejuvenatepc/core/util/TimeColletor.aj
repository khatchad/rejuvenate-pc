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
public aspect TimeColleting {
	private long collectedTime;

	pointcut toRemove() : 
		execution(* Util.readRule(..)) ||
		execution(* Util.makeDotFile(..)) ||
		execution(* Util.get*XML*(..)) ||
		execution(CompilationUnit getCompilationUnit(ICompilationUnit)) ||
		execution(PrintWriter+ get*Writer(..)) ||
		execution(* ASTParser+.createAST(..)) ||
		execution(* getTotalNumberOfShadows(..)) ||
		execution(* org.jdom..*.*(..)) ||
		(execution(* *.*(..)) && within(java.io..*));

	Object around() : toRemove() && !cflowbelow(toRemove()){
		final long start = System.currentTimeMillis();
		final Object ret = proceed();
		final long elapsed = System.currentTimeMillis() - start;
		this.collectedTime += elapsed;
		return ret;
	}

	public long getCollectedTime() {
		return this.collectedTime;
	}

	public void clear() {
		this.collectedTime = 0;
	}
}