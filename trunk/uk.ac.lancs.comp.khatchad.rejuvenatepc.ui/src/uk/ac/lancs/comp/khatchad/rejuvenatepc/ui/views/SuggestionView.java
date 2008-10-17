package uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.*;
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

import uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.SuggestionUIPlugin;
import uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.views.SuggestionViewSorter.SortBy;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class SuggestionView extends ViewPart {
	
	public static final String ID = "uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.views.SuggestionView";
	
	// Set the table column property names
	private final String SUGGESTION_COLUMN = "suggestion";
	private final String PATTERN_COLUMN = "pattern";
	private final String CONFIDENCE_COLUMN = "confidence";
	
	// Set column names
	private final String[] COLUMN_NAMES = new String[] { SUGGESTION_COLUMN,
			PATTERN_COLUMN, CONFIDENCE_COLUMN };
	
	private Action doubleClickAction;
	
	private TableViewer viewer;
	private SuggestionViewContentProvider contentProvider;

	/**
	 * The constructor.
	 */
	public SuggestionView() {
		SuggestionUIPlugin.setSuggestionView(this);
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		Table table = getTable(parent);
		this.viewer = new TableViewer(table);
		this.viewer.setUseHashlookup(true);
		this.viewer.setColumnProperties(COLUMN_NAMES);
		
		this.contentProvider = new SuggestionViewContentProvider();
		viewer.setContentProvider(this.contentProvider);
		viewer.setLabelProvider(new SuggestionViewLabelProvider());
		viewer.setSorter(new SuggestionViewSorter(SortBy.CONFIDENCE));
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.viewer");
		makeActions();
//		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
//		IWorkbenchWindow window = SuggestionUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
//		
//		if (window != null) {
//			window.getSelectionService().addPostSelectionListener(this);
//		}
//		getSite().setSelectionProvider(viewer);
//		
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
	}

	private void fillContextMenu(IMenuManager manager) {
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
	}

	private void makeActions() {
		doubleClickAction = new DoubleClickAction(getViewSite().getShell(),
				viewer);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Suggestion View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	private Table getTable(Composite parent) {
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
		return table;
	}
}