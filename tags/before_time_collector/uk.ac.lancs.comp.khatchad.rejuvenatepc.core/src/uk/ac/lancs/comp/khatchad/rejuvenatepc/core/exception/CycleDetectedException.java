/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.exception;

/**
 * @author raffi
 * 
 */
public class CycleDetectedException extends IllegalStateException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2568634790467529616L;

	/**
	 * @param string
	 */
	public CycleDetectedException(final String string) {
		super(string);
	}

}
