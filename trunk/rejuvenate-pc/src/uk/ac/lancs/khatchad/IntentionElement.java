/**
 * 
 */
package uk.ac.lancs.khatchad;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author raffi
 * 
 * @param <E>
 */
public abstract class IntentionElement<E> {

	private final PropertyChangeSupport changes = new PropertyChangeSupport(
			this);

	private boolean enabled;

	/**
	 * 
	 */
	public IntentionElement() {
		super();
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public void enable() {
		boolean oldState = this.enabled;
		this.enabled = true;
		if (oldState != this.enabled)
			this.changes.firePropertyChange(new PropertyChangeEvent(this,
					"enabled", oldState, this.enabled));
	}

	public void disable() {
		boolean oldState = this.enabled;
		this.enabled = false;
		if (oldState != this.enabled)
			this.changes.firePropertyChange(new PropertyChangeEvent(this,
					"enabled", oldState, this.enabled));
	}

	@Override
	public String toString() {
		return this.enabled ? "*" : "";
	}

	public void addPropertyChangeListener(final PropertyChangeListener l) {
		this.changes.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(final PropertyChangeListener l) {
		this.changes.removePropertyChangeListener(l);
	}
}