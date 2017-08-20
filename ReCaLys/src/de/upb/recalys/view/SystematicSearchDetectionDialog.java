package de.upb.recalys.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import de.upb.recalys.control.ReCaLys;

/**
 * This class defines a JDialog that is used to change the option for the
 * detection of systematic searching
 * 
 * @author Roman Kober
 * @version 1.1
 */
@SuppressWarnings("serial")
public class SystematicSearchDetectionDialog extends javax.swing.JDialog {

	private ReCaLys recalys;

	/**
	 * Constructor: Creates new form SystematicSearchDetectionDialog.
	 *
	 * @param parent
	 *            the parent
	 * @param modal
	 *            the modal
	 * @param recalys
	 *            the recalys
	 */
	public SystematicSearchDetectionDialog(java.awt.Frame parent, boolean modal, ReCaLys recalys) {
		super(parent, modal);
		this.recalys = recalys;

		initComponents();
		updateValues();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {
		setMinimumSize(new Dimension(325, 185));
		setTitle("Systematisches Suchen");

		pnlSystematicSearch = new JPanel();
		pnlSystematicSearch.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(pnlSystematicSearch, BorderLayout.CENTER);
		GridBagLayout gbl_pnlSystematicSearch = new GridBagLayout();
		gbl_pnlSystematicSearch.columnWidths = new int[] { 0, 0, 0 };
		gbl_pnlSystematicSearch.rowHeights = new int[] { 35, 35, 35, 0 };
		gbl_pnlSystematicSearch.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_pnlSystematicSearch.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0 };
		pnlSystematicSearch.setLayout(gbl_pnlSystematicSearch);

		lblMaximaleFehlerrate = new JLabel("Maximale Fehlerrate:");
		GridBagConstraints gbc_lblMaximaleFehlerrate = new GridBagConstraints();
		gbc_lblMaximaleFehlerrate.anchor = GridBagConstraints.WEST;
		gbc_lblMaximaleFehlerrate.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaximaleFehlerrate.gridx = 0;
		gbc_lblMaximaleFehlerrate.gridy = 0;
		pnlSystematicSearch.add(lblMaximaleFehlerrate, gbc_lblMaximaleFehlerrate);

		txtMaxProblemRate = new JTextField();
		txtMaxProblemRate.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_txtMaxProblemRate = new GridBagConstraints();
		gbc_txtMaxProblemRate.insets = new Insets(0, 0, 5, 0);
		gbc_txtMaxProblemRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMaxProblemRate.gridx = 1;
		gbc_txtMaxProblemRate.gridy = 0;
		pnlSystematicSearch.add(txtMaxProblemRate, gbc_txtMaxProblemRate);
		txtMaxProblemRate.setColumns(10);

		lblMaximaleRcksprnge = new JLabel("Maximale Rücksprünge:");
		GridBagConstraints gbc_lblMaximaleRcksprnge = new GridBagConstraints();
		gbc_lblMaximaleRcksprnge.anchor = GridBagConstraints.WEST;
		gbc_lblMaximaleRcksprnge.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaximaleRcksprnge.gridx = 0;
		gbc_lblMaximaleRcksprnge.gridy = 1;
		pnlSystematicSearch.add(lblMaximaleRcksprnge, gbc_lblMaximaleRcksprnge);

		txtMaxReturnRate = new JTextField();
		txtMaxReturnRate.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_txtMaxReturnRate = new GridBagConstraints();
		gbc_txtMaxReturnRate.insets = new Insets(0, 0, 5, 0);
		gbc_txtMaxReturnRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMaxReturnRate.gridx = 1;
		gbc_txtMaxReturnRate.gridy = 1;
		pnlSystematicSearch.add(txtMaxReturnRate, gbc_txtMaxReturnRate);
		txtMaxReturnRate.setColumns(10);

		lblBedenkzeitinMs = new JLabel("Bedenkzeit (in ms):");
		GridBagConstraints gbc_lblBedenkzeitinMs = new GridBagConstraints();
		gbc_lblBedenkzeitinMs.anchor = GridBagConstraints.WEST;
		gbc_lblBedenkzeitinMs.insets = new Insets(0, 0, 5, 5);
		gbc_lblBedenkzeitinMs.gridx = 0;
		gbc_lblBedenkzeitinMs.gridy = 2;
		pnlSystematicSearch.add(lblBedenkzeitinMs, gbc_lblBedenkzeitinMs);

		txtTimeToThink = new JTextField();
		txtTimeToThink.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_txtTimeToThink = new GridBagConstraints();
		gbc_txtTimeToThink.insets = new Insets(0, 0, 5, 0);
		gbc_txtTimeToThink.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTimeToThink.gridx = 1;
		gbc_txtTimeToThink.gridy = 2;
		pnlSystematicSearch.add(txtTimeToThink, gbc_txtTimeToThink);
		txtTimeToThink.setColumns(10);

		btnCancel = new JButton("Abbrechen");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BtnCancelActionPerformed(e);
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 3;
		pnlSystematicSearch.add(btnCancel, gbc_btnCancel);

		btnAdopt = new JButton("Übernehmen");
		btnAdopt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BtnAdoptActionPerformed(e);
			}
		});
		GridBagConstraints gbc_btnAdopt = new GridBagConstraints();
		gbc_btnAdopt.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnAdopt.gridx = 1;
		gbc_btnAdopt.gridy = 3;
		pnlSystematicSearch.add(btnAdopt, gbc_btnAdopt);
	}

	/**
	 * Updates the values in the dialogs JTextFields
	 */
	public void updateValues() {
		txtTimeToThink.setText(recalys.getTimeToThink() + "");
		txtMaxProblemRate.setText(recalys.getMaxProblemRate() + "");
		txtMaxReturnRate.setText(recalys.getMaxReturnRate() + "");
	}

	/**
	 * This method will be triggered if the user click on the cancelButton. Changes
	 * on the option for systematic search detection will be cancelled.
	 * 
	 * @param e
	 *            event
	 */
	private void BtnCancelActionPerformed(ActionEvent e) {
		setVisible(false);
	}

	/**
	 * This method will be triggered if the user click on the adoptButton. Changes
	 * on the option for systematic search detection will be adopted.
	 * 
	 * @param e
	 *            event
	 */
	private void BtnAdoptActionPerformed(ActionEvent e) {
		boolean valuesOkay = true;
		double maxProblemRate = 0.0;
		double maxReturnRate = 0.0;
		int timeToThink = 0;

		try {
			maxProblemRate = Double.parseDouble(txtMaxProblemRate.getText());
		} catch (NumberFormatException ex) {
			txtMaxProblemRate.setText("Ungültiger Wert");
			valuesOkay = false;
		}

		try {
			maxReturnRate = Double.parseDouble(txtMaxReturnRate.getText());
		} catch (NumberFormatException ex) {
			txtMaxReturnRate.setText("Ungültiger Wert");
			valuesOkay = false;
		}

		try {
			timeToThink = Integer.parseInt(txtTimeToThink.getText());
		} catch (NumberFormatException ex) {
			txtTimeToThink.setText("Ungültiger Wert");
			valuesOkay = false;
		}

		if (valuesOkay) {
			recalys.setMaxProblemRate(maxProblemRate);
			recalys.setMaxReturnRate(maxReturnRate);
			recalys.setTimeToThink(timeToThink);
			System.out.println("timetothink: " + recalys.getTimeToThink());
			setVisible(false);
		}
	}

	/*
	 * Generated Components for the GUI
	 */
	private JPanel pnlSystematicSearch;
	private JLabel lblMaximaleFehlerrate;
	private JLabel lblMaximaleRcksprnge;
	private JLabel lblBedenkzeitinMs;
	private JTextField txtMaxProblemRate;
	private JTextField txtMaxReturnRate;
	private JTextField txtTimeToThink;
	private JButton btnCancel;
	private JButton btnAdopt;

}
