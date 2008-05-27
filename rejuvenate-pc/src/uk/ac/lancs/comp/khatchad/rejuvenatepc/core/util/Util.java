package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.PackageBuilder;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.search.SearchMatch;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.IElement;

/**
 * Various utility stuff.
 */
public class Util {

	/**
	 * Where to store benchmark results.
	 */

	public static final File WORKSPACE_LOC = ResourcesPlugin.getWorkspace()
			.getRoot().getLocation().toFile();

	public static void assertExpression(final boolean exp) {
		if (exp == false)
			throw new AssertionError("Failed assertion");
	}
	
//	public static ObjectContainer getDatabaseConnection(File databaseFile) {
//		ObjectContainer db = Db4o.openFile(databaseFile.getAbsolutePath());
//		return db;
//	}

	public static IElement convertBinding(final ICategories category,
			final String readableName) {
		return FlyweightElementFactory.getElement(category, readableName, null);
	}

	/**
	 * @param proj
	 * @return
	 * @throws JavaModelException
	 */
	public static Collection<? extends AdviceElement> extractValidAdviceElements(
			final IJavaProject proj) throws JavaModelException {
		final Collection<AdviceElement> ret = new LinkedHashSet<AdviceElement>();

		if (AspectJPlugin.isAJProject(proj.getProject()))
			for (final IPackageFragment frag : proj.getPackageFragments())
				for (final ICompilationUnit unit : frag.getCompilationUnits()) {
					final ICompilationUnit mappedUnit = AJCompilationUnitManager
							.mapToAJCompilationUnit(unit);
					if (mappedUnit instanceof AJCompilationUnit) {
						final AJCompilationUnit ajUnit = (AJCompilationUnit) mappedUnit;
						for (final IType type : ajUnit.getAllTypes())
							if (type instanceof AspectElement) {
								final AspectElement aspectElem = (AspectElement) type;
								ret.addAll(Arrays
										.asList(aspectElem.getAdvice()));
							}
					}
				}
		return ret;
	}

	public static ASTNode getASTNode(final IJavaElement elem,
			final IProgressMonitor monitor) {
		final IMember mem = Util.getIMember(elem);
		final ICompilationUnit icu = mem.getCompilationUnit();
		if (icu == null)
			throw new IllegalStateException("Source not present.");
		final ASTNode root = Util.getCompilationUnit(icu, monitor);
		return root;
	}

	public static CompilationUnit getCompilationUnit(
			final ICompilationUnit icu, final IProgressMonitor monitor) {
		final ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		final CompilationUnit ret = (CompilationUnit) parser.createAST(monitor);
		return ret;
	}

	public static ASTNode getExactASTNode(final CompilationUnit root,
			final ISourceRange range) {
		final ArrayList<ASTNode> ret = new ArrayList<ASTNode>(1);
		final ASTVisitor visitor = new ASTVisitor() {
			@Override
			public void preVisit(ASTNode node) {
				if (node.getStartPosition() == range.getOffset()) {
					ret.clear();
					ret.add(node);
				}
			}
		};
		root.accept(visitor);
		return ret.get(0);
	}

	public static ASTNode getExactASTNode(final CompilationUnit root,
			final SearchMatch match) {
		final ArrayList<ASTNode> ret = new ArrayList<ASTNode>(1);
		final ASTVisitor visitor = new ASTVisitor() {
			@Override
			public void preVisit(ASTNode node) {
				if (node.getStartPosition() == match.getOffset()) {
					ret.clear();
					ret.add(node);
				}
			}
		};
		root.accept(visitor);
		return ret.get(0);
	}

	public static ASTNode getExactASTNode(final IJavaElement elem,
			final ISourceRange nameRange, final IProgressMonitor monitor) {
		final IMember mem = Util.getIMember(elem);
		final CompilationUnit root = Util.getCompilationUnit(mem
				.getCompilationUnit(), monitor);
		return Util.getExactASTNode(root, nameRange);
	}

	public static ASTNode getExactASTNode(final IJavaElement elem,
			final SearchMatch match, final IProgressMonitor monitor) {
		final IMember mem = Util.getIMember(elem);
		final CompilationUnit root = Util.getCompilationUnit(mem
				.getCompilationUnit(), monitor);
		return Util.getExactASTNode(root, match);
	}

	public static ASTNode getExactASTNode(final SearchMatch match,
			final IProgressMonitor monitor) {
		final IJavaElement elem = (IJavaElement) match.getElement();
		return Util.getExactASTNode(elem, match, monitor);
	}

	public static FieldDeclaration getFieldDeclaration(final ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof FieldDeclaration)
			return (FieldDeclaration) node;
		else
			return Util.getFieldDeclaration(node.getParent());
	}

	public static IMember getIMember(final IJavaElement elem) {

		if (elem == null)
			throw new IllegalArgumentException(
					"Can not get IMember from null element.");

		switch (elem.getElementType()) {
			case IJavaElement.METHOD:
			case IJavaElement.FIELD:
			case IJavaElement.INITIALIZER:
			case IJavaElement.TYPE: {
				return (IMember) elem;
			}
		}

		return Util.getIMember(elem.getParent());
	}

	public static InfixExpression getInfixExpression(final ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof InfixExpression)
			return (InfixExpression) node;
		else
			return Util.getInfixExpression(node.getParent());
	}

	public static MethodDeclaration getMethodDeclaration(final ASTNode node) {
		ASTNode trav = node;
		while (trav.getNodeType() != ASTNode.METHOD_DECLARATION)
			trav = trav.getParent();
		return (MethodDeclaration) trav;
	}

	/**
	 * @param adviceCol
	 * @return
	 */
	@SuppressWarnings("restriction")
	public static Set<IProject> getProjects(
			final Collection<? extends AdviceElement> adviceCol) {
		final Set<IProject> ret = new LinkedHashSet<IProject>();
		for (final AdviceElement elem : adviceCol)
			ret.add(elem.getJavaProject().getProject());
		return ret;
	}

	public static SingleVariableDeclaration getSingleVariableDeclaration(
			final ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof SingleVariableDeclaration)
			return (SingleVariableDeclaration) node;
		else
			return Util.getSingleVariableDeclaration(node.getParent());
	}

	public static Name getTopmostName(final ASTNode node) {
		if (node == null)
			return null;
		else if (node.getParent() == null
				|| node.getParent().getNodeType() != ASTNode.QUALIFIED_NAME)
			return (Name) node;
		else
			return Util.getTopmostName(node.getParent());
	}

	public static VariableDeclarationStatement getVariableDeclarationStatement(
			final ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof VariableDeclarationStatement)
			return (VariableDeclarationStatement) node;
		else
			return Util.getVariableDeclarationStatement(node.getParent());
	}

	public static boolean isConstantField(final IField field)
			throws JavaModelException {
		if (field.getConstant() == null)
			return false;
		return true;
	}

	public static boolean isContainedInCaseLabel(final ASTNode node) {
		if (node == null)
			return false;
		else if (node.getNodeType() == ASTNode.SWITCH_CASE)
			return true;
		else
			return Util.isContainedInCaseLabel(node.getParent());
	}

	/**
	 * @return
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

	public static String stripQualifiedName(final String qualifiedName) {
		if (!qualifiedName.contains("."))
			return qualifiedName;

		final int pos = qualifiedName.lastIndexOf('.');
		return qualifiedName.substring(pos + 1);
	}

	@SuppressWarnings( { "unchecked", "unused" })
	private static boolean distinct(final Collection<Object> col) {
		final Comparable[] objs = new Comparable[col.size()];
		col.toArray(objs);
		try {
			Arrays.sort(objs);
		}
		catch (final ClassCastException E) {
			for (int i = 0; i < objs.length; i++)
				for (int j = i + 1; j < objs.length; j++)
					if (objs[i].equals(objs[j]))
						return false;
			return true;
		}
		for (int i = 1; i < objs.length; i++)
			if (objs[i].equals(objs[i - 1]))
				return false;
		return true;
	}

	private static void makeDotFile(
			final IntentionGraph<IntentionNode<IElement>> graph,
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
	private static void makeDotFile(
			final IntentionGraph<IntentionNode<IElement>> graph,
			final int adviceNumer, final String resultPath) throws IOException {
		final File file = new File(resultPath + "adv" + adviceNumer + ".dot");
		Util.makeDotFile(graph, file);
	}

	@SuppressWarnings("unused")
	private static void makeDotFile(
			final IntentionGraph<IntentionNode<IElement>> graph,
			final String resultPath) throws IOException {
		final File file = new File(resultPath + "intention_graph.dot");
		Util.makeDotFile(graph, file);
	}

	private Util() {
	}
}