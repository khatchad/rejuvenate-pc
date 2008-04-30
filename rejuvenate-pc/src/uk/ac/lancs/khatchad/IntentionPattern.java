/**
 * 
 */
package uk.ac.lancs.khatchad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;


/**
 * @author raffi
 * 
 */
public class IntentionPattern extends IntentionSequence {

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString());
		
//		if ( ! (obj instanceof IntentionPattern ) )
//			return false;
//		IntentionSequence rhs = (IntentionSequence)obj;
//		
//		if (this.sequenceElements.size() != rhs.sequenceElements.size())
//			return false;
//		
//		for ( int i = 0; i < this.sequenceElements.size(); i++ ) {
//			IntentionElement<? extends IElement> lhsElem = this.sequenceElements.elementAt(i);
//			IntentionElement<? extends IElement> rhsElem = rhs.sequenceElements.elementAt(i);
//			
//			if (!lhsElem.equals(rhsElem))
//				return false;
			
//			if ( lhsElem instanceof IntentionEdge && rhsElem instanceof IntentionEdge) {
//				IntentionEdge lhsEdge = (IntentionEdge)lhsElem;
//				IntentionEdge rhsEdge = (IntentionEdge)rhsElem;
//				
//				if ( !lhsEdge.getType().equals(rhsEdge.getType()))
//					return false;
//			}
//			else if ( lhsElem instanceof Wildcard && rhsElem instanceof Wildcard ) {
//				Wildcard lhsWild = (Wildcard)lhsElem;
//				Wildcard rhsWild = (Wildcard)rhsElem;
//				
//				if ( !(lhsWild.isEnabled() && rhsWild.isEnabled()) || !(!lhsWild.isEnabled() && !rhsWild.isEnabled()))
//					return false;
//			}
//			else {
//				if ( !lhsElem.equals(rhsElem))
//					return false;
//			}
//		}
//		return true;
	}
	
	@SuppressWarnings("unchecked")
	public IntentionPattern(IntentionPath path) {
		for (int i = 0; i < path.size(); i++) {
			if (path.elementAt(i).isEnabled()
					&& path.elementAt(i) instanceof IntentionNode)
				this
						.add(new ExecutionWildcard<IElement>());
			else if (path.elementAt(i).isEnabled()
					&& path.elementAt(i) instanceof IntentionEdge) {
				this.pop();
				this.add(new NodeWildcard<IElement>());
				this.add(new CallWildcard<IElement>());
			} else {
				if ( path.elementAt(i) instanceof IntentionEdge )
					this.add(new EdgeWildcard<IElement>((IntentionEdge)path.elementAt(i)));
				else if (!(path.elementAt(i) instanceof IntentionNode)
						|| (i == 0 || i == path.size() - 1)) // only
																				// outside.
					this.add(path
							.elementAt(i));
				else
					this.add(new NodeWildcard<IElement>());
			}
		}
	}
	
	private IntentionPattern() {}
	
	public final Set<IntentionPattern> getMoreAbstractPatterns() {
		Set<IntentionPattern> ret = new LinkedHashSet<IntentionPattern>();
		
		if ( this.getNumberOfNodes() >= 2) {
			if ( !(this.firstElement() instanceof Wildcard) ) {
				IntentionPattern newPattern = (IntentionPattern)this.clone();
				newPattern.setToNodeWildcard(0);
				ret.add(newPattern);
			}
			
			if ( !(this.lastElement() instanceof Wildcard) ) {
				IntentionPattern newPattern = (IntentionPattern)this.clone();
				newPattern.setToNodeWildcard(newPattern.size()-1);
				ret.add(newPattern);
			}
			
			if ( !(this.firstElement() instanceof Wildcard) && !(this.lastElement() instanceof Wildcard)) {
				IntentionPattern newPattern = (IntentionPattern)this.clone();
				newPattern.setToNodeWildcard(0);
				newPattern.setToNodeWildcard(newPattern.size()-1);
				ret.add(newPattern);
			}
		}
		
		return ret;
	}
	
	/**
	 * @param ret
	 * @return
	 */
//	private static Set<IntentionPattern> pruneRedundantPatterns(
//			Set<IntentionPattern> patternSet) {
//		Set<IntentionPattern> ret = new LinkedHashSet<IntentionPattern>();
//
//		for (IntentionPattern pattern : patternSet) {
//			if (pattern.getNumberOfNodes() >= 2)
//				for (int i = 0; i < pattern.size(); i += 3) {
//					if (pattern.elementAt(i) instanceof Wildcard
//							&& pattern.elementAt(i + 2) instanceof Wildcard)
//						if (pattern.elementAt(i + 1) instanceof IntentionEdge) {
//							if (!((IntentionEdge) pattern.elementAt(i + 1))
//									.getType().equals(Relation.CALLS)) {
//								pattern.remove(i);
//								pattern.remove(i + 1);
//								pattern.remove(i + 2);
//							}
//						}
//				}
//			ret.add(pattern);
//		}
//		return ret;
//	}

	private void setToNodeWildcard(int pos) {
		this.set(pos, new NodeWildcard<IElement>());
	}

	public boolean matches(IntentionPath path) {
		int lhsLength = this.size();
		int rhsLength = path.size();

		int inx = 0;
		while (inx < lhsLength && inx < rhsLength) {
			IntentionElement lhs = this.elementAt(inx);
			IntentionElement rhs = path.elementAt(inx);

//			if (lhs instanceof IntentionEdge && rhs instanceof IntentionEdge) {
//				IntentionEdge lhsEdge = (IntentionEdge) lhs;
//				IntentionEdge rhsEdge = (IntentionEdge) rhs;
//
//				if (lhsEdge.getType().equals(rhsEdge.getType()))
//					inx++;
//				else
//					return false;
//			}

//			else 
			if (lhs.equals(rhs))
				inx++;
			else
				return false;
		}

		if (inx == lhsLength && inx == rhsLength)
			return true;
		else
			return false;
	}

	public Set<IntentionPath> matchingPaths(Set<IntentionPath> pathSet) {
		Set<IntentionPath> ret = new LinkedHashSet<IntentionPath>();
		for (IntentionPath path : pathSet) {
			if (this.matches(path))
				ret.add(path);
		}
		return ret;
	}

	public Set<IntentionElement> apply(Set<IntentionPath> pathSet) {
		Set<IntentionPath> matchingPathSet = this.matchingPaths(pathSet);
		List<Integer> wildcardPosSet = this.getEnabledWildcardPositions();
		Set<IntentionElement> matchingElementSet = new LinkedHashSet<IntentionElement>();

		for (IntentionPath matchingPath : matchingPathSet)
			for (int i = 0; i < matchingPath.size(); i++)
				if (wildcardPosSet.contains(i))
					matchingElementSet.add(matchingPath
							.elementAt(i));
		return matchingElementSet;
	}

	public double getPrecision(Set<IntentionPath> pathSet) {
		Set<IntentionElement> applicationResults = this.apply(pathSet);
		if (applicationResults.isEmpty())
			return 1;
		else {
			Set<IntentionElement> enabledElements = new LinkedHashSet<IntentionElement>();
			for (IntentionPath path : pathSet) {
				enabledElements.addAll(path.getEnabledElements());
			}

			Set<IntentionElement> intersection = new LinkedHashSet<IntentionElement>(
					enabledElements);
			intersection.retainAll(applicationResults);

			double ret = ((double) intersection.size())
					/ applicationResults.size();
			return ret;
		}
	}

	public double getConcreteness() {
		int numberOfWildcards = this.getWildcardPositions().size();
		int numberOfElements = this.getNumberOfNodes();

		return ((double) (numberOfElements - numberOfWildcards))
				/ numberOfElements;
	}

	private Set<Integer> getWildcardPositions() {
		Set<Integer> ret = new LinkedHashSet<Integer>();
		for (int i = 0; i < this.size(); i++)
			if (this.elementAt(i) instanceof NodeWildcard)
				ret.add(i);
		return ret;
	}

	private List<Integer> getEnabledWildcardPositions() {
		List<Integer> ret = new ArrayList<Integer>();
		for (int i = 0; i < this.size(); i++)
			if (this.elementAt(i).isEnabled())
				ret.add(i);
		return ret;
	}

	/**
	 * @return
	 */
//	public Collection<? extends IntentionPattern> getAllPermutations() {
//		Collection<? extends IntentionPattern> ret = new ArrayList<IntentionPattern>();
//		int numberOfWildcardsToInsert = this.getLength();
//		int currentWildcardInsertionNumber = 1;
//		while (currentWildcardInsertionNumber <= numberOfWildcardsToInsert) {
//			IntentionPattern newPattern = null;
//				newPattern = (IntentionPattern) this.clone();
//			}
//			// for ( V)
//		}
//		return ret;
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}


}