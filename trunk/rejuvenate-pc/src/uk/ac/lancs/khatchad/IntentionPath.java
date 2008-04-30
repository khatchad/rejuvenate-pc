/**
 * 
 */
package uk.ac.lancs.khatchad;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import ca.mcgill.cs.swevo.jayfx.model.IElement;

/**
 * @author raffi
 *
 */
public class IntentionPath extends IntentionSequence {
		
	public IntentionPath() {}
	
	public IntentionPath(IntentionEdge<IElement> edge) {
		this.add(edge);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1200945877336369791L;

	public boolean isEnabled() {
		for ( IntentionElement<? extends IElement> elem : this )
			if ( elem.isEnabled() )
				return true;
		return false;
	}
}
