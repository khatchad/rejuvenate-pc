/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.lang.JoinPoint;
import org.aspectj.weaver.AsmRelationshipUtils;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaElement;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import uk.ac.lancs.comp.khatchad.ajayfx.Converter;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.DatabaseUtil;

import ca.mcgill.cs.swevo.jayfx.ConversionException;
import ca.mcgill.cs.swevo.jayfx.FastConverter;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.model.FieldElement;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
import ca.mcgill.cs.swevo.jayfx.model.MethodElement;
import ca.mcgill.cs.swevo.jayfx.model.Relation;

/**
 * @author raffi
 * 
 */
public class IntentionArc<E extends IElement> extends IntentionElement<E> {

	/**
	 * 
	 */
	private static final String SOURCE = "source";

	/**
	 * 
	 */
	private static final String TARGET = "target";

	private static final long serialVersionUID = -4758844315757084370L;

	private IntentionNode<E> fromNode;

	private IntentionNode<E> toNode;

	private Relation type;

	/**
	 * 
	 */
	public IntentionArc() {
	}

	/**
	 * @param from
	 * @param to
	 * @param type
	 */
	public IntentionArc(final IntentionNode<E> from, final IntentionNode<E> to,
			final Relation type) {
		this.fromNode = from;
		this.toNode = to;
		this.type = type;
	}

	/**
	 * @param node
	 * @param toNode
	 * @param relation
	 * @param enableEdgesForIncommingRelation
	 */
	public IntentionArc(final IntentionNode<E> from, final IntentionNode<E> to,
			final Relation type, final boolean enabled) {
		this(from, to, type);
		if (enabled)
			this.enable();
		else
			this.disable();
	}

	/**
	 * @param elem
	 * @throws DataConversionException
	 */
	public IntentionArc(Element elem) throws DataConversionException {
		super(elem);

		Element typeElem = elem.getChild(Relation.class.getSimpleName());
		this.type = Relation.valueOf(typeElem);

		Element sourceElem = elem.getChild(SOURCE).getChild(
				IntentionNode.class.getSimpleName());
		this.fromNode = recoverNode(sourceElem);

		Element targetElem = elem.getChild(TARGET).getChild(
				IntentionNode.class.getSimpleName());
		this.toNode = recoverNode(targetElem);
	}

	/**
	 * @param sourceElem
	 * @throws DataConversionException
	 */
	private static <E extends IElement> IntentionNode<E> recoverNode(
			Element sourceElem) throws DataConversionException {
		if (WildcardElement.isWildcardElement(sourceElem
				.getChild(IElement.class.getSimpleName())))
			return (IntentionNode<E>) (IntentionElement.isEnabled(sourceElem) ? IntentionNode.ENABLED_WILDCARD
					: IntentionNode.DISABLED_WILDCARD);
		else
			return new IntentionNode<E>(sourceElem);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof IntentionArc ? this.fromNode
				.equals(((IntentionArc) obj).fromNode)
				&& this.toNode.equals(((IntentionArc) obj).toNode)
				&& this.type.equals(((IntentionArc) obj).type) : false;
	}

	/**
	 * @return the from
	 */
	public IntentionNode<E> getFromNode() {
		return this.fromNode;
	}

	/**
	 * @return the to
	 */
	public IntentionNode<E> getToNode() {
		return this.toNode;
	}

	/**
	 * @return the type
	 */
	public Relation getType() {
		return this.type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.fromNode == null || this.toNode == null || this.type == null)
			throw new IllegalStateException(
					"State can not have null attributes");
		return this.fromNode.hashCode() + this.toNode.hashCode()
				+ this.type.hashCode();
	}

	/**
	 * @param fromNode
	 *            the fromNode to set
	 */
	//	public void setFromNode(final IntentionNode<E> fromNode) {
	//		this.fromNode = fromNode;
	//	}
	//
	//	/**
	//	 * @param toNode
	//	 *            the toNode to set
	//	 */
	//	public void setToNode(final IntentionNode<E> toNode) {
	//		this.toNode = toNode;
	//	}
	/**
	 * @param type
	 *            the type to set
	 */
	//	public void setType(final Relation type) {
	//		this.type = type;
	//	}
	public String toDotFormat() {
		final StringBuilder ret = new StringBuilder();
		ret.append(this.fromNode.hashCode());
		ret.append("->");
		ret.append(this.toNode.hashCode());
		ret.append(' ');
		ret.append("[label=");
		ret.append("\"");
		ret.append(this.type.getFullCode());
		ret.append("\"");
		if (this.isEnabled())
			ret.append(",style=bold,color=red,fontcolor=red");
		ret.append("];");
		return ret.toString();
	}

	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder();
		ret.append(super.toString());
		//		ret.append('(');
		//		ret.append(from.getElem().getShortName());
		//		ret.append(',');
		//		ret.append(to.getElem().getShortName());
		//		ret.append(')');
		ret.append(this.type.getFullCode());
		return ret.toString();
	}

	@Override
	public String getLongDescription() {
		StringBuilder ret = new StringBuilder();
		ret.append(super.toString());
		ret.append(this.type.toString() + ": ");
		ret.append(this.getToNode().getLongDescription());
		return ret.toString();
	}

	public Element getXML() {
		Element ret = super.getXML();

		Element typeXML = this.type.getXML();
		ret.addContent(typeXML);

		Element source = new Element(SOURCE);
		source.addContent(this.getFromNode().getXML());
		ret.addContent(source);

		Element target = new Element(TARGET);
		target.addContent(this.getToNode().getXML());
		ret.addContent(target);

		return ret;
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement#getPrettyString()
	 */
	@Override
	public String toPrettyString() {
		StringBuilder ret = new StringBuilder(this.toString());
		ret.append(": ");
		ret.append(this.toNode);
		return ret.toString();
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement#toJavaElement(ca.mcgill.cs.swevo.jayfx.FastConverter)
	 */
	@Override
	public IJavaElement toJavaElement(JayFX database) throws ConversionException {
		if (!this.isAdvisable())
			return null; //a non-advisable relation has no IJavaElement.

		//TODO: Need to find line number. Also, there may be more than one for a given arc.
		
		IJavaElement source = this.fromNode.toJavaElement(database);
		IJavaElement target = this.toNode.toJavaElement(database);
		
		if (source == null || target == null)
			return null;

		String targetString = null;
		try {
			targetString = getTargetString(target);
		}
		catch (JavaModelException e) {
			return null; //can't get IJavaElement.
		}
		catch(IllegalArgumentException e) {
			return null;
		}

		StringBuilder name = new StringBuilder(this.getJoinPointTypeAsString());
		name.append('(');
		name.append(targetString);
		name.append(')');

		AJCodeElement ret = new AJCodeElement((JavaElement) source, name
				.toString(), 0);

		return ret;
	}

	/**
	 * @param target
	 * @return
	 * @throws JavaModelException
	 * @throws ConversionException 
	 */
	private String getTargetString(IJavaElement target)
			throws JavaModelException, ConversionException {
		switch (target.getElementType()) {
			case IJavaElement.METHOD: {
				IMethod methodTarget = (IMethod) target;
				MethodElement methodElement = Converter.getMethodElement(methodTarget);
				return methodElement.getId();
			}
			
			case IJavaElement.FIELD: {
				IField fieldTarget = (IField)target;
				FieldElement fieldElement = Converter.getFieldElement(fieldTarget);
				return fieldElement.getId();
			}
			//TODO: Add other types? Exception handles, etc.?

			default: {
				throw new IllegalArgumentException(
						"Can't construct target string for " + target);
			}
		}
	}

	private String getJoinPointTypeAsString() {
		switch (this.type) {
			case CALLS:
			case EXPLICITLY_CALLS:
			case STATIC_CALLS:
				if (this.toNode.getElem() instanceof MethodElement
						&& ((MethodElement) this.toNode.getElem())
								.isConstructor())
					return JoinPoint.CONSTRUCTOR_CALL;
				else
					return JoinPoint.METHOD_CALL;

			case GETS:
				return JoinPoint.FIELD_GET;

			case SETS:
				return JoinPoint.FIELD_SET;

			default:
				throw new IllegalStateException("Relation " + this
						+ " has no associated join point kind.");
		}
	}

	/**
	 * @return
	 */
	public boolean isAdvisable() {
		return this.type.isAdvisable();
	}
}