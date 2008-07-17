/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.ajdt.core.javaelements.IAJCodeElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.Constants;

/**
 * @author raffi
 * 
 */
public class DatabaseUtil {

	public enum AdviceShadowRelationship {
		ADVISES("advises_shadow"), HAS_BEEN_SUGGESTED_TO_ADVISE(
				"suggested_shadows");

		private String relationshipTableName;

		AdviceShadowRelationship(String relationshipTableName) {
			this.relationshipTableName = relationshipTableName;
		}

		public String getRelationshipTableName() {
			return this.relationshipTableName;
		}
	}

	private DatabaseUtil() {
	}

	/**
	 * @param javaElem
	 * @return
	 */
	public static String getKey(IJavaElement javaElem) {
		StringBuilder key = new StringBuilder(javaElem.getHandleIdentifier());
		key.delete(0, key.indexOf("<") + 1);
		int pos = key.indexOf("!");
		if (pos != -1)
			key.delete(pos, key.length() - 1);
		return key.toString();
	}

	/**
	 * @param st
	 * @param res
	 * @throws SQLException
	 */
	public static void closeDatabaseConnections(Statement st, ResultSet res)
			throws SQLException {
		res.close();
		st.close();
		st.getConnection().close();
	}

	/**
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Statement getStatement() throws ClassNotFoundException,
			SQLException {
		Class.forName("org.postgresql.Driver");
		Statement st = DriverManager.getConnection(Constants.URL, "raffi", "")
				.createStatement();
		return st;
	}

	/**
	 * @param adviceKey
	 * @param shadowKey
	 * @param version
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void insertAdviceShadowRelationshipIntoDatabase(
			String adviceKey, String shadowKey, int version,
			AdviceShadowRelationship relationship) throws ClassNotFoundException, SQLException {
		Statement st = null;
		ResultSet res = null;
		try {
			st = getStatement();

			res = st.executeQuery("select 1 from "
					+ relationship.getRelationshipTableName()
					+ " where advice_key = '" + adviceKey + "' and version = "
					+ version + " and shadow_key = '" + shadowKey + "'");
			if (!res.next())
				st.executeUpdate("insert into "
						+ relationship.getRelationshipTableName()
						+ " values ('" + adviceKey + "', " + "'" + shadowKey
						+ "', " + version + ")");
		}
		finally {
			closeDatabaseConnections(st, res);
		}
	}

	/**
	 * @param javaElem
	 * @param benchmark
	 * @param shadowKey
	 * @param elemName
	 * @param enclosingMethodName
	 * @param className
	 * @param packageName
	 * @param version
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void insertShadowIntoDatabase(IJavaElement javaElem,
			String benchmark, String shadowKey, String elemName,
			String enclosingMethodName, String className, String packageName,
			int version) throws ClassNotFoundException, SQLException {
		final String query = "insert into shadows values ("
				+ "'"
				+ shadowKey
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

		Statement st = null;
		ResultSet res = null;
		try {
			st = getStatement();

			res = st.executeQuery("select 1 from shadows where key = '"
					+ shadowKey + "' and version = " + version);
			if (!res.next())
				st.executeUpdate(query);
		}
		finally {
			closeDatabaseConnections(st, res);
		}
	}

	/**
	 * @param adviceKey
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void insertAdviceIntoDatabase(String adviceKey)
			throws ClassNotFoundException, SQLException {

		Statement st = null;
		ResultSet res = null;

		try {
			st = getStatement();

			res = st.executeQuery("select 1 from advice where key = '"
					+ adviceKey + "'");
			if (!res.next())
				st.executeUpdate("insert into advice values ('" + adviceKey
						+ "')");
		}
		finally {
			closeDatabaseConnections(st, res);
		}
	}

	/**
	 * @param benchmark
	 * @return
	 */
	public static int getVersionNumber(String benchmark) {
		int pos = benchmark.indexOf('_');
		String number = benchmark.substring(pos + 1, pos + 3);
		int ret = Integer.valueOf(number);
		System.err.println("Version number: " + ret);
		return ret;
	}

	/**
	 * @param adviceKey
	 * @param javaElem
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void insertShadowAndRelationshipIntoDatabase(
			String adviceKey, IJavaElement javaElem,
			AdviceShadowRelationship relationship) throws ClassNotFoundException, SQLException {
		String benchmark = javaElem.getJavaProject().getProject().getName();
		String shadowKey = getKey(javaElem);

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
			packageName = ((IType) javaElem.getParent()).getPackageFragment()
					.getElementName();
		}

		int version = getVersionNumber(benchmark);

		insertShadowIntoDatabase(javaElem, benchmark, shadowKey, elemName,
				enclosingMethodName, className, packageName, version);

		insertAdviceShadowRelationshipIntoDatabase(adviceKey, shadowKey,
				version, relationship);
	}
}