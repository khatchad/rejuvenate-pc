/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * @author raffi
 * 
 * @param <E>
 */
public abstract class IntentionElement<E> implements Serializable {

	/**
	 * 
	 */
	private static final String ENABLED = "enabled";

	private final PropertyChangeSupport changes = new PropertyChangeSupport(
			this);

	private boolean enabled;

	/**
	 * 
	 */
	public IntentionElement() {
		super();
	}

	public void addPropertyChangeListener(final PropertyChangeListener l) {
		this.changes.addPropertyChangeListener(l);
	}

	public void disable() {
		final boolean oldState = this.enabled;
		this.enabled = false;
		if (oldState != this.enabled)
			this.changes.firePropertyChange(new PropertyChangeEvent(this,
					ENABLED, oldState, this.enabled));
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public void enable() {
		final boolean oldState = this.enabled;
		this.enabled = true;
		if (oldState != this.enabled)
			this.changes.firePropertyChange(new PropertyChangeEvent(this,
					ENABLED, oldState, this.enabled));
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	public void removePropertyChangeListener(final PropertyChangeListener l) {
		this.changes.removePropertyChangeListener(l);
	}

	@Override
	public String toString() {
		return this.enabled ? "*" : "";
	}

	/**
	 * @return
	 */
	public Element getXML() {
		Element ret = new Element(this.getClass().getSimpleName());
		ret.setAttribute(ENABLED, String.valueOf(this.isEnabled()));
		return ret;
	}

	public static boolean isEnabled(Element elem)
			throws DataConversionException {
		Attribute enabledAttribute = elem.getAttribute(ENABLED);
		return enabledAttribute.getBooleanValue();
	}

	public IntentionElement(Element elem) throws DataConversionException {
		this.enabled = isEnabled(elem);
	}

	/**
	 * @return
	 */
	public abstract String getLongDescription();
}