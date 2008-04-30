/**
 * 
 */
package uk.ac.lancs.comp.khatchad.core;

import java.util.ArrayList;
import java.util.Vector;

import uk.ac.lancs.khatchad.IntentionEdge;
import uk.ac.lancs.khatchad.IntentionNode;

/**
 * @author raffi
 *
 */
public class PathElements<E extends IntentionNode<?>> extends Vector<E> {
	
	private Path<?> path;	
	
	public PathElements(Path<?> path) {
		super();
		this.path = path;
	}
	
	public PathElements() {}

	/**
	 * @return the path
	 */
	public Path<?> getPath() {
		return this.path;
	}
}
