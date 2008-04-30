/**
 * 
 */
package uk.ac.lancs.khatchad;

import ca.mcgill.cs.swevo.jayfx.model.IElement;

/**
 * @author raffi
 *
 */
public abstract class Wildcard<E extends IElement> extends IntentionElement<E> {
	
	/* (non-Javadoc)
	 * @see uk.ac.lancs.khatchad.IntentionElement#asString()
	 */
	@Override
	public String toString() {
		return this.isEnabled() ? "*?*" : "?";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntentionElement)
			return true;
		else
			return false;
	}
}