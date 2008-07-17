/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
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

/**
 * @author raffi
 *
 */
public class ASTUtil {

	private ASTUtil() {
	}

	public static ASTNode getASTNode(final IJavaElement elem,
			final IProgressMonitor monitor) {
		final IMember mem = ASTUtil.getIMember(elem);
		final ICompilationUnit icu = mem.getCompilationUnit();
		if (icu == null)
			throw new IllegalStateException("Source not present.");
		final ASTNode root = ASTUtil.getCompilationUnit(icu, monitor);
		return root;
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
		final IMember mem = ASTUtil.getIMember(elem);
		final CompilationUnit root = ASTUtil.getCompilationUnit(mem
				.getCompilationUnit(), monitor);
		return ASTUtil.getExactASTNode(root, nameRange);
	}

	public static ASTNode getExactASTNode(final IJavaElement elem,
			final SearchMatch match, final IProgressMonitor monitor) {
		final IMember mem = ASTUtil.getIMember(elem);
		final CompilationUnit root = ASTUtil.getCompilationUnit(mem
				.getCompilationUnit(), monitor);
		return ASTUtil.getExactASTNode(root, match);
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

	public static ASTNode getExactASTNode(final SearchMatch match,
			final IProgressMonitor monitor) {
		final IJavaElement elem = (IJavaElement) match.getElement();
		return getExactASTNode(elem, match, monitor);
	}

	public static FieldDeclaration getFieldDeclaration(final ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof FieldDeclaration)
			return (FieldDeclaration) node;
		else
			return ASTUtil.getFieldDeclaration(node.getParent());
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
	
		return ASTUtil.getIMember(elem.getParent());
	}

	public static InfixExpression getInfixExpression(final ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof InfixExpression)
			return (InfixExpression) node;
		else
			return ASTUtil.getInfixExpression(node.getParent());
	}

	public static MethodDeclaration getMethodDeclaration(final ASTNode node) {
		ASTNode trav = node;
		while (trav.getNodeType() != ASTNode.METHOD_DECLARATION)
			trav = trav.getParent();
		return (MethodDeclaration) trav;
	}

	public static SingleVariableDeclaration getSingleVariableDeclaration(
			final ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof SingleVariableDeclaration)
			return (SingleVariableDeclaration) node;
		else
			return ASTUtil.getSingleVariableDeclaration(node.getParent());
	}

	public static Name getTopmostName(final ASTNode node) {
		if (node == null)
			return null;
		else if (node.getParent() == null
				|| node.getParent().getNodeType() != ASTNode.QUALIFIED_NAME)
			return (Name) node;
		else
			return ASTUtil.getTopmostName(node.getParent());
	}

	public static VariableDeclarationStatement getVariableDeclarationStatement(
			final ASTNode node) {
		if (node == null)
			return null;
		else if (node instanceof VariableDeclarationStatement)
			return (VariableDeclarationStatement) node;
		else
			return ASTUtil.getVariableDeclarationStatement(node.getParent());
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
			return ASTUtil.isContainedInCaseLabel(node.getParent());
	}

}
