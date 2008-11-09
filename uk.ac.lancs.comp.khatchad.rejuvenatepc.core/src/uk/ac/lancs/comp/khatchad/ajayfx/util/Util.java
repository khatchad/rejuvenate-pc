/**
 * 
 */
package uk.ac.lancs.comp.khatchad.ajayfx.util;

import ca.mcgill.cs.swevo.jayfx.model.Category;
import ca.mcgill.cs.swevo.jayfx.model.FlyweightElementFactory;
import ca.mcgill.cs.swevo.jayfx.model.IElement;

/**
 * @author raffi
 *
 */
public class Util {
	private Util() {}

	public static IElement convertBinding(final Category category,
			final String readableName) {
		return FlyweightElementFactory.getElement(category, readableName);
	}
}
