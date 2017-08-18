package de.upb.recalys.visualization;

import org.graphstream.graph.Element;

/**
 * This class implements static methods for small tasks on Graphstream graphs.
 * 
 * @author Roman Kober
 *
 */
public class GraphTools {

	/**
	 * Removes a specific ui.class from a graph element.
	 * 
	 * @param element
	 *            The element on which the class should be removed
	 * @param classToRemove
	 *            The class to remove from the element
	 * @return true, if the element has the class to remove and the
	 *         classToRemove does not contain a "," <br>
	 *         false otherwise
	 */
	public static boolean removeUIClass(Element element, String classToRemove) {
		String currentUIClass = element.getAttribute("ui.class");
		if (currentUIClass != null && currentUIClass.contains(classToRemove) && !classToRemove.contains(",")) {
			String[] classes = currentUIClass.split(",");
			for (int i = 0; i < classes.length; i++) {
				classes[i] = classes[i].trim();
				if (classes[i].equals(classToRemove)) {
					classes[i] = "";
				}
			}
			StringBuffer newUIClass = new StringBuffer();
			for (String uiClass : classes) {
				if (!uiClass.isEmpty()) {
					newUIClass.append(uiClass);
					newUIClass.append(",");
				}
			}
			int lastComma = newUIClass.lastIndexOf(",");
			if (lastComma >= 0) {
				newUIClass.deleteCharAt(lastComma);
			}
			currentUIClass = newUIClass.toString().trim();
			element.setAttribute("ui.class", currentUIClass);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Adds a specific ui.class to a graph element.
	 * 
	 * @param element
	 *            The element on which the class should be added
	 * @param classToRemove
	 *            The class to add to the element
	 * @return
	 */
	public static void addUIClass(Element element, String classToAdd) {
		String currentUIClass = element.getAttribute("ui.class");
		if (currentUIClass != null) {
			for (String uiClass : currentUIClass.split(",")) {
				if (uiClass.trim().equals(classToAdd)) {
					return;
				}
			}
			currentUIClass += ", " + classToAdd;
		} else {
			currentUIClass = classToAdd;
		}
		element.setAttribute("ui.class", currentUIClass);
	}

}
