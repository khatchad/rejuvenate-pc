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
	 * @param string
	 */
	public CycleDetectedException(String string) {
		super(string);
	}

}
