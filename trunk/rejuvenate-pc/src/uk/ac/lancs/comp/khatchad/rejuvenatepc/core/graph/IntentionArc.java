/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.weaver.AsmRelationshipUtils;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJProjectModel;
import org.eclipse.jdt.core.IJavaElement;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.DatabaseUtil;

import ca.mcgill.cs.swevo.jayfx.FastConverter;
import ca.mcgill.cs.swevo.jayfx.JayFX;
import ca.mcgill.cs.swevo.jayfx.model.IElement;
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
	public IntentionArc(final IntentionNode<E> from,
			final IntentionNode<E> to, final Relation type) {
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
	public IntentionArc(final IntentionNode<E> from,
			final IntentionNode<E> to, final Relation type,
			final boolean enabled) {
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
		
		Element sourceElem = elem.getChild(SOURCE).getChild(IntentionNode.class.getSimpleName());
		this.fromNode = recoverNode(sourceElem);
		
		Element targetElem = elem.getChild(TARGET).getChild(IntentionNode.class.getSimpleName());
		this.toNode = recoverNode(targetElem);
	}

	/**
	 * @param sourceElem
	 * @throws DataConversionException
	 */
	private static <E extends IElement> IntentionNode<E> recoverNode(Element sourceElem) throws DataConversionException {
		if ( WildcardElement.isWildcardElement(sourceElem.getChild(IElement.class.getSimpleName())) ) 
			return (IntentionNode<E>)(IntentionElement.isEnabled(sourceElem) ? IntentionNode.ENABLED_WILDCARD : IntentionNode.DISABLED_WILDCARD);
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
		if ( this.fromNode == null || this.toNode == null || this.type == null )
			throw new IllegalStateException("State can not have null attributes");
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

	/**
	 * Returns the AJCodeElements corresponding to this edge.
	 * 
	 * @return The AJCodeElements corresponding to this edge.
	 */

	public Set<IJavaElement> getJavaElement() {
		//TODO: Need to *somehow* get AJCodeElements here. Perhaps use search engine?
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement#getPrettyString()
	 */
	@Override
	public String toPrettyString() {
		StringBuilder ret = new StringBuilder(this.toString().toLowerCase());
		ret.append(": ");
		ret.append(this.toNode);
		return ret.toString();
	}

	/* (non-Javadoc)
	 * @see uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement#toJavaElement(ca.mcgill.cs.swevo.jayfx.FastConverter)
	 */
	@Override
	public IJavaElement toJavaElement(JayFX database) {
		//TODO: Need to find line number.
		
		IJavaElement source = this.fromNode.toJavaElement(database);
		IJavaElement target = this.toNode.toJavaElement(database);
		
		IProgramElement p = new ProgramElement();
		AsmManager.getDefault().getHandleProvider().createHandleIdentifier()
		
		System.out.println(target);
		
//		AJCodeElement ajc = new AJCodeElement()
		
		IJavaElement test = AspectJCore.create("=SimpleTestCase0/src<p{A.java[A~m2?method-call(void p.A.p())!13!0!0!0!I");
		System.out.println(DatabaseUtil.getKey(test));
		
		return null;
	}
}