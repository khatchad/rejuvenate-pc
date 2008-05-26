/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;


import ca.mcgill.cs.swevo.jayfx.ASTCrawler;
import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.model.ICategories;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class IntentionGraph<E extends IntentionNode<IElement>> {
	
	private AdviceElement elementsCurrentlyEnabledAccordingToElem;

	private Map<IElement, IntentionNode<IElement>> nodeMap = new HashMap<IElement, IntentionNode<IElement>>();

	private JayFX database;

	public IntentionGraph(JayFX database, IProgressMonitor monitor) throws Exception {
		this.database = database;

		monitor.beginTask("Building Intention Graph", database.getAllElements().size());
		for (IElement elem : database.getAllElements()) {
			IntentionNode<IElement> node;
			if (nodeMap.containsKey(elem))
				node = nodeMap.get(elem);
			else {
				// make a node for this element.
//				IJavaElement jElem = this.database.convertToJavaElement(elem);
				node = new IntentionNode<IElement>(elem);
				this.nodeMap.put(elem, node);
			}

			// now make the edges.
			makeEdges(elem, node, Relation.GETS);
			makeEdges(elem, node, Relation.SETS);
			makeEdges(elem, node, Relation.CALLS);
			makeEdges(elem, node, Relation.OVERRIDES);
			makeEdges(elem, node, Relation.IMPLEMENTS_METHOD);
			makeEdges(elem, node, Relation.DECLARES_METHOD);
			makeEdges(elem, node, Relation.DECLARES_FIELD);
			makeEdges(elem, node, Relation.DECLARES_TYPE);
			makeEdges(elem, node, Relation.EXTENDS_CLASS);
			makeEdges(elem, node, Relation.EXTENDS_INTERFACES);
			makeEdges(elem, node, Relation.IMPLEMENTS_INTERFACE);
			makeEdges(elem, node, Relation.CONTAINS);
			makeEdges(elem, node, Relation.ANNOTATES);
			makeEdges(elem, node, Relation.ADVISES);
			
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * @param database
	 * @param elem
	 * @param node
	 * @throws Exception
	 */
	private void makeEdges(IElement elem,
			IntentionNode<IElement> node, Relation relation) throws Exception {
		for (IElement toElement : database.getRange(elem,
				relation)) {
			IntentionNode<IElement> toNode = getNode(toElement);
			IntentionEdge<IElement> edge = new IntentionEdge<IElement>(
					node, toNode, relation, toNode.hasEnabledEdgesForIncommingRelation(relation));
//			if (relation.equals(Relation.ADVISES))
//				toNode.enable();
			node.addEdge(edge);
		}
	}
	
	public Collection<IntentionElement<IElement>> getEnabledElements() {
		Collection<IntentionElement<IElement>> ret = new LinkedHashSet<IntentionElement<IElement>>();
		for (IntentionNode<IElement> node : this.getNodes()) {
			if ( node.isEnabled() )
				ret.add(node);
			for ( IntentionEdge<IElement> edge : node.getEdges() )
				if ( edge.isEnabled() )
					ret.add(edge);
		}
		return ret;
	}
	
	public void enableElementsAccordingTo(AdviceElement advElem, IProgressMonitor monitor) throws ConversionException, CoreException {
		this.database.enableElementsAccordingTo(advElem, new SubProgressMonitor(monitor, 1));
		this.updateStateToReflectDatabase(new SubProgressMonitor(monitor, 1));
		
	/*
		IProject proj = this.database.getSelectedAdvice().getJavaProject().getProject();
		List<AJRelationship> relationshipList = AJModel.getInstance().getAllRelationships(proj, new AJRelationshipType[] {AJRelationshipManager.ADVISES});
		for (AJRelationship relationship : relationshipList ) {
			org.eclipse.ajdt.core.javaelements.AdviceElement advice = (org.eclipse.ajdt.core.javaelements.AdviceElement)relationship.getSource();
			if ( advice.equals(this.database.getSelectedAdvice()) ) {
				IJavaElement target = relationship.getTarget();
				switch(target.getElementType()) {
				case IJavaElement.METHOD: {
					IMethod meth = (IMethod)target;
    				IElement adviceElem = Util.convertBinding(ICategories.ADVICE, advice.getHandleIdentifier());
//    				try {
//    					this.database.addElement(adviceElem, advice.getFlags());
//    				} catch (JavaModelException e) {
//    					// TODO Auto-generated catch block
//    					e.printStackTrace();
//    				}
//    				
//    				this.aDB.addRelation(adviceElem, Relation.ADVISES, aCurrMethod);
					break;
				}
				case IJavaElement.LOCAL_VARIABLE: {
					//its an aspect element.
					break;
				}
				default:
					throw new IllegalStateException("Unexpected relationship target type: " + target.getElementType());
				}
			}
		}
	*/
	}

	/**
	 * Updates the graph to reflect changes in the underlying DB.
	 * @param monitor 
	 */
	private void updateStateToReflectDatabase(IProgressMonitor monitor) {
		monitor.beginTask("Updating nodes.", this.nodeMap.values().size());
		for ( IntentionNode<IElement> node : this.nodeMap.values() ) {
			if ( node.getElem().isEnabled() )
				node.enable();
			else
				node.disable();
			
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
			subMonitor.beginTask("Updating edges.", node.getEdges().size());
			for ( IntentionEdge<IElement> edge : node.getEdges()) {
				if ( edge.getToNode().hasEnabledEdgesForIncommingRelation(edge.getType()))
					edge.enable();
				else
					edge.disable();
				subMonitor.worked(1);
			}
			subMonitor.done();
			monitor.worked(1);
		}
		monitor.done();
	}
	
	public String getProlog(IProgressMonitor monitor) {
		StringBuilder ret = new StringBuilder();
		monitor.beginTask("Writing nodes", this.nodeMap.values().size());
		for ( IntentionNode<IElement> node : this.nodeMap.values() ) {
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, -1);
			subMonitor.beginTask("Writing edges", node.getEdges().size());
			for ( IntentionEdge<IElement> edge : node.getEdges() ) {
				ret.append(edge.getType().toString().toLowerCase());
				ret.append('(');
				ret.append("'" + edge.getFromNode().getElem() + "'");
				ret.append(',');
				ret.append("'" + edge.getToNode().getElem() + "'");
				ret.append(')');
				ret.append(". ");
				subMonitor.worked(1);
			}
			monitor.worked(1);
		}
		return ret.toString();
	}

	private IntentionNode<IElement> getNode(IElement elem) throws Exception {
		if (nodeMap.containsKey(elem)) {
			return nodeMap.get(elem);
		} else {
			IntentionNode<IElement> node = new IntentionNode<IElement>(
					elem);
			nodeMap.put(elem, node);
			return node;
		}
	}

	public String toDotFormat() {
		StringBuilder ret = new StringBuilder();
		ret.append("digraph {");
		for (IntentionNode<IElement> node : this.nodeMap.values()) {
			ret.append(node.toDotFormat());
		}
		ret.append('}');
		return ret.toString();
	}

	/**
	 * @return
	 */
	public Set<IntentionNode<IElement>> commenceDFS() {
//		for ( IntentionNode<IElement> node : white )
//			node.dfs();
		return null;
	}
	
	public Collection<IntentionNode<IElement>> getNodes() {
		return this.nodeMap.values();
	}

	/**
	 * @return
	 */
	public Set<IntentionElement<IElement>> flatten() {
		Set<IntentionElement<IElement>> ret = new LinkedHashSet<IntentionElement<IElement>>();
		for ( IntentionNode<IElement> node : this.getNodes() ) {
			ret.add(node);
			for ( IntentionEdge<IElement> edge : node.getEdges() )
				ret.add(edge);
		}
		return ret;
	}

	/**
	 * @return the elementsCurrentlyEnabledAccordingToElem
	 */
	public AdviceElement getElementsCurrentlyEnabledAccordingToElem() {
		return this.elementsCurrentlyEnabledAccordingToElem;
	}
}