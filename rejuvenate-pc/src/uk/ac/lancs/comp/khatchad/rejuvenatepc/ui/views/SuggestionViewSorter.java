/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ca.mcgill.cs.swevo.jayfx.model.IElement;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.model.Suggestion;

/**
 * @author raffi
 * 
 */
public class SuggestionViewSorter extends ViewerSorter {
	private SortBy type;

	public enum SortBy {
		SUGGESTIONS, PATTERNS, CONFIDENCE
	}

	/**
	 * @param type
	 */
	public SuggestionViewSorter(SortBy type) {
		super();
		this.type = type;
	};

	@SuppressWarnings("unchecked")
	public int compare(Viewer viewer, Object o1, Object o2) {
		Suggestion<IntentionElement<IElement>> suggestion1 = (Suggestion<IntentionElement<IElement>>) o1;
		Suggestion<IntentionElement<IElement>> suggestion2 = (Suggestion<IntentionElement<IElement>>) o2;

		switch (this.type) {
			case SUGGESTIONS:
				return compareSuggestions(suggestion1, suggestion2);
			case PATTERNS:
				return comparePatterns(suggestion1, suggestion2);
			case CONFIDENCE:
				return compareConfidence(suggestion1, suggestion2);
			default:
				return 0;
		}
	}

	/**
	 * @param suggestion1
	 * @param suggestion2
	 * @return
	 */
	private int compareSuggestions(
			Suggestion<IntentionElement<IElement>> suggestion1,
			Suggestion<IntentionElement<IElement>> suggestion2) {
		return suggestion1.getSuggestion().toPrettyString().compareTo(
				suggestion2.getSuggestion().toPrettyString());
	}

	/**
	 * @param suggestion1
	 * @param suggestion2
	 * @return
	 */
	private int comparePatterns(
			Suggestion<IntentionElement<IElement>> suggestion1,
			Suggestion<IntentionElement<IElement>> suggestion2) {
		return suggestion1.getPattern().toString().compareTo(suggestion2.getPattern().toString());
	}

	/**
	 * @param suggestion1
	 * @param suggestion2
	 * @return
	 */
	private int compareConfidence(
			Suggestion<IntentionElement<IElement>> suggestion1,
			Suggestion<IntentionElement<IElement>> suggestion2) {
		return Double.compare(suggestion1.getConfidence(), suggestion2.getConfidence());
	}
}