/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.IAJCodeElement;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

import uk.ac.lancs.comp.khatchad.ajayfx.model.JoinpointType;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.Util;

import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class IntentionGraph {

	//	private AdviceElement elementsCurrentlyEnabledAccordingToAdvice;
	private static final String INIT_STRING = ".<init>";

	private Set<IntentionNode<IElement>> nodeSet = new LinkedHashSet<IntentionNode<IElement>>();

	private Map<IElement, IntentionNode<IElement>> elementToNodeMap = new LinkedHashMap<IElement, IntentionNode<IElement>>();

	private void buildNodes(IProgressMonitor monitor) {
		monitor.beginTask("Building Intention Nodes", database.getAllElements()
				.size());

		for (final IElement elem : database.getAllElements()) {
			IntentionNode<IElement> node = new IntentionNode<IElement>(elem);
			this.nodeSet.add(node);
			this.elementToNodeMap.put(elem, node);
			monitor.worked(1);
		}
		monitor.done();
	}

	public Set<IntentionElement<IElement>> flatten() {
		final Set<IntentionElement<IElement>> ret = new LinkedHashSet<IntentionElement<IElement>>();
		for (final IntentionNode<IElement> node : this.getNodes()) {
			ret.add(node);
			for (final IntentionArc<IElement> edge : node.getArcs())
				ret.add(edge);
		}
		return ret;
	}

	private void buildArcs(IProgressMonitor monitor) {

		monitor.beginTask("Building Intention Arcs", this.nodeSet.size());

		for (IntentionNode<IElement> node : this.nodeSet) {
			// now make the edges.
			this.makeArcs(node, database, Relation.GETS);
			this.makeArcs(node, database, Relation.SETS);
			this.makeArcs(node, database, Relation.CALLS);
			this.makeArcs(node, database, Relation.OVERRIDES);
			this.makeArcs(node, database, Relation.IMPLEMENTS_METHOD);
			this.makeArcs(node, database, Relation.DECLARES_METHOD);
			this.makeArcs(node, database, Relation.DECLARES_FIELD);
			this.makeArcs(node, database, Relation.DECLARES_TYPE);
			this.makeArcs(node, database, Relation.EXTENDS_CLASS);
			this.makeArcs(node, database, Relation.EXTENDS_INTERFACES);
			this.makeArcs(node, database, Relation.IMPLEMENTS_INTERFACE);
			this.makeArcs(node, database, Relation.CONTAINS);
			this.makeArcs(node, database, Relation.ANNOTATES);
			this.makeArcs(node, database, Relation.ADVISES);

			monitor.worked(1);
		}
		monitor.done();
	}

	private JayFX database;

	public IntentionGraph(final JayFX database, final IProgressMonitor monitor)
			throws Exception {
		this.database = database;
		buildNodes(new SubProgressMonitor(monitor, -1));
		buildArcs(new SubProgressMonitor(monitor, -1));
	}

	public void enableElementsAccordingTo(final AdviceElement advisingElement,
			final IProgressMonitor monitor) throws JavaModelException,
			ConversionException {

		this.resetAllElements(new SubProgressMonitor(monitor, -1));

		final List<AJRelationship> relationshipList = Util
				.getAdviceRelationshipList(advisingElement);

		monitor.beginTask("Enabling elements according to advice pointcut.",
				relationshipList.size());

		for (final AJRelationship relationship : relationshipList) {

			if (relationship.getSource().equals(advisingElement)) {
				final IJavaElement target = relationship.getTarget();
				enableElementsAccordingTo(target, new SubProgressMonitor(
						monitor, -1));
			}
			monitor.worked(1);
		}
		monitor.done();
	}

	//	public AdviceElement getElementsCurrentlyEnabledAccordingToAdvice() {
	//		return this.elementsCurrentlyEnabledAccordingToAdvice;
	//	}

	/**
	 * @param target
	 * @param monitor
	 * @throws ConversionException
	 * @throws JavaModelException
	 */
	private void enableElementsAccordingTo(IJavaElement advisedElement,
			IProgressMonitor monitor) throws ConversionException,
			JavaModelException {

		switch (advisedElement.getElementType()) {

			case IJavaElement.METHOD: {
				final IMethod meth = (IMethod) advisedElement;

				//No advice for now.
				if (meth.getParent() instanceof AspectElement)
					break;

				final IElement toEnableElement = this.database
						.convertToElement(meth);

				if (toEnableElement == null)
					throw new IllegalStateException("In trouble!");

				IntentionNode<IElement> toEnableNode = this.elementToNodeMap
						.get(toEnableElement);
				toEnableNode.enable();

				break;
			}

			case IJavaElement.TYPE: {

				// its a default ctor.
				final IType type = (IType) advisedElement;

				for (final IMethod meth : type.getMethods())

					if (meth.isConstructor()
							&& meth.getParameterNames().length == 0) {

						final IElement toEnableElement = this.database
								.convertToElement(meth);

						if (toEnableElement == null)
							throw new IllegalStateException("In trouble!");

						IntentionNode<IElement> toEnableNode = this.elementToNodeMap
								.get(toEnableElement);
						toEnableNode.enable();

					}

				break;

			}

			case IJavaElement.LOCAL_VARIABLE: {

				// its an aspect element.
				if (!(advisedElement instanceof IAJCodeElement))
					throw new IllegalStateException("Something is screwy here.");

				final IAJCodeElement ajElem = (IAJCodeElement) advisedElement;
				JoinpointType joinPointType = getJoinPointType(ajElem);
				IJavaElement source = advisedElement.getParent();
				String targetString = getTargetString(ajElem);

				switch (joinPointType) {
					case FIELD_GET: {
						this.enableElementsAccordingToFieldGet(source,
								targetString, monitor);
						break;
					}

					case FIELD_SET: {
						this.enableElementsAccordingToFieldSet(source,
								targetString, monitor);
						break;
					}

					case METHOD_CALL: {
						this.enableElementsAccordingToMethodCall(source,
								targetString, monitor);
						break;
					}

					case CONSTRUCTOR_CALL: {
						this.enableElementsAccordingToConstructorCall(source,
								targetString, monitor);
						break;
					}

					case EXCEPTION_HANDLER: {
						System.out
								.println("Encountered handler-based advice, not sure how to deal with this yet. Nothing enabled.");
						break;
					}
				}

				break;
			}
			default:
				throw new IllegalStateException(
						"Unexpected relationship target type: "
								+ advisedElement.getElementType());
		}
	}

	private void enableElementsAccordingToFieldGet(IJavaElement parent,
			String targetString, IProgressMonitor monitor)
			throws ConversionException {
		String fieldNameTargetString = transformTargetStringToFieldName(targetString);
		this.enableElementsAccordingToRelation(parent, fieldNameTargetString,
				IJavaSearchConstants.FIELD, Relation.GETS, monitor);
	}

	private void enableElementsAccordingToFieldSet(IJavaElement parent,
			String targetString, IProgressMonitor monitor)
			throws ConversionException {
		String fieldNameTargetString = transformTargetStringToFieldName(targetString);
		this.enableElementsAccordingToRelation(parent, fieldNameTargetString,
				IJavaSearchConstants.FIELD, Relation.SETS, monitor);
	}

	private void enableElementsAccordingToConstructorCall(IJavaElement parent,
			String targetString, IProgressMonitor monitor)
			throws ConversionException {
		String constructorNameTargetString = transformTargetStringToConstructorName(targetString);
		this.enableElementsAccordingToRelation(parent,
				constructorNameTargetString, IJavaSearchConstants.METHOD,
				Relation.CALLS, monitor);
	}

	private void enableElementsAccordingToMethodCall(IJavaElement parent,
			String targetString, IProgressMonitor monitor)
			throws ConversionException {
		String methodNameTargetString = transformTargetStringToMethodName(targetString);
		this.enableElementsAccordingToRelation(parent, methodNameTargetString,
				IJavaSearchConstants.METHOD, Relation.CALLS, monitor);
	}

	private void enableElementsAccordingToRelation(IJavaElement parent,
			String targetString, final int javaSearchConstant,
			Relation relation, IProgressMonitor monitor)
			throws ConversionException {

		final SearchPattern pattern = SearchPattern.createPattern(targetString,
				javaSearchConstant, IJavaSearchConstants.DECLARATIONS,
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);

		final Collection<SearchMatch> results = Util.search(pattern,
				SearchEngine.createWorkspaceScope(), monitor);

		IElement sourceElement = this.database.convertToElement(parent);
		IntentionNode<IElement> sourceNode = this.getNode(sourceElement);

		for (final SearchMatch match : results) {

			final IElement targetElement = this.database
					.convertToElement((IJavaElement) match.getElement());

			//find the edge connecting the source to the target and enable it.
			IntentionNode<IElement> targetNode = this.getNode(targetElement);
			IntentionArc<IElement> arcToEnable = sourceNode.getArc(targetNode,
					relation);
			arcToEnable.enable();
		}
	}

	private static String transformTargetStringToFieldName(
			final String targetString) {
		StringBuilder ret = new StringBuilder(targetString);
		ret.delete(0, targetString.indexOf(" ") + 1);
		ret.deleteCharAt(targetString.length() - 1);
		return ret.toString();
	}

	private static String transformTargetStringToMethodName(
			final String targetString) {
		StringBuilder ret = new StringBuilder(targetString);
		ret.delete(0, targetString.indexOf(" ") + 1);
		ret.deleteCharAt(targetString.length() - 1);
		return ret.toString();
	}

	private static String transformTargetStringToConstructorName(
			final String targetString) {
		String methodTargetString = transformTargetStringToMethodName(targetString);
		StringBuilder ret = new StringBuilder(methodTargetString);
		final int initPos = ret.indexOf(INIT_STRING);
		ret.delete(initPos, initPos + INIT_STRING.length());
		return ret.toString();
	}

	/**
	 * @param ajElem
	 */
	private static JoinpointType getJoinPointType(final IAJCodeElement ajElem) {
		final String joinPointTypeAsString = getTargetString(ajElem);

		final JoinpointType joinPointTypeAsEnum = JoinpointType
				.valueOf(joinPointTypeAsString);

		return joinPointTypeAsEnum;
	}

	/**
	 * @param ajElem
	 * @return
	 */
	private static String getTargetString(final IAJCodeElement ajElem) {
		final StringBuilder targetString = new StringBuilder(ajElem
				.getElementName());
		final String type = targetString
				.substring(0, targetString.indexOf("("));
		final StringBuilder typeBuilder = new StringBuilder(type.toUpperCase());
		final int pos = typeBuilder.indexOf("-");

		final String joinPointTypeAsString = typeBuilder.replace(pos, pos + 1,
				"_").toString();
		return joinPointTypeAsString;
	}

	private void resetAllElements(IProgressMonitor monitor) {
		// reset all elements.	
		Set<IntentionElement<IElement>> allElements = this.flatten();
		monitor.beginTask("Disabling intention elements.", allElements.size());
		for (final IntentionElement<IElement> elem : allElements) {
			elem.disable();
			monitor.worked(1);
		}
		monitor.done();
	}

	private IntentionNode<IElement> getNode(final IElement elem) {
		if (this.elementToNodeMap.containsKey(elem))
			return this.elementToNodeMap.get(elem);
		else {
			final IntentionNode<IElement> node = new IntentionNode<IElement>(
					elem);
			//Let's not consider nodes outside the projects.
			//			this.nodeSet.add(node);
			this.elementToNodeMap.put(elem, node);
			return node;
		}
	}

	public Collection<IntentionElement<IElement>> getEnabledElements() {
		final Collection<IntentionElement<IElement>> ret = new LinkedHashSet<IntentionElement<IElement>>();
		for (final IntentionNode<IElement> node : this.getNodes()) {
			if (node.isEnabled())
				ret.add(node);
			for (final IntentionArc<IElement> edge : node.getArcs())
				if (edge.isEnabled())
					ret.add(edge);
		}
		return ret;
	}

	public Collection<IntentionNode<IElement>> getNodes() {
		return this.nodeSet;
	}

	public String getProlog(final IProgressMonitor monitor) {
		final StringBuilder ret = new StringBuilder();
		monitor.beginTask("Writing nodes", this.nodeSet.size());
		for (final IntentionNode<IElement> node : this.nodeSet) {
			final SubProgressMonitor subMonitor = new SubProgressMonitor(
					monitor, -1);
			subMonitor.beginTask("Writing arcs", node.getArcs().size());
			for (final IntentionArc<IElement> edge : node.getArcs()) {
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
		for (final IntentionNode<IElement> node : this.nodeSet) {
			ret.append('\t');
			ret.append(node.toDotFormat());
			ret.append('\n');
		}
		ret.append('}');
		return ret.toString();
	}

	private void makeArcs(final IntentionNode<IElement> fromNode,
			JayFX database, final Relation relation) {

		for (final IElement toElement : database.getRange(fromNode.getElem(),
				relation)) {

			final IntentionNode<IElement> toNode = this.getNode(toElement);

			final IntentionArc<IElement> arc = new IntentionArc<IElement>(
					fromNode, toNode, relation);

			fromNode.addArc(arc);
		}
	}

	public void enableElementsAccordingTo(
			Collection<IJavaElement> advisedElements, IProgressMonitor monitor) {
		//TODO
		//			throws JavaModelException, ConversionException, CoreException {
		//		monitor.beginTask(
		//				"Re-enabling elements according to retrieved information.",
		//				advisedElements.size());
		//		for (IJavaElement elem : advisedElements) {
		//			this.database.enableElementsAccordingTo(elem,
		//					new SubProgressMonitor(monitor, -1));
		//			monitor.worked(1);
		//		}
		//		this.updateStateToReflectDatabase(new SubProgressMonitor(monitor, 1));
	}
}