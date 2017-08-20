package de.upb.recalys.view.models;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.upb.recalys.control.ReCaLys;
import de.upb.recalys.model.RCSNode;
import de.upb.recalys.model.RCSTask;
import de.upb.recalys.view.GUI;

/**
 * This class defines a TableModel that handles detailed data of the
 * rcs-experiment
 * 
 * @author danielbrumberg
 * @version 1.0
 */
public class DetailsTableModel implements TableModel {

	private LinkedList badList;
	private ArrayList<RCSTask> experiment;
	private ReCaLys recalys;
	private GUI gui;
	private RCSNode[] problems;
	private RCSTask[] orderedTasks;
	private String column1Name;

	/**
	 * Constructor: Creates a new DetailsTableModel
	 * 
	 * @param recalys
	 *            object of the main class of this object
	 * @param gui
	 *            the jFrame the jTable of this model belongs to
	 */
	public DetailsTableModel(ReCaLys recalys, GUI gui) {
		this.recalys = recalys;
		this.gui = gui;
		badList = recalys.getBadList();
		experiment = recalys.getExperiment();
		column1Name = "Aufgabe";
		update();
	}

	/**
	 * This method updates the content of the table
	 */
	public void update() {
		badList = recalys.getBadList();
		if (badList == null)
			return;
		Object[] tempArray = badList.toArray();
		problems = new RCSNode[tempArray.length];
		for (int k = 0; k < tempArray.length; k++) {
			problems[k] = (RCSNode) tempArray[k];
		}

		experiment = recalys.getExperiment();
		if (experiment == null)
			return;
		tempArray = experiment.toArray();
		orderedTasks = new RCSTask[tempArray.length];
		for (int k = 0; k < tempArray.length; k++) {
			orderedTasks[k] = (RCSTask) tempArray[k];
		}
		for (int j = 0; j < orderedTasks.length; j++)
			for (int i = 0; i < orderedTasks.length - 1; i++) {
				RCSTask current = orderedTasks[i];
				RCSTask next = orderedTasks[i + 1];
				if (current.getSolutionRate() > next.getSolutionRate()) {
					orderedTasks[i + 1] = current;
					orderedTasks[i] = next;
				}
			}
	}

	/**
	 * This method returns the row count of the table
	 * 
	 * @return row count
	 */
	public int getRowCount() {
		if (gui.getDetailsForProblem())
			if (experiment == null)
				return 0;
			else
				return experiment.size();
		else if (badList == null)
			return 0;
		else
			return badList.size();

	}

	/**
	 * This method returns the column count of this table
	 * 
	 * @return column count
	 */
	public int getColumnCount() {
		return 3;
	}

	/**
	 * This method returns the name of a specific column by its index
	 * 
	 * @param i
	 *            index of the column that name should be returned
	 * @return name of the specified column
	 */
	public String getColumnName(int i) {
		switch (i) {
		case 0:
			return column1Name;
		case 1:
			return "Problem-Verteilung";
		case 2:
			return "Latenz in ms";
		default:
			return "";
		}
	}

	/**
	 * This method changes the name of column 1
	 * 
	 * @param s
	 *            new name of the first column
	 */
	public void setColumn1Name(String s) {
		column1Name = s;
	}

	/**
	 * This method returns the class of the content of a specific column by its
	 * index
	 * 
	 * @param i
	 *            index of the column that content's class shall be returned
	 * @return class of the specified column content
	 */
	public Class<?> getColumnClass(int i) {
		switch (i) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		default:
			return String.class;
		}
	}

	/**
	 * Returns whether a specific cell is editable. Because the table is not
	 * editable it will always be false.
	 * 
	 * @param rowIndex
	 *            row of the cell
	 * @param columnIndex
	 *            column of the cell
	 * @return true if the cell is editable, else false
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * Get the value of a specific cell
	 * 
	 * @param rowIndex
	 *            row of a cell
	 * @param columnIndex
	 *            column of a cell
	 * @return value of the cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		int itemIndex = gui.getSelectedDetail();

		if (gui.getDetailsForProblem()) { // Details for problem
			int sumRatings = 0;
			int taskID = orderedTasks[rowIndex].getID();
			for (int i = 0; i < orderedTasks.length; i++)
				sumRatings += problems[itemIndex].getDistRatingForTask(i);
			if (columnIndex == 0)
				return orderedTasks[rowIndex].getTarget().toString();
			else if (columnIndex == 1) {
				for (int i = 0; i < orderedTasks.length; i++) {
					RCSTask temp = orderedTasks[i];
					if (temp.getID() == taskID)
						if (!temp.getNodesOnOptimalPaths().contains(problems[itemIndex]))
							return "-";
				}
				if (sumRatings == 0)
					return "kein Malus";
				double d = problems[itemIndex].getDistRatingForTask(taskID);
				d = d / (double) sumRatings * 100.0;
				DecimalFormat df = new DecimalFormat("0.00");
				// System.out.println(rowIndex + ": " + df.format(d) + " <> "+
				// d);
				// System.out.println(problems[itemIndex].toString());

				return df.format(d) + " Prozent";
			} else if (columnIndex == 2) {
				for (int i = 0; i < orderedTasks.length; i++) {
					RCSTask temp = orderedTasks[i];
					if (temp.getID() == taskID)
						if (!temp.getNodesOnOptimalPaths().contains(problems[itemIndex]))
							return "-";
				}
				return ((int) problems[itemIndex].getLatencyWeightForTask(taskID)) + "";
			} else
				return "";
		} else {// Details for task
			int sumRatings = 0;
			int taskID = orderedTasks[itemIndex].getID();
			for (int i = 0; i < problems.length; i++)
				sumRatings += problems[i].getDistRatingForTask(taskID);
			if (columnIndex == 0)
				return problems[rowIndex].toString();
			else if (columnIndex == 1) {
				for (int i = 0; i < orderedTasks.length; i++) {
					RCSTask temp = orderedTasks[i];
					if (temp.getID() == taskID)
						if (!temp.getNodesOnOptimalPaths().contains(problems[rowIndex]))
							return "-";
				}
				double d = (double) problems[rowIndex].getDistRatingForTask(taskID);

				d = sumRatings != 0 ? d / (double) sumRatings * 100.0 : 0.0;
				DecimalFormat df = new DecimalFormat("0.00");

				// System.out.println(rowIndex + ": " + df.format(d) + " <> "+
				// d);
				// System.out.println(orderedTasks[itemIndex].toString());

				return df.format(d) + " Prozent";
			} else if (columnIndex == 2) {
				for (int i = 0; i < orderedTasks.length; i++) {
					RCSTask temp = orderedTasks[i];
					if (temp.getID() == taskID)
						if (!temp.getNodesOnOptimalPaths().contains(problems[rowIndex]))
							return "-";
				}
				return ((int) problems[rowIndex].getLatencyWeightForTask(taskID)) + "";
			} else
				return "";
		}
	}

	/**
	 * Sets the value for a specific cell. Not implemented
	 * 
	 * @param aValue
	 *            new value of the cell
	 * @param rowIndex
	 *            row index of the cell
	 * @param columnIndex
	 *            column index of the cell
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		return;
	}

	/**
	 * Adds a TableModelListener to this TableModel. Not implemented.
	 * 
	 * @param tl
	 *            new TableModelListener
	 */
	public void addTableModelListener(TableModelListener tl) {
		return;
	}

	/**
	 * Removes a specific TableModelListener from this TableModel. Not implemented.
	 * 
	 * @param tl
	 *            TableModelListener that shall be removed
	 */
	public void removeTableModelListener(TableModelListener tl) {
		return;
	}
}
