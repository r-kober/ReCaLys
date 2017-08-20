package de.upb.recalys.view.models;

import java.text.DecimalFormat;
import java.util.LinkedList;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import de.upb.recalys.control.ReCaLys;
import de.upb.recalys.model.RCSNode;

/**
 * This class defines a TableModel that handles the coverage-data from an
 * experiment. So a JTable can show which pages of a website should be tested by
 * a new experiment to increase the experiments coverage by maximum.
 * 
 * @author danielbrumberg
 * @version 1.0
 */
public class CoverageTableModel implements TableModel {

	private LinkedList[] hotList;
	private ReCaLys recalys;
	private RCSNode[] hotNodes;
	private Double[] gains;

	/**
	 * Constructor: Creates a new CoverageTableModel
	 * 
	 * @param recalys
	 *            object of the main class of this app
	 */
	public CoverageTableModel(ReCaLys recalys) {
		this.recalys = recalys;
		hotList = recalys.getHotList();
		update();
	}

	/**
	 * This method updates the content of the table
	 */
	public void update() {
		hotList = recalys.getHotList();
		if (hotList == null)
			return;
		Object[] tempArray = hotList[0].toArray();
		hotNodes = new RCSNode[tempArray.length];
		for (int k = 0; k < tempArray.length; k++) {
			hotNodes[k] = (RCSNode) tempArray[k];
		}
		tempArray = hotList[1].toArray();
		gains = new Double[tempArray.length];
		for (int k = 0; k < tempArray.length; k++) {
			gains[k] = (Double) tempArray[k];
		}
	}

	/**
	 * This method returns the row count of the table
	 * 
	 * @return row count
	 */
	public int getRowCount() {
		if (hotList == null)
			return 0;
		return hotList[0].size();
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
			return "#";
		case 1:
			return "Hinzuzufügende Seite";
		case 2:
			return "Steigerung der Abdeckung auf";
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
			return Integer.class;
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
			return new Integer(rowIndex + 1);
		else if (columnIndex == 1)
			return hotNodes[rowIndex].getLabel();
		else if (columnIndex == 2) {
			double gain = gains[rowIndex] * 100.0;
			DecimalFormat df = new DecimalFormat("0.00");
			return df.format(gain) + " Prozent";
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
