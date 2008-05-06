/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.14 $
 */

package ca.mcgill.cs.swevo.jayfx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.weaver.AsmRelationshipProvider;
import org.aspectj.weaver.AsmRelationshipUtils;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.PointcutElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJProjectModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ca.mcgill.cs.swevo.jayfx.model.ClassElement;
import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.MethodElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * Traversing the AST of a compilation unit and inserting all the information
 * gathered to the database.
 */
/*
 * <ul>
 * <li>	Class Initializer and Constructor
 *   	The class initializer method < clinit >() is a virtual method. There
 * is no such a Node for this method. Therefore the creation of such method
 * is inside type declaration node. An empty clinit method is created when
 * the visitor enounter a top level class. During the time before the endVisit
 * method is called, if any static field is initialized, some relations will
 * be added to this clinit class. When endVisit is called, the visitor checks
 * to see if any relation is recorded in the database under this method. If
 * there are some, A relation of DECLARES will be drawn between the current
 * type and clinit method.
 * 		The < temp >() method has similar use. The difference is that temp method
 * serves the purpose of keeping track of non-static initializations. Any
 * such kind of relations that are recored under temp method will be added
 * to all the constructors of the current type. At the end of visiting this
 * type declaration, this temp method WILL be completely removed from database.
 * 
 * <li>	Reminder mechanism:
 *   	When the visitor visit a type declaration node, it saves this type into
 * field aCurrType. Before the endVisit for this type is called, the visitor
 * might enounter another type declaration node. This could be the case
 * of any kind of nested type. When this happens, the current type will be
 * saved, or pushed, into a backup stack -- aCurrTypeReminder. The nested 
 * type will become the current type. This helps to reduce the complexity of
 * all the other visit methods, because they do not have to know whether the
 * current method is a nested method or not. After all the nodes of the outer
 * layer type declaration are visited, the endVisit method will be called.
 * Then, the back-uped type will be restored from the stack.
 *   	Other stacks are aCurrMethodReminder and aTempMethodReminder. They have
 * the save function as the reminder for type declaration. Because the use of
 * stack as reminder, the reminder mechanism support infinit number of nestings,
 * although in most cases we only see two layers of nesting.
 * 
 * <li>	Error Compatible Traversing
 * 		Whenever we call resolveBinding(), we should check to see if this binding is
 * null. Because we try to make this class compatible with erroneous java files,
 * there could be cases where bindings cannot be resolved. This class works
 * with unresolved classes, methods, or field, but it is might not be able to
 * pass a java file with structural errors.
 * </ul>
 */
public class ASTCrawler extends ASTVisitor {
	// This is an optimization flag tagged onto the modifiers for IElements.  It represents
	// the declaration of an abstract definition (as opposed to an implementation.
	// With an type, it designates an interface.  With a method, it defines either an interface
	// method or an abstract method.
	private static final int ABSTRACT_FLAG = 16384;
	private static final String aTEMP_METHOD_NAME = "<temp>()";
	private static final String aINIT_METHOD_NAME = "<init>()";
	private static final String aCLINIT_METHOD_NAME = "<clinit>()";
	private static final int aINIT_CONSTRUCTOR_LIST_SIZE = 5;

	private static final int aINIT_METHOD_MODIFIERS = 8; //8 is the modifier integer of an initializer block

	/**
	 * Encapsulates behavior to report null objects.
	 * 
	 * @param pObject
	 *            Object to check.
	 */
	private static boolean checkForNull(Object pObject) {
		boolean lReturn = false;
		if (pObject == null) {
			//			Thread.dumpStack();
			lReturn = true;
		}
		return lReturn;
	}

	/**
	 * Converts a method binding to a method element.
	 * 
	 * @param pBinding
	 *            The binding to covert. Cannot be null.
	 * @return A method element corresponding to pBinding. Never null.
	 */
	private static IElement convertBinding(IMethodBinding pBinding) {
		checkForNull(pBinding);
		String lReturn = null;
		try {
			lReturn = convertBinding(pBinding.getDeclaringClass()).getId()
					+ ".";
		}
		catch (NullPointerException E) {
			E.printStackTrace();
			throw E;
		}
		if (pBinding.isConstructor()) {
			lReturn += "<init>";
		}
		else {
			lReturn += pBinding.getName();
		}
		lReturn += "(";
		ITypeBinding lParamBindings[] = pBinding.getParameterTypes();
		for (int i = 0; i < lParamBindings.length - 1; i++) {
			lReturn += convertParameterTypeBinding(lParamBindings[i]).getId();
			lReturn += ",";
		}
		if (lParamBindings.length > 0) {
			lReturn += convertParameterTypeBinding(
					lParamBindings[lParamBindings.length - 1]).getId();
		}
		lReturn += ")";

		return FlyweightElementFactory.getElement(ICategories.METHOD, lReturn,
				pBinding.getJavaElement());
	}

	/**
	 * Converts a type binding to a class element or an enum element.
	 * 
	 * @param pBinding
	 *            The binding to convert. Cannot be null.
	 * @return A class or enum element representing this binding. Cannot be
	 *         null.
	 */
	private static IElement convertBinding(ITypeBinding pBinding) {
		checkForNull(pBinding);
		IJavaElement elem = null;
		try {
			elem = pBinding.getJavaElement();
		}
		catch (NullPointerException E) {
			System.out.println("Bug in eclipse encountered for: "
					+ pBinding.getName());
			return null;
		}
		return FlyweightElementFactory.getElement(ICategories.CLASS, pBinding
				.getBinaryName(), elem);
	}

	/**
	 * Converts a variable binding to a field element.
	 * 
	 * @param pBinding
	 *            The binding to convert. Cannot be null.
	 * @return A field element representing this binding. Cannot be null.
	 */
	private static IElement convertBinding(IVariableBinding pBinding) {
		checkForNull(pBinding);
		String lFieldID = convertBinding(pBinding.getDeclaringClass()).getId()
				+ "." + pBinding.getName();
		return FlyweightElementFactory.getElement(ICategories.FIELD, lFieldID,
				pBinding.getJavaElement());
	}

	/**
	 * Converts a type binding into a parameter-style binding Please see JVM
	 * Specification 4.3.2
	 * {@link http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#84645}.
	 * 
	 * @param pBinding
	 *            The binding to convert. Cannot be null.
	 * @return A class element representing this binding. Cannot be null.
	 */
	private static IElement convertParameterTypeBinding(ITypeBinding pBinding) {
		checkForNull(pBinding);
		if ((pBinding.getDimensions() == 0) && !(pBinding.isPrimitive()))
			return FlyweightElementFactory.getElement(ICategories.CLASS,
					Signature.C_RESOLVED + pBinding.getBinaryName()
							+ Signature.C_SEMICOLON, pBinding.getJavaElement());
		else
			return FlyweightElementFactory.getElement(ICategories.CLASS,
					pBinding.getBinaryName(), pBinding.getJavaElement());
	}

	/**
	 * Standard logging behavior
	 */
	private static void log(String pMessage) {
		System.out.println(pMessage);
	}

	private ClassElement aCurrType;
	private MethodElement aCurrMethod;
	private MethodElement aTempMethod; // for field initializations

	private Stack<MethodElement> aTempMethodReminder;

	private Stack<ClassElement> aCurrTypeReminder; // for anonymous/inner class situations

	private Stack<MethodElement> aCurrMethodReminder; // for anonymous/inner class situations and <clinit> method situations

	private Stack<ArrayList<MethodElement>> aCurrConstructorListReminder; // for all constructor methods

	private ArrayList<MethodElement> aCurrConstructorList;

	//private boolean			aHasConstructor = false;

	//private BooleanStack	aHaaConstructorReminder;

	private ProgramDatabase aDB;

	private FastConverter aConverter;

	/**
	 * Constructor of ASTSpider working on a given database. A new DBManager is
	 * created.
	 * 
	 * @param pDatabase
	 *            Can be empty or can contain data
	 */
	public ASTCrawler(ProgramDatabase pDB, FastConverter pConverter) {
		aDB = pDB;
		aConverter = pConverter;
	}

	/**
	 * Add an ACCESS relation and its transpose between aCurrentMethod and the
	 * field described by pBinding. If any problem is detected the relation is
	 * not added and the error is logged.
	 * 
	 * @param pBinding
	 *            The field to add a relation to.
	 * @pre aCurrMethod is not null
	 * @pre aCurrMethod is in the database
	 * @pre pBinding is not null
	 */
	private void addAccessRelation(IVariableBinding pBinding) {
		assert (pBinding != null);
		assert (aDB.contains(aCurrMethod));
		assert (aCurrMethod != null);

		if (pBinding.getDeclaringClass() == null) {
			// This is most likely an access to the length
			// field of an array.
			return;
		}
		IElement lField = convertBinding(pBinding);
		aDB.addElement(lField, pBinding.getModifiers());
		aDB.addRelationAndTranspose(aCurrMethod, Relation.ACCESSES, lField);
	}

	private void addSetsRelation(IVariableBinding pBinding) {
		assert (pBinding != null);
		assert (aDB.contains(aCurrMethod));
		assert (aCurrMethod != null);

		if (pBinding.getDeclaringClass() == null) {
			// This is most likely an access to the length
			// field of an array.
			return;
		}
		IElement lField = convertBinding(pBinding);
		aDB.addElement(lField, pBinding.getModifiers());
		aDB.addRelationAndTranspose(aCurrMethod, Relation.SETS, lField);
	}

	private void addGetsRelation(IVariableBinding pBinding) {
		assert (pBinding != null);
		assert (aDB.contains(aCurrMethod));
		assert (aCurrMethod != null);

		if (pBinding.getDeclaringClass() == null) {
			// This is most likely an access to the length
			// field of an array.
			return;
		}
		IElement lField = convertBinding(pBinding);
		aDB.addElement(lField, pBinding.getModifiers());
		aDB.addRelationAndTranspose(aCurrMethod, Relation.GETS, lField);
	}

	private void addCallRelation(ASTNode pNode, IMethodBinding pBinding,
			boolean pStatic) {
		//assert( pBinding != null ); TODO

		if (aCurrMethod == null) {
			//constructors calling itself. Ignore it.
			return;
		}

		//lAcceptor could be a Java.util method
		if (pBinding == null)
			return;
		IElement lAcceptor = convertBinding(pBinding);
		IElement lCaller = aCurrMethod;

		// lCaller instanceof IClassElement
		// lAcceptor instanceof IMethodElement
		int lModifiers = pBinding.getModifiers();
		if ((pBinding.getDeclaringClass().isInterface())
				|| (Modifier.isAbstract(lModifiers))) {
			lModifiers = lModifiers | ABSTRACT_FLAG;
		}

		aDB.addElement(lAcceptor, lModifiers);

		if (pStatic) {
			aDB.addRelationAndTranspose(lCaller, Relation.STATIC_CALLS,
					lAcceptor);
		}
		else {
			aDB.addRelationAndTranspose(lCaller, Relation.CALLS, lAcceptor);
		}
	}

	/**
	 * Parsing a compilation unit. Starting point of the AST traversal.
	 * 
	 * @param pCU
	 */
	@SuppressWarnings( { "restriction", "unchecked" })
	public void analyze(ICompilationUnit pCU) {
		resetSpider();

		extractTypes(pCU);
		ASTParser lParser = ASTParser.newParser(AST.JLS3); // handles JLS3 (J2SE 1.5)
		lParser.setSource(pCU);
		lParser.setResolveBindings(true);
		CompilationUnit lResult = (CompilationUnit) lParser.createAST(null);
		lResult.accept(this);
	}

	public void endVisit(AnonymousClassDeclaration pNode) {
		aCurrType = aCurrTypeReminder.pop();
	}

	public void endVisit(EnumConstantDeclaration pNode) {
		restoreMethodRelation();
	}

	public void endVisit(EnumDeclaration pNode) {
		restoreTypeRelation();
	}

	public void endVisit(FieldDeclaration pNode) {
		aCurrMethod = aCurrMethodReminder.pop();
	}

	public void endVisit(Initializer pNode) {
		if (!aCurrMethodReminder.isEmpty()) {
			aCurrMethod = aCurrMethodReminder.pop();
		}
		else {
			aCurrMethod = null;
		}
	}

	public void endVisit(MethodDeclaration pNode) {
		restoreMethodRelation();
	}

	//
	public void endVisit(TypeDeclaration pNode) {
		restoreTypeRelation();
	}

	// Extracts types and loads them into the converter
	private void extractTypes(ICompilationUnit pCU) {
		try {
			IType[] lTypes = pCU.getAllTypes();

			for (int i = 0; i < lTypes.length; i++) {
				aConverter.addMapping(lTypes[i]);
			}
		}
		catch (JavaModelException pException) {
			pException.printStackTrace();
		}
	}

	private boolean isNonPrimitive(ITypeBinding pBinding) {
		//As long as it's not of primitive type or primitive type array
		if (pBinding.isPrimitive()) {
			return false;
		}
		else if (pBinding.isArray()) {
			ITypeBinding lBinding = pBinding.getElementType();
			return !lBinding.isPrimitive();
		}

		return true;
	}

	/**
	 * Clear class fields and be ready to work on a different compilation unit.
	 */
	private void resetSpider() {
		//		aHasConstructor = false;
		//		aHasConstructorReminder = new BooleanStack();
		aCurrConstructorList = null;
		aCurrConstructorListReminder = new Stack<ArrayList<MethodElement>>();
		aCurrType = null;
		aCurrTypeReminder = new Stack<ClassElement>();
		aCurrMethod = null;
		aCurrMethodReminder = new Stack<MethodElement>();
		aTempMethod = null;
		aTempMethodReminder = new Stack<MethodElement>();
	}

	private void restoreMethodRelation() {
		if (!aCurrMethodReminder.isEmpty()) {
			aCurrMethod = aCurrMethodReminder.pop();
		}
		else {
			aCurrMethod = null;
		}
	}

	/**
	 * Conclusion method for the visit on this Type Declaration and Enum
	 * Declaration. This type could be an inner class. This method checks to see
	 * if there is an actual (non-empty) <clinit>() method. If the method does
	 * exist and has relations in it, a DECLARES relations is added to the
	 * current type for this method. Then it removes current <temp> method
	 * completely from database And restore backup <temp>, if there is one.
	 * Should only be called by endVisit( EnumDeclaration ) and endVisit(
	 * TypeDeclaration ).
	 * 
	 */
	private void restoreTypeRelation() {
		//		if this <clinit> method has relations in it, the relation DECLARES is added to the current type
		MethodElement lMethod = (MethodElement) FlyweightElementFactory
				.getElement(ICategories.METHOD, aCurrType.getId() + "."
						+ aCLINIT_METHOD_NAME, null);
		if (aDB.contains(lMethod) && aDB.hasRelations(lMethod)) {
			aDB.addRelation(aCurrType, Relation.DECLARES_METHOD, lMethod);
		}

		if (aDB.hasRelations(aTempMethod)) {
			if (aCurrConstructorList.size() == 0) {
				IElement lDefaultConstructor = FlyweightElementFactory
						.getElement(ICategories.METHOD, aCurrType.getId() + "."
								+ aINIT_METHOD_NAME, null);
				aDB.addElement(lDefaultConstructor, aINIT_METHOD_MODIFIERS);
				aDB.copyRelations(aTempMethod, lDefaultConstructor);
				aDB.addRelation(aCurrType, Relation.DECLARES_METHOD,
						lDefaultConstructor);
			}
			else {
				for (MethodElement lConstructor : aCurrConstructorList) {
					aDB.copyRelations(aTempMethod, lConstructor);
				}
			}
		}
		//Remove temp method, and any relations associated with it, from memory database
		aDB.removeElement(aTempMethod);

		//restore current type and temp method
		if (!aCurrTypeReminder.isEmpty()) {
			aCurrType = aCurrTypeReminder.pop();
		}
		else {
			aCurrType = null;
		}

		if (!aTempMethodReminder.isEmpty()) {
			aTempMethod = aTempMethodReminder.pop();
		}
		else {
			aTempMethod = null;
		}

		if (!aCurrConstructorListReminder.empty()) {
			aCurrConstructorList = aCurrConstructorListReminder.pop();
		}
		else {
			aCurrConstructorList = null;
		}
	}

	private void saveMethodRelation(IMethodBinding pMBinding) {

		if (aCurrMethod != null) {
			aCurrMethodReminder.push(aCurrMethod);
		}
		aCurrMethod = (MethodElement) convertBinding(pMBinding);

		int lModifiers = pMBinding.getModifiers();
		if ((pMBinding.getDeclaringClass().isInterface())
				|| (Modifier.isAbstract(lModifiers))) {
			lModifiers = lModifiers | ABSTRACT_FLAG;
		}
		aDB.addElement(aCurrMethod, lModifiers);
	}

	private void saveTypeRelation(ITypeBinding pBinding) {
		if (pBinding.isTopLevel()) { //Assume both reminder stacks are empty

		}
		else //other wise, backup current type.
		{
			aCurrTypeReminder.push(aCurrType);
			aTempMethodReminder.push(aTempMethod);
			aCurrConstructorListReminder.push(aCurrConstructorList);
		}

		// Initialize the list for storing constructor declaration
		aCurrConstructorList = new ArrayList<MethodElement>(
				aINIT_CONSTRUCTOR_LIST_SIZE); // 

		//Insert this type
		aCurrType = (ClassElement) convertBinding(pBinding);
		aDB.addElement(aCurrType, pBinding.getModifiers()
				| (pBinding.isInterface() ? ABSTRACT_FLAG : 0));

		//Insert temp method for field initializers
		aTempMethod = (MethodElement) FlyweightElementFactory.getElement(
				ICategories.METHOD,
				aCurrType.getId() + "." + aTEMP_METHOD_NAME, pBinding
						.getJavaElement());
		aDB.addElement(aTempMethod, pBinding.getModifiers());
	}

	public boolean visit(ImportDeclaration node) {
		return false;
	}

	public boolean visit(AnonymousClassDeclaration pNode) {
		ITypeBinding lBinding = pNode.resolveBinding();

		if (checkForNull(lBinding))
			return false;
		if (checkForNull(aCurrType))
			return false;

		IElement lAnonymousClass = convertBinding(lBinding);
		aCurrTypeReminder.push(aCurrType);
		aCurrType = (ClassElement) lAnonymousClass;
		aDB.addElement(aCurrType, pNode.resolveBinding().getModifiers());
		aDB.addRelation((IElement) aCurrMethod, Relation.DECLARES_TYPE,
				aCurrType);

		ITypeBinding lSuperBinding = lBinding.getSuperclass();
		if (lSuperBinding != null) {
			IElement lSuperClass = convertBinding(lSuperBinding);
			aDB.addElement(lSuperClass, lSuperBinding.getModifiers());
			aDB.addRelationAndTranspose(aCurrType, Relation.EXTENDS_CLASS,
					lSuperClass);
		}

		ITypeBinding lInterfaceBindings[] = lBinding.getInterfaces();
		for (int i = 0; i < lInterfaceBindings.length; i++) {
			IElement lInterface = convertBinding(lInterfaceBindings[i]);
			aDB.addElement(lInterface, lInterfaceBindings[i].getModifiers()
					| ABSTRACT_FLAG);
			aDB.addRelationAndTranspose(aCurrType,
					Relation.IMPLEMENTS_INTERFACE, lInterface);
		}
		return true;
	}

	public boolean visit(CastExpression pNode) {
		assert (aCurrMethod != null);

		ITypeBinding lBinding = pNode.resolveTypeBinding();

		if (lBinding != null) {
			IElement lClass = convertBinding(lBinding);
			aDB.addElement(lClass, lBinding.getModifiers()
					| (lBinding.isInterface() ? ABSTRACT_FLAG : 0));
			aDB.addRelationAndTranspose(aCurrMethod, Relation.CHECKS, lClass);
		}

		return true;
	}

	public boolean visit(ClassInstanceCreation pNode) {
		if (checkForNull(aCurrMethod))
			return false;

		IMethodBinding lCBinding = pNode.resolveConstructorBinding();
		ITypeBinding lTBinding = pNode.resolveTypeBinding();

		if (checkForNull(lCBinding))
			return false;
		if (checkForNull(lTBinding))
			return false;

		MethodElement lConstructor = null;

		if (lTBinding.isAnonymous()) {
			IElement lDeclaringClass = convertBinding(lTBinding);
			// TODO HACK A bug in Eclipse occasionally causes binary names to crap out.
			if (lDeclaringClass == null || lDeclaringClass.getId() == null)
				return false;

			lConstructor = (MethodElement) FlyweightElementFactory.getElement(
					ICategories.METHOD, lDeclaringClass.getId() + "."
							+ aINIT_METHOD_NAME, lTBinding.getJavaElement());
			aDB.addElement(lConstructor, aINIT_METHOD_MODIFIERS);
		}
		else {
			lConstructor = (MethodElement) convertBinding(lCBinding);
		}

		IElement lClass = lConstructor.getDeclaringClass();

		//Register CALLS relationship to constructor
		addCallRelation(pNode, lCBinding, true);

		try {
			aDB.contains(lClass);
		}
		catch (RuntimeException pException) {
			System.out.println(lClass.getId());
			System.out.println(lConstructor.getId());
			throw pException;
		}

		if (!aDB.contains(lClass)) {
			ITypeBinding lType = lCBinding.getDeclaringClass();
			aDB.addElement(lClass, lType.getModifiers());
		}

		//Register CREATES relationship
		aDB.addRelationAndTranspose(aCurrMethod, Relation.CREATES, lClass);

		return true;
	}

	public boolean visit(ConstructorInvocation pNode) {
		addCallRelation(pNode, pNode.resolveConstructorBinding(), true);
		return true;
	}

	public boolean visit(EnumConstantDeclaration pNode) {
		// JLS3(§8.9): It is impossible to define a local (§14.3) enum, or to define an enum in an inner class (§8.1.3).
		assert (aCurrMethodReminder.empty());

		String lSimpleName = pNode.getName().getIdentifier();
		if (lSimpleName == null)
			return false;

		IElement lField;
		lField = FlyweightElementFactory.getElement(ICategories.FIELD,
				aCurrType.getId() + "." + lSimpleName, pNode.resolveVariable()
						.getJavaElement());
		aDB.addElement(lField, pNode.getModifiers());
		aDB.addRelation(aCurrType, Relation.DECLARES_FIELD, lField);

		//	Register CALLS relationship to constructor
		//		IMethodBinding lCBinding = pNode.resolveConstructorBinding();
		//		if( checkForNull( lCBinding )) return false;
		//		saveMethodRelation( lCBinding );
		//		return true; 
		return false;
	}

	/**
	 * Generated the DECLARES relations between a enum and and nested types.
	 */
	public boolean visit(EnumDeclaration pNode) {
		ITypeBinding lBinding = pNode.resolveBinding();
		assert (lBinding.isEnum());

		if (checkForNull(lBinding))
			return false;

		try {
			saveTypeRelation(lBinding);

			// JLS3(§8.9): It is impossible to define a local (§14.3) enum, or to define an enum in an inner class (§8.1.3).
			// TODO: check if enum type is always a member class of an closing class
			if (lBinding.isMember()) {
				aDB.addRelation(aCurrTypeReminder.peek(),
						Relation.DECLARES_TYPE, aCurrType);
			}

			// Find interfaces.
			ITypeBinding lInterfaceBindings[] = lBinding.getInterfaces();
			for (int i = 0; i < lInterfaceBindings.length; i++) {
				IElement lInterface = convertBinding(lInterfaceBindings[i]);
				aDB.addElement(lInterface, lInterfaceBindings[i].getModifiers()
						| ABSTRACT_FLAG);
				aDB.addRelationAndTranspose(aCurrType,
						Relation.IMPLEMENTS_INTERFACE, lInterface);
			}
		}
		catch (Exception pException) {
			ProblemManager.reportException(pException);
		}

		return true;
	}

	public boolean visit(FieldAccess pNode) {
		IVariableBinding lBinding = (IVariableBinding) pNode.getName()
				.resolveBinding();

		if (lBinding == null) {
			log("Null binding 1 for " + pNode.toString());
			return false;
		}

		addAccessRelation(lBinding);
		Assignment assignment = getAssignment(pNode);
		if (assignment != null) {
			addSetsRelation((IVariableBinding) lBinding);

			if (!(assignment.getOperator() == Assignment.Operator.ASSIGN))
				addGetsRelation((IVariableBinding) lBinding);
		}
		else
			addGetsRelation((IVariableBinding) lBinding);
		return true;
	}

	/*
	 * Add information about one or more fields in this field declaration statment
	 * The method also desides to put "access" relation to Class Initializer or
	 * Constructor depending on if the method is static or not, and if it's initalized
	 * with a value or not.
	 */
	public boolean visit(FieldDeclaration pNode) {
		List fragments = pNode.fragments();
		IElement lField;

		aCurrMethodReminder.push(aCurrMethod);

		if (Modifier.isStatic(pNode.getModifiers())) {
			aCurrMethod = (MethodElement) FlyweightElementFactory.getElement(
					ICategories.METHOD, aCurrType.getId() + "."
							+ aCLINIT_METHOD_NAME, null);
			if (!aDB.contains(aCurrMethod)) {
				//This <clinit>() method will be in any class that has a static field with initialization
				//But the DECLARES relation will be linked only if this method has at least one sub relations
				//The linkage is done at the end of traversing each compilations unit while end visiting type Declaration
				aDB.addElement(aCurrMethod, pNode.getModifiers());
			}
		}
		else {
			aCurrMethod = aTempMethod;
		}

		// Consider multiple declaration in one statment
		for (Iterator itr = fragments.iterator(); itr.hasNext();) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) itr
					.next();
			String lSimpleName = fragment.getName().getIdentifier();
			Expression lInit = fragment.getInitializer();

			if (lSimpleName != null) {
				lField = FlyweightElementFactory.getElement(ICategories.FIELD,
						aCurrType.getId() + "." + lSimpleName, null);
				aDB.addElement(lField, pNode.getModifiers());

				aDB.addRelation(aCurrType, Relation.DECLARES_FIELD, lField);

				//If there is any initialization to this field then we write them as an access by <init> or <clinit>
				if (lInit != null) {
					aDB.addRelationAndTranspose(aCurrMethod, Relation.ACCESSES,
							lField);
					aDB.addRelationAndTranspose(aCurrMethod, Relation.SETS,
							lField);

					// Want to go into the right side of assignment operator
					lInit.accept(this);
				}
			}
		}

		//Because we have covered everything we need about field declaration,
		//we dont' have to go deeper into this node.
		//		return false;
		return true;
	}

	public boolean visit(Initializer pNode) {
		if (aCurrMethod != null) {
			aCurrMethodReminder.push(aCurrMethod);
		}

		if (Flags.isStatic(pNode.getModifiers())) {
			aCurrMethod = (MethodElement) FlyweightElementFactory.getElement(
					ICategories.METHOD, aCurrType.getId() + "."
							+ aCLINIT_METHOD_NAME, null);
		}
		else {
			aCurrMethod = (MethodElement) FlyweightElementFactory.getElement(
					ICategories.METHOD, aCurrType.getId() + "."
							+ aINIT_METHOD_NAME, null);
			aCurrConstructorList.add(aCurrMethod);
		}

		aDB.addElement(aCurrMethod, pNode.getModifiers());
		aDB.addRelation(aCurrType, Relation.DECLARES_METHOD, aCurrMethod);
		return true;
	}

	public boolean visit(InstanceofExpression pNode) {
		if (checkForNull(aCurrMethod))
			return false;

		ITypeBinding lBinding = pNode.getRightOperand().resolveBinding();
		if (lBinding != null) {
			IElement lClass = convertBinding(lBinding);
			aDB.addElement(lClass, lBinding.getModifiers()
					| (lBinding.isInterface() ? ABSTRACT_FLAG : 0));
			aDB.addRelationAndTranspose(aCurrMethod, Relation.CHECKS, lClass);
		}
		return true;
	}

	/*
	 * Add the method to database. Add "Declares" relation to Class.
	 * If it's a constructor, copy all relations in temp method
	 */
	@SuppressWarnings("restriction")
	public boolean visit(MethodDeclaration pNode) {
		IMethodBinding lMBinding = pNode.resolveBinding();

		if (checkForNull(lMBinding))
			return false;
		saveMethodRelation(lMBinding);

		aDB.addRelation(aCurrType, Relation.DECLARES_METHOD, aCurrMethod);

		//If this is a constructor, we dump the class initilization relations into the constructor
		if (lMBinding.isConstructor()) {
			aCurrConstructorList.add(aCurrMethod);
			//			aDB.copyRelations( aTempMethod, aCurrMethod ); // TODO: But what if there're fields that have not yet be parsed?

			// Note: Now the relation copying occurs at the end of type declaration 
		}

		//		System.out.println(this.aCurrMethod);
		//		IJavaElement elem = lMBinding.getJavaElement();
		//		if ( AJModel.getInstance().isAdvised(elem) ) {
		//			List<AdviceElement> applicableAdvice = getApplicableAdvice(elem);
		//			for (AdviceElement advice: applicableAdvice ) {
		//				if ( advice != null && advice.equals(this.selectedAdvice) ) {
		//    				IElement adviceElem = Utils.convertBinding(ICategories.ADVICE, advice.getHandleIdentifier());
		//    				try {
		//    					this.aDB.addElement(adviceElem, advice.getFlags());
		//    				} catch (JavaModelException e) {
		//    					// TODO Auto-generated catch block
		//    					e.printStackTrace();
		//    				}
		//    				
		//    				this.aDB.addRelation(adviceElem, Relation.ADVISES, aCurrMethod);
		//				}
		//			}
		//		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static List<AdviceElement> getApplicableAdvice(IJavaElement je) {
		List<AdviceElement> advisedBy = new ArrayList<AdviceElement>();
		List<AdviceElement> direct = AJModel.getInstance().getRelatedElements(
				AJRelationshipManager.ADVISED_BY, je);
		if (direct != null)
			advisedBy.addAll(direct);

		// check for advised code elements
		List extras = AJModel.getInstance().getExtraChildren(je);
		if (extras != null) {
			for (Iterator iter = extras.iterator(); iter.hasNext();) {
				IJavaElement element = (IJavaElement) iter.next();
				List<AdviceElement> indirect = AJModel.getInstance()
						.getRelatedElements(AJRelationshipManager.ADVISED_BY,
								element);
				if (indirect != null) {
					advisedBy.addAll(indirect);
				}
			}
		}
		return advisedBy;
	}

	public boolean visit(MethodInvocation pNode) {
		SimpleName lName = pNode.getName();
		IBinding lBinding = lName.resolveBinding();

		if (checkForNull(lBinding))
			return false;

		IMethodBinding lMethod = (IMethodBinding) lBinding;
		addCallRelation(pNode, lMethod, Modifier.isStatic(lMethod
				.getModifiers()));
		return true;
	}

	public boolean visit(MarkerAnnotation node) {
		ITypeBinding binding = node.resolveTypeBinding();
		if (checkForNull(binding))
			return false;
		IElement annoteElem = convertBinding(binding);
		aDB.addElement(annoteElem, binding.getModifiers());

		ASTNode annotatedNode = node.getParent();
		switch (annotatedNode.getNodeType()) {
			case ASTNode.METHOD_DECLARATION: {
				MethodDeclaration annotatedMethod = (MethodDeclaration) annotatedNode;
				IMethodBinding mBinding = annotatedMethod.resolveBinding();
				return addAnnotationRelation(annoteElem, mBinding);

			}
			case ASTNode.ANNOTATION_TYPE_DECLARATION: {
				AnnotationTypeDeclaration annotatedAnnotation = (AnnotationTypeDeclaration) annotatedNode;
				ITypeBinding tBinding = annotatedAnnotation.resolveBinding();
				return addAnnotationRelation(annoteElem, tBinding);
			}

			case ASTNode.VARIABLE_DECLARATION_FRAGMENT: {
				VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) annotatedNode;
				IVariableBinding vBinding = varDeclFrag.resolveBinding();
				return addAnnotationRelation(annoteElem, vBinding);
			}

			case ASTNode.PACKAGE_DECLARATION: {
				PackageDeclaration packDecl = (PackageDeclaration) annotatedNode;
				IPackageBinding pBinding = packDecl.resolveBinding();
				return addAnnotationRelation(annoteElem, pBinding);
			}

			case ASTNode.SINGLE_VARIABLE_DECLARATION: {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) annotatedNode;
				IVariableBinding vBinding = svd.resolveBinding();
				return addAnnotationRelation(annoteElem, vBinding);
			}
			case ASTNode.TYPE_DECLARATION: {
				TypeDeclaration tDecl = (TypeDeclaration) annotatedNode;
				ITypeBinding tBinding = tDecl.resolveBinding();
				return addAnnotationRelation(annoteElem, tBinding);
			}
			case ASTNode.ENUM_DECLARATION: {
				EnumDeclaration eDecl = (EnumDeclaration) annotatedNode;
				ITypeBinding tBinding = eDecl.resolveBinding();
				return addAnnotationRelation(annoteElem, tBinding);
			}

			case ASTNode.FIELD_DECLARATION: {
				FieldDeclaration fieldDecl = (FieldDeclaration) annotatedNode;
				for (Object obj : fieldDecl.fragments()) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) obj;
					IVariableBinding vBinding = vdf.resolveBinding();
					return addAnnotationRelation(annoteElem, vBinding);
				}
			}

			default: {
				throw new IllegalStateException("Illegal annotated node type: "
						+ annotatedNode);
			}
		}
	}

	public boolean visit(NormalAnnotation node) {
		ITypeBinding binding = node.resolveTypeBinding();
		if (checkForNull(binding))
			return false;
		IElement annoteElem = convertBinding(binding);
		aDB.addElement(annoteElem, binding.getModifiers());

		ASTNode annotatedNode = node.getParent();
		switch (annotatedNode.getNodeType()) {
			case ASTNode.METHOD_DECLARATION: {
				MethodDeclaration annotatedMethod = (MethodDeclaration) annotatedNode;
				IMethodBinding mBinding = annotatedMethod.resolveBinding();
				return addAnnotationRelation(annoteElem, mBinding);

			}
			case ASTNode.ANNOTATION_TYPE_DECLARATION: {
				AnnotationTypeDeclaration annotatedAnnotation = (AnnotationTypeDeclaration) annotatedNode;
				ITypeBinding tBinding = annotatedAnnotation.resolveBinding();
				return addAnnotationRelation(annoteElem, tBinding);
			}

			case ASTNode.VARIABLE_DECLARATION_FRAGMENT: {
				VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) annotatedNode;
				IVariableBinding vBinding = varDeclFrag.resolveBinding();
				return addAnnotationRelation(annoteElem, vBinding);
			}

			case ASTNode.PACKAGE_DECLARATION: {
				PackageDeclaration packDecl = (PackageDeclaration) annotatedNode;
				IPackageBinding pBinding = packDecl.resolveBinding();
				return addAnnotationRelation(annoteElem, pBinding);
			}

			case ASTNode.SINGLE_VARIABLE_DECLARATION: {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) annotatedNode;
				IVariableBinding vBinding = svd.resolveBinding();
				return addAnnotationRelation(annoteElem, vBinding);
			}
			case ASTNode.TYPE_DECLARATION: {
				TypeDeclaration tDecl = (TypeDeclaration) annotatedNode;
				ITypeBinding tBinding = tDecl.resolveBinding();
				return addAnnotationRelation(annoteElem, tBinding);
			}
			case ASTNode.ENUM_DECLARATION: {
				EnumDeclaration eDecl = (EnumDeclaration) annotatedNode;
				ITypeBinding tBinding = eDecl.resolveBinding();
				return addAnnotationRelation(annoteElem, tBinding);
			}
			case ASTNode.FIELD_DECLARATION: {
				FieldDeclaration fieldDecl = (FieldDeclaration) annotatedNode;
				for (Object obj : fieldDecl.fragments()) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) obj;
					IVariableBinding vBinding = vdf.resolveBinding();
					return addAnnotationRelation(annoteElem, vBinding);
				}
			}
			default: {
				throw new IllegalStateException("Illegal annotated node type: "
						+ annotatedNode);
			}
		}
	}

	public boolean visit(SingleMemberAnnotation node) {
		ITypeBinding binding = node.resolveTypeBinding();
		if (checkForNull(binding))
			return false;
		IElement annoteElem = convertBinding(binding);
		aDB.addElement(annoteElem, binding.getModifiers());

		ASTNode annotatedNode = node.getParent();
		switch (annotatedNode.getNodeType()) {
			case ASTNode.METHOD_DECLARATION: {
				MethodDeclaration annotatedMethod = (MethodDeclaration) annotatedNode;
				IMethodBinding mBinding = annotatedMethod.resolveBinding();
				return addAnnotationRelation(annoteElem, mBinding);

			}
			case ASTNode.ANNOTATION_TYPE_DECLARATION: {
				AnnotationTypeDeclaration annotatedAnnotation = (AnnotationTypeDeclaration) annotatedNode;
				ITypeBinding tBinding = annotatedAnnotation.resolveBinding();
				return addAnnotationRelation(annoteElem, tBinding);
			}

			case ASTNode.VARIABLE_DECLARATION_FRAGMENT: {
				VariableDeclarationFragment varDeclFrag = (VariableDeclarationFragment) annotatedNode;
				IVariableBinding vBinding = varDeclFrag.resolveBinding();
				return addAnnotationRelation(annoteElem, vBinding);
			}

			case ASTNode.PACKAGE_DECLARATION: {
				PackageDeclaration packDecl = (PackageDeclaration) annotatedNode;
				IPackageBinding pBinding = packDecl.resolveBinding();
				return addAnnotationRelation(annoteElem, pBinding);
			}

			case ASTNode.SINGLE_VARIABLE_DECLARATION: {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) annotatedNode;
				IVariableBinding vBinding = svd.resolveBinding();
				return addAnnotationRelation(annoteElem, vBinding);
			}
			case ASTNode.TYPE_DECLARATION: {
				TypeDeclaration tDecl = (TypeDeclaration) annotatedNode;
				ITypeBinding tBinding = tDecl.resolveBinding();
				return addAnnotationRelation(annoteElem, tBinding);
			}
			case ASTNode.ENUM_DECLARATION: {
				EnumDeclaration eDecl = (EnumDeclaration) annotatedNode;
				ITypeBinding tBinding = eDecl.resolveBinding();
				return addAnnotationRelation(annoteElem, tBinding);
			}
			case ASTNode.FIELD_DECLARATION: {
				FieldDeclaration fieldDecl = (FieldDeclaration) annotatedNode;
				for (Object obj : fieldDecl.fragments()) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) obj;
					IVariableBinding vBinding = vdf.resolveBinding();
					return addAnnotationRelation(annoteElem, vBinding);
				}
			}
			default: {
				throw new IllegalStateException("Illegal annotated node type: "
						+ annotatedNode);
			}
		}
	}

	/**
	 * @param annoteElem
	 * @param binding
	 * @return
	 */
	private boolean addAnnotationRelation(IElement annoteElem,
			IPackageBinding binding) {
		if (checkForNull(binding))
			return false;
		IElement annotatedElement = convertBinding(binding);
		addAnnotationRelation(annoteElem, binding, annotatedElement);
		return true;
	}

	/**
	 * @param annoteElem
	 * @param binding
	 * @return
	 */
	private boolean addAnnotationRelation(IElement annoteElem,
			IVariableBinding binding) {
		if (checkForNull(binding))
			return false;
		IElement annotatedElement = convertBinding(binding);
		addAnnotationRelation(annoteElem, binding, annotatedElement);
		return true;
	}

	/**
	 * @param annoteElem
	 * @param annotatedAnnotation
	 */
	private boolean addAnnotationRelation(IElement annoteElem,
			ITypeBinding tBinding) {
		if (checkForNull(tBinding))
			return false;
		IElement annotatedElement = convertBinding(tBinding);
		addAnnotationRelation(annoteElem, tBinding, annotatedElement);
		return true;
	}

	/**
	 * @param annoteElem
	 * @param tBinding
	 * @param annotatedElement
	 */
	private void addAnnotationRelation(IElement annoteElem, IBinding binding,
			IElement annotatedElement) {
		aDB.addElement(annotatedElement, binding.getModifiers());
		aDB.addRelation(annoteElem, Relation.ANNOTATES, annotatedElement);
	}

	/**
	 * @param annoteElem
	 * @param annotatedAnnotation
	 */
	private boolean addAnnotationRelation(IElement annoteElem,
			IMethodBinding mBinding) {
		if (checkForNull(mBinding))
			return false;
		IElement annotatedElement = convertBinding(mBinding);
		this.addAnnotationRelation(annoteElem, mBinding, annotatedElement);
		return true;
	}

	public boolean visit(QualifiedName pNode) {
		IBinding lBinding = pNode.resolveBinding();

		if (lBinding == null) {
			log("Null binding 3 for " + pNode);
			return false;
		}

		if (lBinding.getKind() == IBinding.VARIABLE) {
			if (((IVariableBinding) lBinding).isField()) {
				addAccessRelation((IVariableBinding) lBinding);
				
				Assignment assignment = getAssignment(pNode);
				if (assignment != null) {
					addSetsRelation((IVariableBinding) lBinding);

					if (!(assignment.getOperator() == Assignment.Operator.ASSIGN))
						addGetsRelation((IVariableBinding) lBinding);
				}
				else
					addGetsRelation((IVariableBinding) lBinding);
			}
		}
		return false;
	}

	public boolean visit(SimpleName pNode) {
		IBinding lBinding = pNode.resolveBinding();

		if (lBinding == null) {
			// Occurs for all labels (e.g., loop labels)
			//log( "Null binding 4 for " + pNode );
			return false;
		}
		if (lBinding.getKind() == IBinding.VARIABLE) {
			if (((IVariableBinding) lBinding).isField()) {
				addAccessRelation((IVariableBinding) lBinding);

				Assignment assignment = getAssignment(pNode);
				if (assignment != null) {
					addSetsRelation((IVariableBinding) lBinding);

					if (!(assignment.getOperator() == Assignment.Operator.ASSIGN))
						addGetsRelation((IVariableBinding) lBinding);
				}
				else
					addGetsRelation((IVariableBinding) lBinding);
			}
		}

		return false;
	}

	public boolean visit(SuperConstructorInvocation pNode) {
		Object obj = pNode.resolveConstructorBinding();
		if (obj == null) {
			System.out.println("uh oh");
			return true;
		}
		addCallRelation(pNode, pNode.resolveConstructorBinding(), true);
		return true;
	}

	public boolean visit(SuperFieldAccess pNode) {
		IVariableBinding lBinding = (IVariableBinding) pNode.getName()
				.resolveBinding();

		if (lBinding == null) {
			log("Null binding 2 for" + pNode);
			return false;
		}
		addAccessRelation(lBinding);
		
		Assignment assignment = getAssignment(pNode);
		if (assignment != null) {
			addSetsRelation((IVariableBinding) lBinding);

			if (!(assignment.getOperator() == Assignment.Operator.ASSIGN))
				addGetsRelation((IVariableBinding) lBinding);
		}
		else
			addGetsRelation((IVariableBinding) lBinding);
		return true;
	}
	
	private static Assignment getAssignment(ASTNode node) {
		if ( node == null )
			return null;
		
		if ( node.getNodeType() == ASTNode.ASSIGNMENT )
			return (Assignment)node;
		
		else
			return getAssignment(node.getParent());
	}

	public boolean visit(SuperMethodInvocation pNode) {
		addCallRelation(pNode, pNode.resolveMethodBinding(), true);
		return true;
	}

	public boolean visit(PackageDeclaration pNode) {
		IPackageBinding binding = pNode.resolveBinding();
		if (checkForNull(binding))
			return false;
		IElement packageElem = convertBinding(binding);

		aDB.addElement(packageElem, binding.getModifiers());

		CompilationUnit parent = (CompilationUnit) pNode.getParent();
		List containedTypes = parent.types();
		for (Iterator it = containedTypes.iterator(); it.hasNext();) {
			AbstractTypeDeclaration type = (AbstractTypeDeclaration) it.next();
			ITypeBinding typeBinding = type.resolveBinding();
			IElement typeElem = convertBinding(typeBinding);
			this.aDB.addElement(typeElem, typeBinding.getModifiers());
			this.aDB.addRelation(packageElem, Relation.CONTAINS, typeElem);
		}

		return true;
	}

	/**
	 * @param binding
	 * @return
	 */
	private IElement convertBinding(IPackageBinding binding) {
		checkForNull(binding);
		return FlyweightElementFactory.getElement(ICategories.PACKAGE, binding
				.getName(), binding.getJavaElement());
	}

	/**
	 * Generated the DECLARES relations between a type and and nested types, the
	 * EXTENDS relations, and the IMPLEMENTS relation
	 */
	public boolean visit(TypeDeclaration pNode) {
		ITypeBinding lBinding = pNode.resolveBinding();

		if (checkForNull(lBinding))
			return false;

		try {
			saveTypeRelation(lBinding);

			//Add Declaration relations if this is a local or nested class
			if (lBinding.isLocal() || lBinding.isAnonymous()) {
				aDB.addRelation((IElement) aCurrMethod, Relation.DECLARES_TYPE,
						aCurrType);
			}
			else if (lBinding.isNested()) {
				aDB.addRelation(aCurrTypeReminder.peek(),
						Relation.DECLARES_TYPE, aCurrType);
			}

			//Find superclass
			if (!pNode.isInterface()) {
				ITypeBinding lSuperBinding = lBinding.getSuperclass();
				if (lSuperBinding != null) {
					IElement lSuperClass = convertBinding(lSuperBinding);
					aDB.addElement(lSuperClass, lSuperBinding.getModifiers());
					aDB.addRelationAndTranspose(aCurrType,
							Relation.EXTENDS_CLASS, lSuperClass);
				}
			}

			// Find interfaces.
			ITypeBinding lInterfaceBindings[] = lBinding.getInterfaces();
			for (int i = 0; i < lInterfaceBindings.length; i++) {
				IElement lInterface = convertBinding(lInterfaceBindings[i]);
				aDB.addElement(lInterface, lInterfaceBindings[i].getModifiers()
						| ABSTRACT_FLAG);
				if (pNode.isInterface()) {
					aDB.addRelationAndTranspose(aCurrType,
							Relation.EXTENDS_INTERFACES, lInterface);
				}
				else {
					aDB.addRelationAndTranspose(aCurrType,
							Relation.IMPLEMENTS_INTERFACE, lInterface);
				}
			}
		}
		catch (Exception pException) {
			ProblemManager.reportException(pException);
		}

		return true;
	}

}
