package de.upb.recalys.view.models;

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.upb.recalys.control.ReCaLys;
import de.upb.recalys.model.RCSTask;

/**
 * This class defines a TableModel that handles data about the tasks of of the
 * rcs-experiment
 * 
 * @author danielbrumberg
 * @version 1.0
 */
public class TaskTableModel implements TableModel {

	private ArrayList<RCSTask> experiment;
	private ReCaLys recalys;
	private RCSTask[] orderedTasks;

	/**
	 * Constructor: Creates a new DetailsTableModel
	 * 
	 * @param recalys
	 *            object of the main class of this object
	 */
	public TaskTableModel(ReCaLys recalys) {
		this.recalys = recalys;
		experiment = recalys.getExperiment();
		update();
	}

	/**
	 * This method updates the content of the table
	 */
	public void update() {
		experiment = recalys.getExperiment();
		if (experiment == null)
			return;
		Object[] tempArray = experiment.toArray();
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
		if (experiment == null)
			return 0;
		return experiment.size();
	}

	/**
	 * This method returns the column count of this table
	 * 
	 * @return column count
	 */
	public int getColumnCount() {
		return 2;
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
			return "Aufgabe";
		case 1:
			return "Lösungsquote";
		default:
			return "";
		}
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
		if (columnIndex == 0)
			return orderedTasks[rowIndex].getTarget().toString();
		else if (columnIndex == 1) {
			double d = orderedTasks[rowIndex].getSolutionRate() * 100;
			DecimalFormat df = new DecimalFormat("0.00");
			return df.format(d) + " Prozent";
		} else
			return "";
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
