package de.upb.recalys.helper;

import java.awt.Color;

/**
 * The Class CSSColorExtractor provides static methods to extract specific
 * colors from a css file.
 * 
 * @author Roman Kober
 */
public class CSSColorExtractor {

	/**
	 * Gets the fill-colors from a CSS File that defines a node element.
	 *
	 * @param path
	 *            the path to a CSS File in the BuildPath
	 * @return the fill-colors of the node element
	 */
	public static Color[] getPieChartFillColors(String path) {
		Color[] fillColors;

		StringBuffer css = new StringBuffer(ResourceHandler.getDataAsString(path));
		css.delete(0, css.indexOf("node {") + 7);
		css.delete(css.indexOf("}"), css.length());
		css.delete(0, css.indexOf("fill-color"));
		css.delete(css.indexOf(";"), css.length());

		css.delete(0, css.indexOf(":") + 1);
		String colorsString = css.toString();
		colorsString = colorsString.trim();
		String[] colors = colorsString.split(",");

		fillColors = new Color[colors.length];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = colors[i].trim();
			if (colors[i].startsWith("#")) {
				fillColors[i] = Color.decode(colors[i]);
			} else {
				try {
					fillColors[i] = (Color) Color.class.getField(colors[i]).get(null);
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
						| SecurityException e) {
					e.printStackTrace();
				}
			}
		}
		return fillColors;
	}

	/**
	 * Gets the fill-colors from the edges in a CSS-File that defines an edge
	 * element in a Pie Graph.
	 *
	 * @param path
	 *            the path to a CSS File in the BuildPath.
	 * @return the fill-colors of the edge element. The first element is the
	 *         fill-color for a Simple Path edge and the second is the
	 *         fill-color for a normal edge.
	 */
	public static Color[] getEdgeColors(String path) {
		Color[] fillColors = new Color[3];

		boolean isUserPath = false;

		StringBuffer css = new StringBuffer(ResourceHandler.getDataAsString(path));
		while (css.indexOf("edge") != -1) {
			isUserPath = false;
			css.delete(0, css.indexOf("edge") + 4);
			int index = -1;
			if (css.indexOf(" {") == 0) {
				// normal edge
				index = 1;
			} else if (css.indexOf(".simplePath") == 0) {
				// Simple Path edge
				index = 0;
			} else if (css.indexOf(".userPath") == 0) {
				index = 2;
				isUserPath = true;
			}
			if (css.indexOf("fill-color") == -1 && !isUserPath || css.indexOf("shadow-color") == -1 && isUserPath) {
				continue;
			}

			if (isUserPath) {
				css.delete(0, css.indexOf("shadow-color"));
			} else {
				css.delete(0, css.indexOf("fill-color"));
			}

			css.delete(0, css.indexOf(":") + 1);

			String fillcolor = css.substring(0, css.indexOf(";"));
			fillcolor = fillcolor.trim();

			if (index != -1) {
				if (fillcolor.startsWith("#")) {
					fillColors[index] = Color.decode(fillcolor);
				} else {
					try {
						fillColors[index] = (Color) Color.class.getField(fillcolor).get(null);
					} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
							| SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return fillColors;
	}
}
