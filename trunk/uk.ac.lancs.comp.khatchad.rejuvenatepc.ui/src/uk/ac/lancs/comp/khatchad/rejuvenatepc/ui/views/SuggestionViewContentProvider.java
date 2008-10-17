/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.RejuvenatePointcutPlugin;

/**
 * @author raffi
 * 
 */
public class SuggestionViewContentProvider implements
		IStructuredContentProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
	 * .lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		RejuvenatePointcutPlugin instance = RejuvenatePointcutPlugin.getInstance();
		if ( instance == null )
			return new Object[] {};
		return RejuvenatePointcutPlugin.getInstance().getSuggestionList()
				.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}
}