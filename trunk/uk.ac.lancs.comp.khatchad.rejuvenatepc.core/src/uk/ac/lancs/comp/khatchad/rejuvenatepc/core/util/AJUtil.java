/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.IAJCodeElement;
import org.eclipse.ajdt.core.model.AJModel; //import org.eclipse.ajdt.core.model.AJProjectModelFacade;
//import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author raffi
 * 
 */
public class AJUtil {
	private AJUtil() {
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
	 * @param advElem
	 * @return
	 * @throws JavaModelException
	 */
	public static Set<IJavaElement> getAdvisedJavaElements(AdviceElement advElem)
			throws JavaModelException {
		Set<IJavaElement> ret = new LinkedHashSet<IJavaElement>();
		AJProjectModelFacade model = AJProjectModelFactory.getInstance()
				.getModelForJavaElement(advElem);
		List relationshipsForElement = model.getRelationshipsForElement(
				advElem, AJRelationshipManager.ADVISES);
		for (Iterator it = relationshipsForElement.iterator(); it.hasNext();) {
			IJavaElement target = (IJavaElement) it.next();
			switch (target.getElementType()) {
				case IJavaElement.METHOD: {
					final IMethod meth = (IMethod) target;
					if (meth.getParent() instanceof AspectElement)
						break; //TODO: don't consider advice right now.
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
		return ret;
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
}
