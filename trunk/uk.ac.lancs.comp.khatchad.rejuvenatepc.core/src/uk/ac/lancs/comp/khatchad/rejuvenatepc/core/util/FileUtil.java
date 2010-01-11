/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * @author raffi
 *
 */
public class FileUtil {
	/**
	 * Where to store benchmark results.
	 */
	public static final File WORKSPACE_LOC = ResourcesPlugin.getWorkspace()
			.getRoot().getLocation().toFile();

	private FileUtil() {}

	/**
	 * @return
	 * @throws Exception 
	 * @throws Exception
	 */
	public static RuleBase readRule(final Reader source) throws Exception {
		//Use package builder to build up a rule package.
		//An alternative lower level class called "DrlParser" can also be used...
	
		final PackageBuilder builder = new PackageBuilder();
	
		//this wil parse and compile in one step
		//NOTE: There are 2 methods here, the one argument one is for normal DRL.
		builder.addPackageFromDrl(source);
	
		//get the compiled package (which is serializable)
		final org.drools.rule.Package pkg = builder.getPackage();
	
		//add the package to a rulebase (deploy the rule package).
		final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
		ruleBase.addPackage(pkg);
		return ruleBase;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public static PrintWriter getPrintWriter(final File aFile,
			final boolean append) throws IOException {
		TimeCollector.start();
		final FileWriter resFileOut = new FileWriter(aFile, append);
		PrintWriter ret = new PrintWriter(resFileOut);
		TimeCollector.stop();
		return ret;
	}
}
