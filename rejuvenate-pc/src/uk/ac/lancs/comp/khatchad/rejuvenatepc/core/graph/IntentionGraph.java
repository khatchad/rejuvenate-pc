/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class IntentionGraph<E extends IntentionNode<IElement>> {

	private AdviceElement elementsCurrentlyEnabledAccordingToElem;

	private final Map<IElement, IntentionNode<IElement>> nodeMap = new HashMap<IElement, IntentionNode<IElement>>();

	private final JayFX database;

	public IntentionGraph(final JayFX database, final IProgressMonitor monitor)
			throws Exception {
		this.database = database;

		monitor.beginTask("Building Intention Graph", database.getAllElements()
				.size());
		for (final IElement elem : database.getAllElements()) {
			IntentionNode<IElement> node;
			if (this.nodeMap.containsKey(elem))
				node = this.nodeMap.get(elem);
			else {
				// make a node for this element.
				// IJavaElement jElem =
				// this.database.convertToJavaElement(elem);
				node = new IntentionNode<IElement>(elem);
				this.nodeMap.put(elem, node);
			}

			// now make the edges.
			this.makeEdges(elem, node, Relation.GETS);
			this.makeEdges(elem, node, Relation.SETS);
			this.makeEdges(elem, node, Relation.CALLS);
			this.makeEdges(elem, node, Relation.OVERRIDES);
			this.makeEdges(elem, node, Relation.IMPLEMENTS_METHOD);
			this.makeEdges(elem, node, Relation.DECLARES_METHOD);
			this.makeEdges(elem, node, Relation.DECLARES_FIELD);
			this.makeEdges(elem, node, Relation.DECLARES_TYPE);
			this.makeEdges(elem, node, Relation.EXTENDS_CLASS);
			this.makeEdges(elem, node, Relation.EXTENDS_INTERFACES);
			this.makeEdges(elem, node, Relation.IMPLEMENTS_INTERFACE);
			this.makeEdges(elem, node, Relation.CONTAINS);
			this.makeEdges(elem, node, Relation.ANNOTATES);
			this.makeEdges(elem, node, Relation.ADVISES);

			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * @return
	 */
	public Set<IntentionNode<IElement>> commenceDFS() {
		// for ( IntentionNode<IElement> node : white )
		// node.dfs();
		return null;
	}

	public void enableElementsAccordingTo(final AdviceElement advElem,
			final IProgressMonitor monitor) throws ConversionException,
			CoreException {
		this.database.enableElementsAccordingTo(advElem,
				new SubProgressMonitor(monitor, 1));
		this.updateStateToReflectDatabase(new SubProgressMonitor(monitor, 1));
	}

	/**
	 * @return
	 */
	public Set<IntentionElement<IElement>> flatten() {
		final Set<IntentionElement<IElement>> ret = new LinkedHashSet<IntentionElement<IElement>>();
		for (final IntentionNode<IElement> node : this.getNodes()) {
			ret.add(node);
			for (final IntentionEdge<IElement> edge : node.getEdges())
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

	public Collection<IntentionElement<IElement>> getEnabledElements() {
		final Collection<IntentionElement<IElement>> ret = new LinkedHashSet<IntentionElement<IElement>>();
		for (final IntentionNode<IElement> node : this.getNodes()) {
			if (node.isEnabled())
				ret.add(node);
			for (final IntentionEdge<IElement> edge : node.getEdges())
				if (edge.isEnabled())
					ret.add(edge);
		}
		return ret;
	}

	public Collection<IntentionNode<IElement>> getNodes() {
		return this.nodeMap.values();
	}

	public String getProlog(final IProgressMonitor monitor) {
		final StringBuilder ret = new StringBuilder();
		monitor.beginTask("Writing nodes", this.nodeMap.values().size());
		for (final IntentionNode<IElement> node : this.nodeMap.values()) {
			final SubProgressMonitor subMonitor = new SubProgressMonitor(
					monitor, -1);
			subMonitor.beginTask("Writing edges", node.getEdges().size());
			for (final IntentionEdge<IElement> edge : node.getEdges()) {
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

	public String toDotFormat() {
		final StringBuilder ret = new StringBuilder();
		ret.append("digraph {");
		ret.append('\n');
		for (final IntentionNode<IElement> node : this.nodeMap.values()) {
			ret.append('\t');
			ret.append(node.toDotFormat());
			ret.append('\n');
		}
		ret.append('}');
		return ret.toString();
	}

	private IntentionNode<IElement> getNode(final IElement elem)
			throws Exception {
		if (this.nodeMap.containsKey(elem))
			return this.nodeMap.get(elem);
		else {
			final IntentionNode<IElement> node = new IntentionNode<IElement>(
					elem);
			this.nodeMap.put(elem, node);
			return node;
		}
	}

	/**
	 * @param database
	 * @param elem
	 * @param fromNode
	 * @throws Exception
	 */
	private void makeEdges(final IElement elem,
			final IntentionNode<IElement> fromNode, final Relation relation)
			throws Exception {
		for (final IElement toElement : this.database.getRange(elem, relation)) {
			final IntentionNode<IElement> toNode = this.getNode(toElement);
			final IntentionEdge<IElement> edge = new IntentionEdge<IElement>(
					fromNode, toNode, relation, toNode
							.hasEnabledEdgesForIncommingRelation(relation));
			fromNode.addEdge(edge);
		}
	}

	/**
	 * Updates the graph to reflect changes in the underlying DB.
	 * 
	 * @param monitor
	 */
	private void updateStateToReflectDatabase(final IProgressMonitor monitor) {
		monitor.beginTask("Updating nodes.", this.nodeMap.values().size());
		for (final IntentionNode<IElement> node : this.nodeMap.values()) {
			if (node.getElem().isEnabled())
				node.enable();
			else
				node.disable();

			final IProgressMonitor subMonitor = new SubProgressMonitor(monitor,
					1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
			subMonitor.beginTask("Updating edges.", node.getEdges().size());
			for (final IntentionEdge<IElement> edge : node.getEdges()) {
				if (edge.getToNode().hasEnabledEdgesForIncommingRelation(
						edge.getType()))
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

	/**
	 * @param advisedElements
	 * @param monitor
	 * @throws CoreException 
	 * @throws ConversionException 
	 * @throws JavaModelException 
	 */
	public void enableElementsAccordingTo(
			Collection<IJavaElement> advisedElements, IProgressMonitor monitor) throws JavaModelException, ConversionException, CoreException {
		monitor.beginTask("Re-enabling elements according to retrieved information.", advisedElements.size());
		for ( IJavaElement elem : advisedElements ) {
			this.database.enableElementsAccordingTo(elem);
			monitor.worked(1);
		}
		this.updateStateToReflectDatabase(new SubProgressMonitor(monitor, 1));
	}
}