/**
 * 
 */
package uk.ac.lancs.comp.khatchad.ajayfx;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;
import ca.mcgill.cs.swevo.jayfx.FastConverter;
import ca.mcgill.cs.swevo.jayfx.ProgramDatabase;
import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.Category;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class AjASTCrawler extends AsmHierarchyBuilder {

	/**
	 * Encapsulates behavior to report null objects.
	 * 
	 * @param pObject
	 *            Object to check.
	 */
	private static boolean checkForNull(final Object pObject) {
		boolean lReturn = false;
		if (pObject == null)
			//			Thread.dumpStack();
			lReturn = true;
		return lReturn;
	}

	/**
	 * Converts a type binding to a class element or an enum element.
	 * 
	 * @param pBinding
	 *            The binding to convert. Cannot be null.
	 * @param name
	 * @return A class or enum element representing this binding. Cannot be
	 *         null.
	 */
	private static IElement convertBinding(final TypeBinding pBinding) {
		AjASTCrawler.checkForNull(pBinding);
		return FlyweightElementFactory.getElement(Category.ASPECT, pBinding
				.debugName());
	}

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

	private final ProgramDatabase adb;

	/**
	 * @param adb
	 * @param converter
	 */
	public AjASTCrawler(final ProgramDatabase adb, final FastConverter converter) {
		this.adb = adb;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder#visit(org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration, org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	@Override
	public boolean visit(final MethodDeclaration methodDeclaration,
			final ClassScope scope) {
		super.visit(methodDeclaration, scope);
		//		if ( methodDeclaration instanceof PointcutDeclaration ) {
		//			PointcutDeclaration pd = (PointcutDeclaration)methodDeclaration;
		//		}

		if (methodDeclaration instanceof AdviceDeclaration) {
			final AdviceDeclaration ad = (AdviceDeclaration) methodDeclaration;
			//			Map<AdviceElement, Set<IElement>> map = this.getAdvisingMap();
			//			for ( AdviceElement adviceElem : map.keySet()) {
			//				String str = adviceElem.getKey();
			//				if ( String.valueOf(ad.binding.readableName()).contains(adviceElem.getKey())) {
			//					;
			//				}

			final ReferenceBinding dBinding = ad.binding.declaringClass;
			final IElement declaringType = AjASTCrawler
					.convertBinding(dBinding);
			this.adb.addElement(declaringType, dBinding.modifiers);

			final MethodBinding aBinding = ad.binding;
			final IElement advice = this.convertBinding(aBinding);
			this.adb.addElement(advice, aBinding.modifiers);

			this.adb.addRelation(declaringType, Relation.DECLARES_METHOD,
					advice);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder#visit(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	@Override
	public boolean visit(final TypeDeclaration memberTypeDeclaration,
			final BlockScope scope) {
		// TODO Auto-generated method stub
		return super.visit(memberTypeDeclaration, scope);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder#visit(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	@Override
	public boolean visit(final TypeDeclaration memberTypeDeclaration,
			final ClassScope scope) {
		// TODO Auto-generated method stub
		return super.visit(memberTypeDeclaration, scope);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder#visit(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration, org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope)
	 */
	@Override
	public boolean visit(final TypeDeclaration typeDeclaration,
			final CompilationUnitScope scope) {
		super.visit(typeDeclaration, scope);

		if (typeDeclaration instanceof AspectDeclaration) {
			final SourceTypeBinding binding = typeDeclaration.binding;
			final IElement elem = AjASTCrawler.convertBinding(binding);
			this.adb.addElement(elem, binding.modifiers);
			final PackageBinding pBinding = binding.fPackage;
			final IElement pElem = this.convertBinding(pBinding);
			this.adb.addElement(pElem, 0);
			this.adb.addRelation(pElem, Relation.CONTAINS, elem);

			final ReferenceBinding sBinding = binding.superclass();
			final IElement superElem = AjASTCrawler.convertBinding(sBinding);
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
	private IElement convertBinding(final MethodBinding binding) {
		return Util.convertBinding(Category.ADVICE, String.valueOf(binding
				.readableName()));
	}

	/**
	 * @param binding
	 * @return
	 */
	private IElement convertBinding(final PackageBinding binding) {
		return FlyweightElementFactory.getElement(Category.PACKAGE, String
				.valueOf(binding.readableName()));
	}
}