/**
 * 
 */
package uk.ac.lancs.khatchad;

import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import ca.mcgill.cs.swevo.jayfx.model.IElement;


/**
 * @author raffi
 *
 */
public abstract class IntentionSequence extends Stack<IntentionElement<? extends IElement>> {

//	protected Stack<IntentionElement<? extends IElement>> sequenceElements = new Stack<IntentionElement<? extends IElement>>();
	
	public Set<IntentionElement<? extends IElement>> getEnabledElements() {
		Set<IntentionElement<? extends IElement>> ret = new LinkedHashSet<IntentionElement<? extends IElement>>();
		for (IntentionElement<? extends IElement> elem : this)
			if ( elem.isEnabled() )
				ret.add(elem);
		return ret;
	}
	
	/**
	 * @return the pathElements
	 */
//	public Stack<IntentionElement<? extends IElement>> getSequenceElements() {
//		return this.sequenceElements;
//	}
	
	public int getNumberOfNodes() {
		int ret = 0;
		for ( IntentionElement<? extends IElement> elem : this )
			if ( elem instanceof IntentionNode || elem instanceof NodeWildcard)
				ret++;
		return ret;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append('(');
		
		for (ListIterator<IntentionElement<? extends IElement>> li = this.listIterator(this.size()); li.hasPrevious(); ) {
			IntentionElement<? extends IElement> elem = li.previous();
			ret.append(elem);
			ret.append(',');
		}
		
		ret.deleteCharAt(ret.length()-1);
		ret.append(')');
		return ret.toString();
	}
}