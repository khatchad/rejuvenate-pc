/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

/**
 * @author raffi
 *
 */
public class JDTUtil {
	
	public static IType getType(IJavaElement elem) {
		if ( elem == null )
			return null;
		else if (elem.getElementType() == IJavaElement.TYPE)
			return (IType)elem;
		else
			return getType(elem.getParent());
	}

}
