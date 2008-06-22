/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.model;

import ca.mcgill.cs.swevo.jayfx.model.IElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionArc;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.Pattern;

/**
 * @author raffi
 * 
 */
public class Suggestion<E extends IntentionElement<IElement>> {
	private E suggestion;
	private Pattern<IntentionArc<IElement>> pattern;
	private double confidence;

	/**
	 * @param suggestion
	 * @param pattern
	 * @param confidence
	 */
	public Suggestion(E suggestion,
			Pattern<IntentionArc<IElement>> pattern, double confidence) {
		this.suggestion = suggestion;
		this.pattern = pattern;
		this.confidence = confidence;
	}

	/**
	 * @return the suggestion
	 */
	public E getSuggestion() {
		return this.suggestion;
	}

	/**
	 * @return the pattern
	 */
	public Pattern<IntentionArc<IElement>> getPattern() {
		return this.pattern;
	}

	/**
	 * @return the confidence
	 */
	public double getConfidence() {
		return this.confidence;
	}
}
