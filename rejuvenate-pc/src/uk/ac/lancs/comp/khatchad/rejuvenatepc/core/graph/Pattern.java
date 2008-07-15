/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import static java.lang.Math.max;

import java.util.Collection;
import java.util.Set;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.jdom.DataConversionException;
import org.jdom.Element;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;

import ca.mcgill.cs.swevo.jayfx.model.IElement;

/**
 * @author raffi
 * 
 */
public class Pattern<E extends IntentionArc<IElement>> extends Path<E> {

	private static final long serialVersionUID = -8126850132892419370L;

	/**
	 * @param patternElem
	 * @throws DataConversionException 
	 */
	public Pattern(Element patternElem) throws DataConversionException {
		super(patternElem);
	}
	
	public Pattern() {
	}

	public static double calculateConfidence(final double precision, final double coverage,
			final double concreteness) {
		return (precision * concreteness) + (coverage * (1 - concreteness));
	}
	
	public static double calculatePrecision(
			final Set<IntentionElement<IElement>> searchedFor,
			final Set<IntentionElement<IElement>> found) {
		final int totalElements = found.size();
		final int lookingFor = searchedFor.size();
		return (double) lookingFor / totalElements;
	}

	/**
	 * @param pattern
	 * @return
	 */
	public static double calculateConcreteness(
			final Path<IntentionArc<IElement>> pattern) {
		final Collection<IntentionNode<IElement>> allNodes = pattern.getNodes();
		final Collection<IntentionNode<IElement>> wildNodes = pattern
				.getWildcardNodes();
		return (double) (allNodes.size() - wildNodes.size())
				/ allNodes.size();
	}
}