package de.upb.recalys.view.models;

import java.util.LinkedList;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.upb.recalys.control.ReCaLys;
import de.upb.recalys.model.RCSNode;

/**
 * This class defines a TableModel that handles problem data of the
 * rcs-experiment
 * 
 * @author danielbrumberg
 */
public class ProblemTableModel implements TableModel {

	private LinkedList<RCSNode> badList;
	private ReCaLys recalys;
	private RCSNode[] problems;

	/**
	 * Constructor: Creates a new DetailsTableModel
	 * 
	 * @param recalys
	 *            object of the main class of this object
	 */
	public ProblemTableModel(ReCaLys recalys) {
		this.recalys = recalys;
		badList = recalys.getBadList();
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
	}

	/**
	 * This method returns the row count of the table
	 * 
	 * @return row count
	 */
	public int getRowCount() {
		if (badList == null)
			return 0;
		return badList.size();
	}

	/**
	 * This method returns the column count of this table
	 * 
	 * @return column count
	 */
	public int getColumnCount() {
		return 4;
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
			return "Seite";
		case 1:
			return "Gewichteter Malus";
		case 2:
			return "Ungewichteter Malus";
		case 3:
			return "Latenz in ms";
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
			return Double.class;
		case 2:
			return Double.class;
		case 3:
			return Double.class;
		default:
			return String.class;
		}
	}

	/**
	 * Returns whether a specific cell is editable. Because the table is not
	 * editable it will always be false
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
			return problems[rowIndex].getLabel();
		else if (columnIndex == 1)
			return new Double(problems[rowIndex].getMalus());
		else if (columnIndex == 2)
			return new Double(problems[rowIndex].getDistRating());
		else if (columnIndex == 3)
			return new Double(problems[rowIndex].getLatencyWeight());
		else
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
