/**
 * 
 */
package uk.ac.lancs.khatchad;

import java.util.Stack;
import ca.mcgill.cs.swevo.jayfx.model.*;

/**
 * @author raffi
 * 
 */
public aspect IntentionPathCyclePrevention {

	pointcut addEdgeToPath(IntentionPath stack, IntentionEdge edge) : 
		(call(* IntentionPath.push(..)) || call(* IntentionPath.add*(..))) 
			&& target(stack) && args(edge);

	pointcut addNodeToPath(IntentionPath stack, IntentionNode elem) : 
		(call(* IntentionPath.push(..)) || call(* IntentionPath.add*(..))) 
			&& target(stack) && args(elem);

	Object around(IntentionPath stack, IntentionEdge edge) : addEdgeToPath(stack, edge) {
		IntentionNode target = edge.getFromNode();
		if (stack.contains(target)) // found a cycle.
			throw new CycleDetectedException("Cycle found.");
		else
			return proceed(stack, edge);
	}

	Object around(IntentionPath stack, IntentionNode elem) : addNodeToPath(stack, elem) {
		if (stack.contains(elem)) // found a cycle.
			throw new CycleDetectedException("Cycle found.");
		else
			return proceed(stack, elem);
	}
}