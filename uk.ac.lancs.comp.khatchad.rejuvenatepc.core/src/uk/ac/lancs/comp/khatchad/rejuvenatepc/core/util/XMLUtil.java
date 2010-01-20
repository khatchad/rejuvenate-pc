/**
 * 
 */
package uk.ac.lancs.comp.khatchad.rejuvenatepc.core.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IJavaElement;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * @author raffi
 *
 */
public class XMLUtil {
	private XMLUtil() {}

	/**
	 * @param advElem
	 * @return
	 */
	@SuppressWarnings("restriction")
	public static File getSavedXMLFile(AdviceElement advElem) {
		String relativeFileName = XMLUtil.getRelativeXMLFileName(advElem);
		File aFile = new File(FileUtil.WORKSPACE_LOC, relativeFileName);
		if (!aFile.exists())
			throw new IllegalArgumentException("No XML file found for advice "
					+ advElem.getElementName());
		return aFile;
	}

	/**
	 * @param advElem
	 * @return
	 */
	public static String getRelativeXMLFileName(AdviceElement advElem) {
		StringBuilder fileNameBuilder = new StringBuilder(advElem.getPath()
				.toOSString());
		fileNameBuilder.append("#" + advElem.toDebugString());
		fileNameBuilder.append(".rejuv-pc.xml");
		return fileNameBuilder.toString();
	}

	@SuppressWarnings("restriction")
	public static PrintWriter getXMLFileWriter(AdviceElement advElem)
			throws IOException {
		String fileName = getRelativeXMLFileName(advElem);
		final File aFile = new File(FileUtil.WORKSPACE_LOC, fileName);
		PrintWriter ret = FileUtil.getPrintWriter(aFile, false);
		return ret;
	}

	/**
	 * @param elem
	 * @return
	 */
	public static Element getXML(IJavaElement elem) {
		Element ret = new Element(elem.getClass().getSimpleName());
		String handleIdentifier = null;
		try {
			handleIdentifier = elem.getHandleIdentifier();
		}
		catch(NullPointerException e) {
			System.err.println("Can't retrieve element handler for: " + elem);
			System.exit(-1);
		}
		ret.setAttribute(new Attribute("id", handleIdentifier));
		ret.setAttribute(new Attribute("name", elem.getElementName()));
		ret.setAttribute(new Attribute("type", String.valueOf(elem
				.getElementType())));
		return ret;
	}
}
