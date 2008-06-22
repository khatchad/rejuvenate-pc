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
import java.util.List;
import java.util.Set;

import org.aspectj.asm.IProgramElement;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.PackageBuilder;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.IAJCodeElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
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
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.jdom.Attribute;
import org.jdom.Element;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionGraph;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionNode;
import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.Category;
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

	public static IElement convertBinding(final Category category,
			final String readableName) {
		return FlyweightElementFactory.getElement(category, readableName);
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
		Util.makeDotFile(graph, file);
	}

	@SuppressWarnings("unused")
	public static void makeDotFile(
			final IntentionGraph graph,
			final String resultPath) throws IOException {
		final File file = new File(resultPath + "intention_graph.dot");
		Util.makeDotFile(graph, file);
	}

	private Util() {
	}

	/**
	 * @param advElem
	 * @return
	 * @throws JavaModelException
	 */
	public static Set<IJavaElement> getAdvisedJavaElements(AdviceElement advElem)
			throws JavaModelException {
		Set<IJavaElement> ret = new LinkedHashSet<IJavaElement>();
		List<AJRelationship> relationshipList = Util
				.getAdviceRelationshipList(advElem);
		for (final AJRelationship relationship : relationshipList) {
			final IJavaElement advice = relationship.getSource();
			if (advice.equals(advElem)) {
				final IJavaElement target = relationship.getTarget();
				switch (target.getElementType()) {
					case IJavaElement.METHOD: {
						final IMethod meth = (IMethod) target;
						if (meth.getParent() instanceof AspectElement)
							break; //don't consider advice right now.
						ret.add(meth);
						break;
					}
					case IJavaElement.TYPE: {
						// its a default ctor.
						final IType type = (IType) target;
						for (final IMethod meth : type.getMethods())
							if (meth.isConstructor()
									&& meth.getParameterNames().length == 0) {
								ret.add(meth);
							}
						break;
					}
					case IJavaElement.LOCAL_VARIABLE: {
						// its an aspect element.
						if (!(target instanceof IAJCodeElement))
							throw new IllegalStateException(
									"Something is screwy here.");
						ret.add(target);
						break;
					}
					default:
						throw new IllegalStateException(
								"Unexpected relationship target type: "
										+ target.getElementType());
				}
			}
		}
		return ret;
	}

	/**
	 * @param advElem
	 * @return
	 */
	@SuppressWarnings( { "restriction", "unchecked" })
	public static List<AJRelationship> getAdviceRelationshipList(
			final AdviceElement advElem) {
		final IProject proj = advElem.getJavaProject().getProject();
		final List<AJRelationship> relationshipList = AJModel
				.getInstance()
				.getAllRelationships(
						proj,
						new AJRelationshipType[] { AJRelationshipManager.ADVISES });
		return relationshipList;
	}

	/**
	 * @param elem
	 * @return
	 */
	public static Element getXML(IJavaElement elem) {
		Element ret = new Element(elem.getClass().getSimpleName());
		ret.setAttribute(new Attribute("id", elem.getHandleIdentifier()));
		ret.setAttribute(new Attribute("name", elem.getElementName()));
		ret.setAttribute(new Attribute("type", String.valueOf(elem
				.getElementType())));
		return ret;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public static PrintWriter getPrintWriter(final File aFile,
			final boolean append) throws IOException {
		final FileWriter resFileOut = new FileWriter(aFile, append);
		return new PrintWriter(resFileOut);
	}

	/**
	 * @param values
	 * @return
	 */
	public static <E> Collection<E> flattenCollection(
			Collection<? extends Collection<E>> values) {
		Collection<E> ret = new LinkedHashSet<E>();
		for (Collection<E> col : values)
			for (E e : col)
				ret.add(e);
		return ret;
	}

	@SuppressWarnings("restriction")
	public static PrintWriter getXMLFileWriter(AdviceElement advElem)
			throws IOException {
		String fileName = getRelativeXMLFileName(advElem);
		final File aFile = new File(WORKSPACE_LOC, fileName);
		return getPrintWriter(aFile, false);
	}

	/**
	 * @param advElem
	 * @return
	 */
	private static String getRelativeXMLFileName(AdviceElement advElem) {
		StringBuilder fileNameBuilder = new StringBuilder(advElem.getPath()
				.toOSString());
		fileNameBuilder.append("#" + advElem.toDebugString());
		fileNameBuilder.append(".rejuv-pc.xml");
		return fileNameBuilder.toString();
	}

	/**
	 * @param advElem
	 * @return
	 */
	@SuppressWarnings("restriction")
	public static File getSavedXMLFile(AdviceElement advElem) {
		String relativeFileName = getRelativeXMLFileName(advElem);
		File aFile = new File(WORKSPACE_LOC, relativeFileName);
		if (!aFile.exists())
			throw new IllegalArgumentException("No XML file found for advice "
					+ advElem.getElementName());
		return aFile;
	}

	/**
	 * @param pattern
	 * @return
	 */
	public static Collection<SearchMatch> search(final SearchPattern pattern,
			IJavaSearchScope scope, IProgressMonitor monitor) {
		final SearchEngine engine = new SearchEngine();
		final Collection<SearchMatch> results = new ArrayList<SearchMatch>();
		try {
			engine.search(pattern, new SearchParticipant[] { SearchEngine
					.getDefaultSearchParticipant() }, scope,
					new SearchRequestor() {

						@Override
						public void acceptSearchMatch(final SearchMatch match)
								throws CoreException {
							if (match.getAccuracy() == SearchMatch.A_ACCURATE
									&& !match.isInsideDocComment())
								results.add(match);
						}
					}, monitor);
		}
		catch (final NullPointerException e) {
			System.err.println("Caught " + e
					+ " from search engine. Rethrowing.");
			throw e;
		}
		catch (final CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}

	public static Collection<SearchMatch> search(final SearchPattern pattern,
			IJavaSearchScope scope, final ISourceRange range, IProgressMonitor monitor) {
		final SearchEngine engine = new SearchEngine();
		final Collection<SearchMatch> results = new ArrayList<SearchMatch>();
		try {
			engine.search(pattern, new SearchParticipant[] { SearchEngine
					.getDefaultSearchParticipant() }, scope,
					new SearchRequestor() {

						@Override
						public void acceptSearchMatch(final SearchMatch match)
								throws CoreException {
							if (match.getAccuracy() == SearchMatch.A_ACCURATE
									&& !match.isInsideDocComment()
									&& match.getOffset() == range.getOffset()
									&& match.getLength() == range.getLength())
								results.add(match);
						}
					}, monitor);
		}
		catch (final NullPointerException e) {
			System.err.println("Caught " + e
					+ " from search engine. Rethrowing.");
			throw e;
		}
		catch (final CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}

	public static Collection<SearchMatch> search(final SearchPattern pattern, IProgressMonitor monitor) {
		return search(pattern, SearchEngine.createWorkspaceScope(), monitor);
	}
}