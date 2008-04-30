/**
 * 
 */
package uk.ac.lancs.khatchad;

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
