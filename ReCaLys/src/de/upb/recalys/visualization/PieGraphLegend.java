package de.upb.recalys.visualization;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.upb.recalys.helper.CSSColorExtractor;

/**
 * The Class PieGraphLegend is a JPanel that shows a legend for a Pie Graph.
 */
public class PieGraphLegend extends JPanel {

	private static final long serialVersionUID = 8429755420269128525L;
	private Color[] pieColors;
	private Color[] edgeColors;
	private final String[] pieItems = { "richtig gegangen", "falsch gegangen", "zurück gegangen",
			"zum richtigen Pfad gewechselt", "zum selben Knoten gegangen", "Knoten ausgewählt",
			"Aufgabe übersprungen" };;
	private final String[] edgeItems = { "Weg führt zum Ziel", "Weg führt nicht zum Ziel", "vom Nutzer gegangener Weg" };


	/**
	 * Create the panel.
	 */
	public PieGraphLegend() {
		this.setBackground(Color.white);
		pieColors = CSSColorExtractor.getPieChartFillColors("/stylesheets/PieGraph.css");
		edgeColors = CSSColorExtractor.getEdgeColors("/stylesheets/PieGraph.css");
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int row = 1;
		int column = 1;
		int xOffset = 250;
		int yOffset = 20;
		int xStart = 5;

		Font defaultFont = g.getFont();
		Font boldFont = new Font("bold", Font.BOLD, g.getFont().getSize());
		g.setFont(boldFont);
		g.drawString("Pie Chart:", xStart, 15);
		g.setFont(defaultFont);
		for (int i = 0; i < pieColors.length; i++) {
			g.setColor(pieColors[i]);
			g.fillOval(xStart + 5 + (column - 1) * xOffset, 5 + (row) * yOffset, 15, 15);
			g.setColor(Color.black);
			g.drawString(pieItems[i], 30 + (column - 1) * xOffset, 17 + (row) * yOffset);
			if (row % 4 == 0) {
				column++;
				row = 1;
			} else {
				row++;
			}
		}

		column++;
		row = 1;
		g.setFont(boldFont);
		g.drawString("Verbindungen:", (column - 1) * xOffset, 15);
		g.setFont(defaultFont);
		for (int i = 0; i < edgeColors.length; i++) {
			g.setColor(edgeColors[i]);
			g.fillRect(xStart + 5 + (column - 1) * xOffset, 10 + (row) * yOffset, 15, 4);
			g.setColor(Color.black);
			g.drawString(edgeItems[i], 30 + (column - 1) * xOffset, 17 + (row) * yOffset);
			row++;
		}
	}

}
