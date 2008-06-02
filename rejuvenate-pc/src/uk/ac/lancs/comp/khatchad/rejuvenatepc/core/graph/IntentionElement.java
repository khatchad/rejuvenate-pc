/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import org.jdom.Element;

/**
 * @author raffi
 * 
 * @param <E>
 */
public abstract class IntentionElement<E> implements Serializable {

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
					"enabled", oldState, this.enabled));
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
					"enabled", oldState, this.enabled));
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
	public abstract Element getXML();

	/**
	 * @return
	 */
	public abstract String getLongDescription();
}