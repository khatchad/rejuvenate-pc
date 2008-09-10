/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

/**
 * @author raffi
 * 
 */
public aspect DatasbaseControlling {
	void around() : execution(void uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.DatabaseUtil.*(..)) {
	}
}
