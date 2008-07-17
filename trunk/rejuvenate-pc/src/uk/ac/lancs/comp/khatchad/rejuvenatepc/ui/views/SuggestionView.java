package uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.views;

import java.util.Arrays;
import java.util.Collections;

import javax.security.auth.Refreshable;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import ca.mcgill.cs.swevo.jayfx.model.IElement;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.RejuvenatePointcutPlugin;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.graph.IntentionElement;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.core.model.Suggestion;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.views.SuggestionViewSorter.SortBy;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class SuggestionView extends ViewPart {
	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	// Set the table column property names
	private final String SUGGESTION_COLUMN = "suggestion";
	private final String PATTERN_COLUMN = "pattern";
	private final String CONFIDENCE_COLUMN = "confidence";

	// Set column names
	private final String[] COLUMN_NAMES = new String[] { SUGGESTION_COLUMN,
			PATTERN_COLUMN, CONFIDENCE_COLUMN };

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		@SuppressWarnings("unchecked")
		public String getColumnText(Object obj, int index) {
			if (!(obj instanceof Suggestion))
				throw new IllegalArgumentException("Expecting Suggestion, not "
						+ obj.getClass().getSimpleName());
			Suggestion<IJavaElement> suggestion = (Suggestion<IJavaElement>) obj;
			switch (index) {
				case 0:
					return suggestion.getSuggestion().getElementName();
				case 1:
					return suggestion.getPattern().toString();
				case 2:
					return String.valueOf(suggestion.getConfidence());
				default:
					throw new IllegalArgumentException(
							"Invalid column number: " + index);
			}
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	/**
	 * The constructor.
	 */
	public SuggestionView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		int tableStyle = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		Table table = new Table(parent, tableStyle);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn suggestionColumn = new TableColumn(table, SWT.LEFT, 0);
		suggestionColumn.setText("Suggestion");
		suggestionColumn.setWidth(200);

		// Add listener to column so tasks are sorted by suggestion when clicked 
		suggestionColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				viewer.setSorter(new SuggestionViewSorter(SortBy.SUGGESTIONS));
			}
		});

		TableColumn patternColumn = new TableColumn(table, SWT.LEFT, 1);
		patternColumn.setText("Pattern");
		patternColumn.setWidth(400);
		suggestionColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				viewer.setSorter(new SuggestionViewSorter(SortBy.PATTERNS));
			}
		});

		TableColumn confidenceColumn = new TableColumn(table, SWT.LEFT, 2);
		confidenceColumn.setText("Confidence");
		confidenceColumn.setWidth(300);
		suggestionColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				viewer.setSorter(new SuggestionViewSorter(SortBy.CONFIDENCE));
			}
		});

		this.viewer = new TableViewer(table);
		this.viewer.setUseHashlookup(true);
		this.viewer.setColumnProperties(COLUMN_NAMES);
		//		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
		//				| SWT.V_SCROLL);

		IContentProvider provider = RejuvenatePointcutPlugin.getInstance();
		if (provider != null)
			viewer.setContentProvider(provider);
		else
			viewer.setContentProvider(new IStructuredContentProvider() {

				public Object[] getElements(Object inputElement) {
					return new Object[0];
				}

				public void dispose() {
				}

				public void inputChanged(Viewer viewer, Object oldInput,
						Object newInput) {
				}
			});

		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		viewer.setSorter(new SuggestionViewSorter(SortBy.CONFIDENCE));

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SuggestionView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
//				showMessage("Action 1 executed");
				SuggestionView.this.viewer.refresh();
			}
		};
//		action1.setText("Action 1");
		action1.setText("Refresh");
//		action1.setToolTipText("Action 1 tooltip");
		action1.setToolTipText("Refresh the view");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Suggestion View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}