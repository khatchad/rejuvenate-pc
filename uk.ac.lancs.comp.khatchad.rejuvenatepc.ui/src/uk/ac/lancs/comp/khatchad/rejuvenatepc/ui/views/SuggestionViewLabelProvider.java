/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.views;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.model.Suggestion;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util.JDTUtil;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.SuggestionUIPlugin;

class SuggestionViewLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public ILabelProvider labelProvider;

	public SuggestionViewLabelProvider() {
		this.labelProvider = new DecoratingLabelProvider(
				new JavaElementLabelProvider(), SuggestionUIPlugin.getDefault()
						.getWorkbench().getDecoratorManager()
						.getLabelDecorator());
	}

	private boolean addedListener = false;

	private ListenerList fListeners;

	public void addListener(ILabelProviderListener listener) {
		super.addListener(listener);
		if (fListeners == null) {
			fListeners = new ListenerList();
		}
		fListeners.add(listener);
		if (!addedListener) {
			addedListener = true;
			// as we are only retrieving images from labelProvider not using it
			// directly, we need to update this label provider whenever that one
			// updates
			labelProvider.addListener(new ILabelProviderListener() {
				public void labelProviderChanged(LabelProviderChangedEvent event) {
					fireLabelChanged();
				}
			});
		}
	}

	private void fireLabelChanged() {
		if (fListeners != null && !fListeners.isEmpty()) {
			LabelProviderChangedEvent event = new LabelProviderChangedEvent(
					this);
			Object[] listeners = fListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				((ILabelProviderListener) listeners[i])
						.labelProviderChanged(event);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public String getColumnText(Object obj, int index) {
		if (!(obj instanceof Suggestion))
			throw new IllegalArgumentException("Expecting Suggestion, not "
					+ obj.getClass().getSimpleName());
		Suggestion<IJavaElement> suggestion = (Suggestion<IJavaElement>) obj;
		switch (index) {
		case 0:
			return labelProvider.getText(suggestion.getSuggestion());
		case 1:
			return suggestion.getPattern().toString();
		case 2:
			return String.valueOf(suggestion.getConfidence());
		default:
			throw new IllegalArgumentException("Invalid column number: "
					+ index);
		}
	}

	@SuppressWarnings("unchecked")
	public Image getColumnImage(Object obj, int index) {
		if (index == 0) {
			if (!(obj instanceof Suggestion))
				throw new IllegalArgumentException("Expecting Suggestion, not "
						+ obj.getClass().getSimpleName());
			Suggestion<IJavaElement> suggestion = (Suggestion<IJavaElement>) obj;
			return this.labelProvider.getImage(suggestion.getSuggestion());
		} else
			return null; // no image for you.
	}
	
	public void dispose() {
		fListeners = null;
		if(labelProvider != null) {
			labelProvider.dispose();
			labelProvider = null;
		}
	}
}