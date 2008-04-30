/**
 * 
 */
package uk.ac.lancs.khatchad;

import ca.mcgill.cs.swevo.jayfx.model.IElement;

/**
 * @author raffi
 *
 */
public final class ExecutionWildcard<E extends IElement> extends NodeWildcard<E> {

	/* (non-Javadoc)
	 * @see uk.ac.lancs.khatchad.Wildcard#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ExecutionWildcard) 
			return this.hashCode() == obj.hashCode();
		else
			return super.equals(obj);
	}
	
	public ExecutionWildcard() {
		this.enable();
	}
}
