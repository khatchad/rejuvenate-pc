/**
 * 
 */
package uk.ac.lancs.khatchad;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDesignator;
import org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder;
import org.aspectj.asm.AsmManager;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.patterns.FastMatchInfo;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.PointcutUtilities;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.ui.AspectJUIPlugin;


import ca.mcgill.cs.swevo.jayfx.FastConverter;
import ca.mcgill.cs.swevo.jayfx.ProgramDatabase;
import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 *
 */
public class AjASTCrawler extends AsmHierarchyBuilder {
	
	private ProgramDatabase adb;
	private FastConverter converter;
	
//	private static Map<AdviceElement, Set<IElement>> advisingMap = new HashMap<AdviceElement, Set<IElement>>();
//
//	/**
//	 * @param advisingMap the advisingMap to set
//	 */
//	public static void setAdvisingMap(Map<AdviceElement, Set<IElement>> advisingMap) {
//		AjASTCrawler.advisingMap = advisingMap;
//	}
//
//	/**
//	 * @return the advisingMap
//	 */
//	public static Map<AdviceElement, Set<IElement>> getAdvisingMap() {
//		return advisingMap;
//	}

	/**
	 * @param adb
	 * @param converter
	 */
	public AjASTCrawler(ProgramDatabase adb, FastConverter converter) {
		this.adb = adb;
		this.converter = converter;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder#visit(org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration, org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	@Override
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		super.visit(methodDeclaration, scope);
//		if ( methodDeclaration instanceof PointcutDeclaration ) {
//			PointcutDeclaration pd = (PointcutDeclaration)methodDeclaration;
//		}
		
		if (methodDeclaration instanceof AdviceDeclaration) {
			AdviceDeclaration ad = (AdviceDeclaration)methodDeclaration;
//			Map<AdviceElement, Set<IElement>> map = this.getAdvisingMap();
//			for ( AdviceElement adviceElem : map.keySet()) {
//				String str = adviceElem.getKey();
//				if ( String.valueOf(ad.binding.readableName()).contains(adviceElem.getKey())) {
//					;
//				}
			
			ReferenceBinding dBinding = ad.binding.declaringClass;
			IElement declaringType = convertBinding(dBinding);
			this.adb.addElement(declaringType, dBinding.modifiers);
			
			MethodBinding aBinding = ad.binding;
			IElement advice = convertBinding(aBinding);
			this.adb.addElement(advice, aBinding.modifiers);
			
			this.adb.addRelation(declaringType, Relation.DECLARES , advice);
		}
		return false;
	}

	/**
	 * @param binding
	 * @return
	 */
	private IElement convertBinding(MethodBinding binding) {
		return Util.convertBinding(ICategories.ADVICE, String.valueOf(binding.readableName()));
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder#visit(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	@Override
	public boolean visit(TypeDeclaration memberTypeDeclaration, BlockScope scope) {
		// TODO Auto-generated method stub
		return super.visit(memberTypeDeclaration, scope);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder#visit(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	@Override
	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		// TODO Auto-generated method stub
		return super.visit(memberTypeDeclaration, scope);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder#visit(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope)
	 */
	@Override
	public boolean visit(TypeDeclaration typeDeclaration,
			CompilationUnitScope scope) {
		super.visit(typeDeclaration, scope);
		
		if ( typeDeclaration instanceof AspectDeclaration) {
			SourceTypeBinding binding = typeDeclaration.binding;
			IElement elem = convertBinding(binding);
			this.adb.addElement(elem, binding.modifiers);
			PackageBinding pBinding = binding.fPackage;
			IElement pElem = convertBinding(pBinding);
			this.adb.addElement(pElem, 0);
			this.adb.addRelation(pElem, Relation.CONTAINS, elem);
			
			ReferenceBinding sBinding = binding.superclass();
			IElement superElem = convertBinding(sBinding);
			this.adb.addElement(superElem, sBinding.modifiers);
			this.adb.addRelation(elem, Relation.EXTENDS_CLASS, superElem);
			
//			for (FieldBinding fBinding : binding.fields ) {
//				IElement fElem = convertBinding(fBinding);
//			}
		}
		
		return true;
	}
	
	/**
	 * @param binding
	 * @return
	 */
	private IElement convertBinding(PackageBinding binding) {
		return FlyweightElementFactory.getElement( ICategories.PACKAGE, 
				String.valueOf(binding.readableName()), null);
	}

	/**
	 * Converts a type binding to a class element or an enum element.
	 * @param pBinding The binding to convert.  Cannot be null.
	 * @param name 
	 * @return A class or enum element representing this binding.  Cannot be null.
	 */
	private static IElement convertBinding(TypeBinding pBinding)
	{
		checkForNull( pBinding );
		return FlyweightElementFactory.getElement( ICategories.ASPECT, 
				pBinding.debugName(), null);
	}
	
	/**
	 * Encapsulates behavior to report null objects.
	 * @param pObject Object to check.
	 */
	private static boolean checkForNull( Object pObject )
	{
		boolean lReturn = false;
		if( pObject == null )
		{
//			Thread.dumpStack();
			lReturn = true;
		}
		return lReturn;
	}
}