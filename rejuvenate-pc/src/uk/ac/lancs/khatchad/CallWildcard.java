/**
 * 
 */
package uk.ac.lancs.khatchad;

import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 *
 */
public final class CallWildcard<E extends IElement> extends Wildcard<E>{

	/* (non-Javadoc)
	 * @see uk.ac.lancs.khatchad.Wildcard#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if ( obj instanceof IntentionEdge) {
			IntentionEdge rhs = (IntentionEdge)obj;
			if (rhs.getType().equals(Relation.CALLS))
				return true;
			else 
				return false;
		}
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.khatchad.Wildcard#asString()
	 */
	@Override
	public String toString() {
		return "*calls*";
	}
	
	public CallWildcard() {
		this.enable();
	}
}