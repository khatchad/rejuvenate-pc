package uk.ac.lancs.comp.khatchad.rejuvenatepc.ui;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

import uk.ac.lancs.comp.khatchad.rejuvenatepc.ui.views.SuggestionView;

/**
 * The activator class controls the plug-in life cycle
 */
public class SuggestionUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "uk.ac.lancs.comp.khatchad.rejuvenatepc.ui";

	// The shared instance
	private static SuggestionUIPlugin plugin;
	
	private static SuggestionView suggestionView;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SuggestionUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static SuggestionView getSuggestionView() {
		return suggestionView;
	}

	public static void setSuggestionView(SuggestionView suggestionView) {
		SuggestionUIPlugin.suggestionView = suggestionView;
	}
	
	/**
	 * Refresh the Suggestions view.
	 */
	/*
	public static void refresh() {
		if (suggestionView != null) {
		 	SuggestionViewUpdateJob.getInstance().schedule();
		}
	}
	*/
	
	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
}

//UIJob that updates the XReference View
//class SuggestionViewUpdateJob extends UIJob {
//		 private static SuggestionViewUpdateJob theJob;
//		 
//		 private SuggestionViewUpdateJob(String name){
//		 		 super (name);
//		 }
//		 
//		 public static SuggestionViewUpdateJob getInstance() {
//	 		 if(theJob == null) {
// 		 		 theJob = new SuggestionViewUpdateJob(SuggestionMessages.SuggestionUIPlugin_Jobs_SuggestionViewUpdate);
// 		 		 theJob.setSystem(true);
// 		 		 theJob.setPriority(Job.SHORT);
//	 		 }
//	 		 return theJob;
//		 }
//		 
//		/* (non-Javadoc)
//		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
//		 */
//		 public IStatus runInUIThread(IProgressMonitor monitor) {
//				monitor.beginTask(SuggestionMessages.SuggestionUIPlugin_Jobs_Update, 1);
//		 		if (SuggestionUIPlugin.getSuggestionView() !=null) {
//		 			IWorkbenchPart workbenchPart = null;
//		 			if (SuggestionUIUtils.getActiveWorkbenchWindow() != null) {
//		 				workbenchPart = SuggestionUIUtils.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
//		 			}
//		 			// fix for bug 107719 and 107589 and also required for 
//		 			// enhancement 95724 when refreshing the xref views.
//		 			ISelection selection = null;
//		 			if (workbenchPart != null && workbenchPart.equals(SuggestionUIPlugin.getSuggestionView().getLastSelectedWorkbenchPart())) {
//		 				// if the active workbench part is the same as the last active
//		 				// workbench part recorded in the XReferenceView, then the
//		 				// selection needs to be the corresponding last selection
//		 				// recorded in the XReferenceView.
//		 				selection = XReferenceUIPlugin.xrefView.getLastSelection();
//					} else {
//						selection = XRefUIUtils.getCurrentSelection();
//					}
//		 			
//		 		 	XReferenceUIPlugin.xrefView.setChangeDrivenByBuild(true);
//		 		 	XReferenceUIPlugin.xrefView.selectionChanged(workbenchPart,selection);		
//
//					
//		 		 	XReferenceUIPlugin.xrefView.setChangeDrivenByBuild(false);
//		 		}
//		 		monitor.done();
//		 		return Status.OK_STATUS;
//			}
//		 
//}