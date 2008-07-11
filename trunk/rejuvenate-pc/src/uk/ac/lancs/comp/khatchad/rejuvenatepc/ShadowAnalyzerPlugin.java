/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.IAJCodeElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;

/**
 * @author raffi
 * 
 */
public class ShadowAnalyzerPlugin extends Plugin {

	private static final String URL = "jdbc:postgresql://localhost/rejuv-pc";

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.Plugin#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void run(IProgressMonitor monitor) {
		Collection<AdviceElement> toAnalyze = new LinkedHashSet<AdviceElement>();

		toAnalyze.addAll(this.getSelectedAdvice());

		for (IJavaProject proj : this.getSelectedJavaProjects()) {
			try {
				toAnalyze.addAll(Util.extractValidAdviceElements(proj));
			}
			catch (JavaModelException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
		}

		monitor.beginTask("Analyzing advice.", toAnalyze.size());
		System.out
				.println("Key\tType\tBenchmark\tPackage\tClass\tEnclosing Method\tShadow\tAdvised By");
		for (AdviceElement advElem : toAnalyze) {
			try {
				analyze(advElem, new SubProgressMonitor(monitor, -1));
			}
			catch (JavaModelException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
			monitor.worked(1);
		}
	}

	/**
	 * @param advElem
	 * @param monitor
	 * @throws JavaModelException
	 * @throws SQLException
	 */
	private void analyze(AdviceElement advElem, IProgressMonitor monitor)
			throws JavaModelException, SQLException {

		String adviceKey = getKey(advElem);

		try {
			Class.forName("org.postgresql.Driver");
			Connection conn = DriverManager.getConnection(URL, "raffi", "");
			Statement st = conn.createStatement();

			ResultSet res = st
					.executeQuery("select 1 from advice where key = '"
							+ adviceKey + "'");
			if (!res.next())
				st.executeUpdate("insert into advice values ('" + adviceKey
						+ "')");

			res.close();
			st.close();
			conn.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		Set<IJavaElement> advisedJavaElements = Util
				.getAdvisedJavaElements(advElem);
		monitor.beginTask("Retrieving advised Java Elements.",
				advisedJavaElements.size());

		for (IJavaElement javaElem : advisedJavaElements) {
			String benchmark = javaElem.getJavaProject().getProject().getName();
			String key = getKey(javaElem);

			String elemName = javaElem.getElementName();

			String enclosingMethodName = null;
			String className = null;
			String packageName = null;

			if (javaElem instanceof IAJCodeElement)
				enclosingMethodName = javaElem.getParent().getElementName();

			if (javaElem instanceof IAJCodeElement) {
				className = javaElem.getParent().getParent().getElementName();
				packageName = ((IType) javaElem.getParent().getParent())
						.getPackageFragment().getElementName();
			}
			else {
				className = javaElem.getParent().getElementName();
				packageName = ((IType) javaElem.getParent())
						.getPackageFragment().getElementName();
			}

			System.out.println(key + "\t" + javaElem.getClass().getSimpleName()
					+ "\t" + benchmark + "\t" + packageName + "\t" + className
					+ "\t" + enclosingMethodName + "\t" + elemName + "\t"
					+ adviceKey);

			int version = getVersionNumber(benchmark);

			final String query = "insert into shadows values ("
					+ "'"
					+ key
					+ "', "
					+ "'"
					+ javaElem.getClass().getSimpleName()
					+ "', "
					+ "'"
					+ benchmark
					+ "', "
					+ "'"
					+ packageName
					+ "', "
					+ "'"
					+ className
					+ "', "
					+ (enclosingMethodName == null ? "NULL" : "'"
							+ enclosingMethodName + "'") + "," + "'" + elemName
					+ "', " + version + ")";

			System.err.println(query);

			try {
				Class.forName("org.postgresql.Driver");
				Connection conn = DriverManager.getConnection(URL, "raffi", "");
				Statement st = conn.createStatement();

				ResultSet res = st
						.executeQuery("select 1 from shadows where key = '"
								+ key + "' and version = " + version);
				if (!res.next())
					st.executeUpdate(query);

				res = st
						.executeQuery("select 1 from advises_shadow where advice_key = '"
								+ adviceKey
								+ "' and version = "
								+ version
								+ " and shadow_key = '" + key + "'");
				if (!res.next())
					st.executeUpdate("insert into advises_shadow values ('"
							+ adviceKey + "', " + "'" + key + "', " + version
							+ ")");

				res.close();
				st.close();
				conn.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * @param benchmark
	 * @return
	 */
	private static int getVersionNumber(String benchmark) {
		int pos = benchmark.indexOf('_');
		String number = benchmark.substring(pos + 1, pos + 3);
		int ret = Integer.valueOf(number);
		System.err.println("Version number: " + ret);
		return ret;
	}

	/**
	 * @param javaElem
	 * @return
	 */
	private static String getKey(IJavaElement javaElem) {
		StringBuilder key = new StringBuilder(javaElem.getHandleIdentifier());
		key.delete(0, key.indexOf("<") + 1);
		int pos = key.indexOf("!");
		if (pos != -1)
			key.delete(pos, key.length() - 1);
		return key.toString();
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}
}