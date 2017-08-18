package de.upb.recalys.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import de.upb.recalys.control.ReCaLys;
import de.upb.recalys.helper.FileChecker;
import de.upb.recalys.model.RCSNode;
import de.upb.recalys.model.RCSPath;
import de.upb.recalys.model.RCSTask;
import de.upb.recalys.view.models.CoverageTableModel;
import de.upb.recalys.view.models.DetailsTableModel;
import de.upb.recalys.view.models.ProblemTableModel;
import de.upb.recalys.view.models.TaskTableModel;
import de.upb.recalys.visualization.IAGraph;
import de.upb.recalys.visualization.PieGraph;
import de.upb.recalys.visualization.PieGraphLegend;
import de.upb.recalys.visualization.view.RCSGraphMouseManager;
import de.upb.recalys.visualization.view.RCSGraphShortcutManager;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.FlowLayout;
import java.awt.Color;
import javax.swing.UIManager;
import java.awt.Font;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

/**
 * This class defines the gui of this app.
 *
 * @author Roman Kober
 * @version 1.1
 */
@SuppressWarnings("serial")
public class GUI extends javax.swing.JFrame {

	private ReCaLys recalys;
	private IAGraph iaGraph;
	private Viewer iaViewer;
	private PieGraph pieGraph;
	private Viewer pieViewer;
	private PieGraphLegend pieGraphLegend;

	private SystematicSearchDetectionDialog ssdd;
	private TaskTableModel ttm;
	private ProblemTableModel ptm;
	private CoverageTableModel ctm;
	private DetailsTableModel dtm;
	private boolean detailsForProblem = true;
	private boolean updateInProgress = false;
	private boolean showLegend = false;
	private boolean newTaskSelected = false;

	/**
	 * Constructor: Creates new form GUI.
	 *
	 * @param recalys
	 *            object of the main class of this app
	 */
	public GUI(ReCaLys recalys) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		setMinimumSize(new Dimension(800, 700));
		setSize(new Dimension(425, 236));
		this.recalys = recalys;

		this.setGraphs();

		setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		initComponents();
		ssdd = new SystematicSearchDetectionDialog(this, true, recalys);
		ssdd.setVisible(false);
		setModels();
	}

	/**
	 * initializes the IA- and Pie-graph and assigns them to a {@link Viewer}.
	 */
	private void setGraphs() {
		iaGraph = new IAGraph();
		iaViewer = new Viewer(iaGraph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		iaViewer.enableAutoLayout();
		pieGraph = new PieGraph();
		pieViewer = new Viewer(pieGraph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		pieViewer.enableAutoLayout();
	}

	/**
	 * Sets the models for all JTables in the GUI
	 */
	private void setModels() {
		ttm = new TaskTableModel(recalys);
		tblTaskSurvey.setModel(ttm);
		ptm = new ProblemTableModel(recalys);
		tblProblems.setModel(ptm);
		ctm = new CoverageTableModel(recalys);
		tblCoverage.setModel(ctm);
		dtm = new DetailsTableModel(recalys, this);
		tblDetails.setModel(dtm);
		tblDetails.setAutoCreateColumnsFromModel(true);
	}

	/**
	 * This method updates the gui when the experiments data has changed.
	 */
	public void updateGUI() {

		updateInProgress = true;
		int currentSelectedIndexPieGraphTask = comboBoxTaskForPieGraph.getSelectedIndex();
		if (recalys.getExperiment() == null) {
			updateInProgress = false;
			return;
		}

		/* Overview-Tab */
		lblUserCountIndicator.setText(recalys.getUserCount() + "");
		barTotalCoverageOverview.setValue((int) (recalys.getCoverage() * 100));
		barTotalSolutionRate.setValue((int) (recalys.getTotalSolutionRate() * 100));
		ttm.update();
		tblTaskSurvey.updateUI();

		/* Problem-Tab */
		ptm.update();
		tblProblems.updateUI();

		/* Details-Tab & Pie Graph */
		comboBoxProblem.removeAllItems();
		comboBoxTask.removeAllItems();
		comboBoxTaskForPieGraph.removeAllItems();
		ArrayList<RCSTask> experiment = recalys.getExperiment();
		RCSTask[] orderedTasks;
		if (experiment == null) {
			updateInProgress = false;
			return;
		}
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
		for (int i = 0; i < orderedTasks.length; i++) {
			comboBoxTask.addItem(orderedTasks[i].getTarget().toString());
			comboBoxTaskForPieGraph.addItem(orderedTasks[i]);
			comboBoxTaskForPieGraph.setSelectedIndex(currentSelectedIndexPieGraphTask);
		}
		while (recalys.analysisComplete() && recalys.getBadList() == null) {
			System.out.println("no badlist");
		}
		if (recalys.getBadList() == null) {
			updateInProgress = false;
			return;
		}

		LinkedList<RCSNode> badList = recalys.getBadList();
		tempArray = badList.toArray();
		for (int k = 0; k < tempArray.length; k++) {
			comboBoxProblem.addItem(tempArray[k].toString());
		}

		if (detailsForProblem) {
			dtm.setColumn1Name("Aufgabe");
			lblDetailsFor.setText("Details zu Problem-Seite \"" + badList.get(getSelectedDetail()) + "\"");
		} else {
			dtm.setColumn1Name("Problem-Seite");
			double rating = orderedTasks[getSelectedDetail()].getSolutionRate() * 100.0;
			DecimalFormat df = new DecimalFormat("0.00");
			lblDetailsFor.setText("Details zu Aufgabe \"" + orderedTasks[getSelectedDetail()].getTarget().toString()
					+ "\" (Lösungsquote: " + df.format(rating) + " Prozent" + ")");
		}
		dtm.update();
		tblDetails.updateUI();

		/* Coverage-Tab */
		barTotalCoverage.setValue((int) (recalys.getCoverage() * 100));
		ctm.update();
		tblCoverage.updateUI();

		/* Pie-Graph Tab */
		try {
			if (comboBoxTaskForPieGraph.getItemCount() > 0 && currentSelectedIndexPieGraphTask == -1) {
				updateInProgress = false;
				comboBoxTaskForPieGraph.setSelectedIndex(0);
			} else {
				comboBoxTaskForPieGraph.setSelectedIndex(currentSelectedIndexPieGraphTask);
			}
		} catch (IllegalArgumentException e) {
			comboBoxTaskForPieGraph.setSelectedIndex(-1);
		}

		repaint();
		updateInProgress = false;
	}

	/**
	 * This method updates the pnlDetails to the details the user has chosen to
	 * see.
	 */
	public void updateDetailsPanel() {
		if (recalys.getExperiment() == null)
			return;

		ArrayList<RCSTask> experiment = recalys.getExperiment();
		RCSTask[] orderedTasks;
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

		LinkedList<RCSNode> badList = recalys.getBadList();

		if (badList != null) {
			if (detailsForProblem) {
				dtm.setColumn1Name("Aufgabe");
				lblDetailsFor.setText("Details zu Problem-Seite \"" + badList.get(getSelectedDetail()) + "\"");
			} else {
				dtm.setColumn1Name("Problem-Seite");
				double rating = orderedTasks[getSelectedDetail()].getSolutionRate() * 100.0;
				DecimalFormat df = new DecimalFormat("0.00");
				lblDetailsFor.setText("Details zu Aufgabe \"" + orderedTasks[getSelectedDetail()].getTarget().toString()
						+ "\" (Lösungsquote: " + df.format(rating) + " Prozent" + ")");
			}
		} else
			lblDetailsFor.setText("Details zu ...");

		dtm.update();
		tblDetails.createDefaultColumnsFromModel();
		tblDetails.updateUI();

		repaint();
	}

	/**
	 * This method sets menuAnalyse en- or disabled.
	 *
	 * @param b
	 *            true, if menu shall be enabled, else false
	 */
	public void setAnalyseMenuEnabled(boolean b) {
		menuAnalyse.setEnabled(b);
	}

	/**
	 * This method sets menuExport en- or disabled.
	 *
	 * @param b
	 *            true, if menu shall be enabled, else false
	 */
	public void setExportMenuEnabled(boolean b) {
		menuExport.setEnabled(b);
	}

	/**
	 * This method sets menuGraph en- or disabled.
	 *
	 * @param b
	 *            true, if menu shall be enabled, else false
	 */
	public void setGraphMenuEnabled(boolean b) {
		menuIAGraph.setEnabled(b);
	}

	/**
	 * This method sets miSavePieGraphAsPicture en- or disabled.
	 *
	 * @param b
	 *            true, if menu shall be enabled, else false
	 */
	public void setPieGraphMenuItemsEnabled(boolean b) {
		menuPieGraph.setEnabled(true);
		chckbxmntmShowCoverage.setEnabled(true);
		miSavePieGraphAsPicture.setEnabled(b);
		chckbxmntmShowLegend.setEnabled(b);
	}
	
	/**
	 * Sets the SSD log.
	 *
	 * @param log the new SSD log
	 */
	public void setSSDLog(String log){
		txtrSSDLog.setText(log);
	}

	/**
	 * Returns true, if details-for-problem is selected returns false, if
	 * details-for-task is selected.
	 *
	 * @return the details for problem
	 */
	public boolean getDetailsForProblem() {
		return detailsForProblem;
	}

	/**
	 * Gets the index of the currently selected Element on pnlDetails (either
	 * problem or task) to be shown in detail in tblDetails.
	 *
	 * @return index of the selectedElement
	 */
	public int getSelectedDetail() {
		int index;
		if (detailsForProblem) {
			// System.out.println(comboBoxProblem.getSelectedItem()+" >
			// "+comboBoxProblem.getSelectedIndex());
			index = comboBoxProblem.getSelectedIndex();
		}

		else
			index = comboBoxTask.getSelectedIndex();

		if (index < 0)
			index = 0;
		return index;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		menuImport = new JMenu("Datei");
		menuBar.add(menuImport);

		lblRecapo = new JLabel("ReCaPo");
		menuImport.add(lblRecapo);

		miImportRecapoIA = new JMenuItem("1. erweiterte IA importieren");
		miImportRecapoIA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miImportRecapoIAActionPerformed(e);
			}
		});
		menuImport.add(miImportRecapoIA);

		miImportResults = new JMenuItem("2. Gesamtresultate importieren");
		miImportResults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miImportResultsActionPerformed(e);
			}
		});
		miImportResults.setEnabled(false);
		menuImport.add(miImportResults);

		separatorImport2 = new JSeparator();
		menuImport.add(separatorImport2);

		miImportExperiment = new JMenuItem("Auswertung importieren");
		miImportExperiment.setVisible(false);
		miImportExperiment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miImportExperimentActionPerformed(e);
			}
		});
		
		miSaveSSDlog = new JMenuItem("SSD-Log speichern");
		miSaveSSDlog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miSaveSSDlogActionPerformed(e);
			}
		});
		miSaveSSDlog.setEnabled(false);
		menuImport.add(miSaveSSDlog);
		menuImport.add(miImportExperiment);

		menuAnalyse = new JMenu("Auswerten");
		menuAnalyse.setEnabled(false);
		menuBar.add(menuAnalyse);

		miSsd = new JMenuItem("Optionen zum Aufdecken von systematischem Suchen");
		miSsd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miSsdActionPerformed(e);
			}
		});
		menuAnalyse.add(miSsd);

		miAnalyse = new JMenuItem("Auswertung durchführen");
		miAnalyse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miAnalyseActionPerformed(e);
			}
		});
		menuAnalyse.add(miAnalyse);

		menuExport = new JMenu("Sichern");
		menuExport.setVisible(false);
		menuExport.setEnabled(false);
		menuBar.add(menuExport);

		miExport = new JMenuItem("Auswertung sichern");
		miExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miExportActionPerformed(e);
			}
		});
		menuExport.add(miExport);

		menuIAGraph = new JMenu("Strukturgraph");
		menuIAGraph.setEnabled(false);
		menuBar.add(menuIAGraph);

		chckbxmntmShowCoverage = new JCheckBoxMenuItem("Abdeckung anzeigen");
		chckbxmntmShowCoverage.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		chckbxmntmShowCoverage.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				chckbxmntmShowCoverageStateChanged(e);
			}
		});
		chckbxmntmShowCoverage.setEnabled(false);
		menuIAGraph.add(chckbxmntmShowCoverage);

		miResetIAView = new JMenuItem("Anzeige zurücksetzen");
		miResetIAView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miResetIAViewActionPerformed(e);
			}
		});
		menuIAGraph.add(miResetIAView);

		separator_1 = new JSeparator();
		menuIAGraph.add(separator_1);

		miSaveIAGraphAsPicture = new JMenuItem("Strukturgraph als Bild speichern");
		menuIAGraph.add(miSaveIAGraphAsPicture);
		miSaveIAGraphAsPicture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miSaveIAGraphAsPictureActionPerformed(e);
			}
		});

		menuPieGraph = new JMenu("Pie-Graph");
		menuPieGraph.setEnabled(false);
		menuBar.add(menuPieGraph);

		chckbxmntmShowLegend = new JCheckBoxMenuItem("Legende anzeigen");
		chckbxmntmShowLegend.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		chckbxmntmShowLegend.setEnabled(false);
		chckbxmntmShowLegend.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				chckbxmntmShowLegendItemStateChanged(e);
			}
		});
		menuPieGraph.add(chckbxmntmShowLegend);

		chckbxmntmShowUserPaths = new JCheckBoxMenuItem("Einzelne Wege anzeigen");
		chckbxmntmShowUserPaths.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		chckbxmntmShowUserPaths.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chckbxmntmShowUserPathsActionPerformed(e);
			}
		});
		menuPieGraph.add(chckbxmntmShowUserPaths);

		miResetPieGraphView = new JMenuItem("Anzeige zurücksetzen");
		miResetPieGraphView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miResetViewers(e);
			}
		});
		menuPieGraph.add(miResetPieGraphView);

		separator = new JSeparator();
		menuPieGraph.add(separator);

		miSavePieGraphAsPicture = new JMenuItem("Pie-Graph als Bild speichern");
		miSavePieGraphAsPicture.setEnabled(false);
		menuPieGraph.add(miSavePieGraphAsPicture);
		miSavePieGraphAsPicture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miSavePieGraphAsPictureActionPerformed(e);
			}
		});
		miSavePieGraphAsPicture.setActionCommand("");

		menuExit = new JMenu("Beenden");
		menuBar.add(menuExit);

		miExit = new JMenuItem("Anwendung beenden");
		miExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miExitActionPerformed(e);
			}
		});
		menuExit.add(miExit);

		menuTesten = new JMenu("Testen");
		menuTesten.setFont(new Font("Lucida Grande", Font.PLAIN, 0));
		menuBar.add(menuTesten);

		miImportRecapo = new JMenuItem("import ReCaPo");
		miImportRecapo.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		miImportRecapo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				miImportRecapoTestActionPerformed(e);
			}
		});
		menuTesten.add(miImportRecapo);

		tbPaneMain = new JTabbedPane(JTabbedPane.TOP);
		tbPaneMain.setBorder(new EmptyBorder(5, 0, 0, 0));
		getContentPane().add(tbPaneMain, BorderLayout.CENTER);

		pnlOverview = new JPanel();
		pnlOverview.setBorder(new EmptyBorder(15, 15, 15, 15));
		tbPaneMain.addTab("Übersicht", null, pnlOverview, null);
		GridBagLayout gbl_pnlOverview = new GridBagLayout();
		gbl_pnlOverview.columnWidths = new int[] { 175, 30, 175, 30, 175, 0 };
		gbl_pnlOverview.rowHeights = new int[] { 0, 0, 20, 0, 0, 0 };
		gbl_pnlOverview.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
		gbl_pnlOverview.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		pnlOverview.setLayout(gbl_pnlOverview);

		lblTotalSolutionRate = new JLabel("Gesamt-Erfolgsrate:");
		GridBagConstraints gbc_lblTotalSolutionRate = new GridBagConstraints();
		gbc_lblTotalSolutionRate.anchor = GridBagConstraints.WEST;
		gbc_lblTotalSolutionRate.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalSolutionRate.gridx = 0;
		gbc_lblTotalSolutionRate.gridy = 0;
		pnlOverview.add(lblTotalSolutionRate, gbc_lblTotalSolutionRate);

		lblTotalCoverageOverview = new JLabel("Gesamt-Abdeckung:");
		GridBagConstraints gbc_lblTotalCoverageOverview = new GridBagConstraints();
		gbc_lblTotalCoverageOverview.anchor = GridBagConstraints.WEST;
		gbc_lblTotalCoverageOverview.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalCoverageOverview.gridx = 2;
		gbc_lblTotalCoverageOverview.gridy = 0;
		pnlOverview.add(lblTotalCoverageOverview, gbc_lblTotalCoverageOverview);

		lblUserCount = new JLabel("Anzahl Teilnehmer:");
		GridBagConstraints gbc_lblUserCount = new GridBagConstraints();
		gbc_lblUserCount.anchor = GridBagConstraints.WEST;
		gbc_lblUserCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblUserCount.gridx = 4;
		gbc_lblUserCount.gridy = 0;
		pnlOverview.add(lblUserCount, gbc_lblUserCount);

		barTotalSolutionRate = new JProgressBar();
		barTotalSolutionRate.setStringPainted(true);
		GridBagConstraints gbc_barTotalSolutionRate = new GridBagConstraints();
		gbc_barTotalSolutionRate.fill = GridBagConstraints.HORIZONTAL;
		gbc_barTotalSolutionRate.insets = new Insets(0, 0, 5, 5);
		gbc_barTotalSolutionRate.gridx = 0;
		gbc_barTotalSolutionRate.gridy = 1;
		pnlOverview.add(barTotalSolutionRate, gbc_barTotalSolutionRate);

		barTotalCoverageOverview = new JProgressBar();
		barTotalCoverageOverview.setStringPainted(true);
		GridBagConstraints gbc_barTotalCoverageOverview = new GridBagConstraints();
		gbc_barTotalCoverageOverview.fill = GridBagConstraints.HORIZONTAL;
		gbc_barTotalCoverageOverview.insets = new Insets(0, 0, 5, 5);
		gbc_barTotalCoverageOverview.gridx = 2;
		gbc_barTotalCoverageOverview.gridy = 1;
		pnlOverview.add(barTotalCoverageOverview, gbc_barTotalCoverageOverview);

		lblUserCountIndicator = new JLabel("0");
		lblUserCountIndicator.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblUserCountIndicator = new GridBagConstraints();
		gbc_lblUserCountIndicator.insets = new Insets(0, 0, 5, 5);
		gbc_lblUserCountIndicator.anchor = GridBagConstraints.WEST;
		gbc_lblUserCountIndicator.gridx = 4;
		gbc_lblUserCountIndicator.gridy = 1;
		pnlOverview.add(lblUserCountIndicator, gbc_lblUserCountIndicator);

		separatorOverview = new JSeparator();
		GridBagConstraints gbc_separatorOverview = new GridBagConstraints();
		gbc_separatorOverview.weightx = 1.0;
		gbc_separatorOverview.fill = GridBagConstraints.BOTH;
		gbc_separatorOverview.insets = new Insets(0, 0, 5, 0);
		gbc_separatorOverview.gridwidth = 6;
		gbc_separatorOverview.gridx = 0;
		gbc_separatorOverview.gridy = 2;
		pnlOverview.add(separatorOverview, gbc_separatorOverview);

		lblTaskOverview = new JLabel("Aufgaben-Übersicht:");
		GridBagConstraints gbc_lblTaskOverview = new GridBagConstraints();
		gbc_lblTaskOverview.insets = new Insets(0, 0, 5, 5);
		gbc_lblTaskOverview.anchor = GridBagConstraints.WEST;
		gbc_lblTaskOverview.gridx = 0;
		gbc_lblTaskOverview.gridy = 3;
		pnlOverview.add(lblTaskOverview, gbc_lblTaskOverview);

		scrollPaneSurvey = new JScrollPane();
		scrollPaneSurvey.setBorder(new LineBorder(Color.GRAY, 1, true));
		GridBagConstraints gbc_scrollPaneSurvey = new GridBagConstraints();
		gbc_scrollPaneSurvey.gridwidth = 6;
		gbc_scrollPaneSurvey.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneSurvey.gridx = 0;
		gbc_scrollPaneSurvey.gridy = 4;
		pnlOverview.add(scrollPaneSurvey, gbc_scrollPaneSurvey);

		tblTaskSurvey = new JTable();
		scrollPaneSurvey.setViewportView(tblTaskSurvey);

		pnlProblems = new JPanel();
		pnlProblems.setBorder(new EmptyBorder(15, 15, 15, 15));
		tbPaneMain.addTab("Probleme", null, pnlProblems, null);
		pnlProblems.setLayout(new BorderLayout(0, 0));

		lblProblem = new JLabel(
				"<html>\n\t<p>\n\tIm Folgenden werden die Seiten aufgelistet, die ihren Testpersonen Probleme bereitet haben. \n"
						+ "\t</p>\n\t<p style='margin-top:10'>\n\tJe höher der Malus-Wert, desto größer die Probleme.\n"
						+ "\t</p>\n\t<p style='margin-top:10'>\n\tJe höher die latenz, desto länger mussten die Testpersonen überlegen.\n\t</p>\n</html>");
		lblProblem.setBorder(new EmptyBorder(0, 0, 15, 0));
		pnlProblems.add(lblProblem, BorderLayout.NORTH);
		
		splitPane = new JSplitPane();
		splitPane.setResizeWeight(1.0);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		pnlProblems.add(splitPane, BorderLayout.CENTER);
		
				scrollPaneProblems = new JScrollPane();
				scrollPaneProblems.setBorder(new LineBorder(Color.GRAY, 1, true));
				splitPane.setLeftComponent(scrollPaneProblems);
				
						tblProblems = new JTable();
						
								scrollPaneProblems.setViewportView(tblProblems);
								
								scrollPaneSSDLog = new JScrollPane();
								scrollPaneSSDLog.setPreferredSize(new Dimension(4, 70));
								scrollPaneSSDLog.setBorder(new LineBorder(Color.GRAY, 1, true));
								splitPane.setRightComponent(scrollPaneSSDLog);
								
								txtrSSDLog = new JTextArea();
								txtrSSDLog.setPreferredSize(new Dimension(0, 50));
								txtrSSDLog.setEditable(false);
								txtrSSDLog.setLineWrap(true);
								txtrSSDLog.setWrapStyleWord(true);
								txtrSSDLog.setTabSize(4);
								scrollPaneSSDLog.setViewportView(txtrSSDLog);
								
								lblSsdlog = new JLabel("SSD-Log");
								scrollPaneSSDLog.setColumnHeaderView(lblSsdlog);
								splitPane.setDividerLocation(400);

		pnlDetails = new JPanel();
		pnlDetails.setBorder(new EmptyBorder(15, 15, 15, 15));
		tbPaneMain.addTab("Details", null, pnlDetails, null);

		lblShowDetails = new JLabel("Details anzeigen:");

		rdbtnProblem = new JRadioButton("nach Problem-Seite");
		rdbtnProblem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rdbtnProblemActionPerformed(e);
			}
		});
		buttonGroupDetails.add(rdbtnProblem);
		rdbtnProblem.setSelected(true);

		rdbtnTask = new JRadioButton("nach Aufgabe");
		rdbtnTask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rdbtnTaskActionPerformed(e);
			}
		});

		comboBoxProblem = new JComboBox<String>();
		comboBoxProblem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				comboBoxProblemActionPerformed(e);
			}
		});
		buttonGroupDetails.add(rdbtnTask);

		comboBoxTask = new JComboBox<String>();
		comboBoxTask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				comboBoxTaskActionPerformed(e);
			}
		});
		comboBoxTask.setEnabled(false);

		lblDetailsFor = new JLabel("Details zu ...");

		scrollPaneDetails = new JScrollPane();
		scrollPaneDetails.setBorder(new LineBorder(new Color(128, 128, 128), 1, true));

		tblDetails = new JTable();
		scrollPaneDetails.setViewportView(tblDetails);
		GroupLayout gl_pnlDetails = new GroupLayout(pnlDetails);
		gl_pnlDetails.setHorizontalGroup(gl_pnlDetails.createParallelGroup(Alignment.LEADING)
				.addComponent(lblShowDetails, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)
				.addGroup(gl_pnlDetails.createSequentialGroup().addComponent(rdbtnProblem).addGap(5)
						.addComponent(comboBoxProblem, 0, 403, Short.MAX_VALUE).addContainerGap())
				.addGroup(gl_pnlDetails.createSequentialGroup()
						.addComponent(rdbtnTask, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE).addGap(5)
						.addComponent(comboBoxTask, 0, 403, Short.MAX_VALUE).addContainerGap())
				.addComponent(scrollPaneDetails, GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
				.addComponent(lblDetailsFor, GroupLayout.DEFAULT_SIZE, 669, Short.MAX_VALUE));
		gl_pnlDetails.setVerticalGroup(gl_pnlDetails.createParallelGroup(Alignment.LEADING).addGroup(gl_pnlDetails
				.createSequentialGroup()
				.addGroup(gl_pnlDetails.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlDetails.createSequentialGroup().addComponent(lblShowDetails)
								.addGap(7).addComponent(rdbtnProblem))
						.addGroup(gl_pnlDetails.createSequentialGroup().addGap(21).addComponent(comboBoxProblem,
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGroup(gl_pnlDetails.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlDetails.createSequentialGroup().addGap(8).addComponent(rdbtnTask))
						.addGroup(gl_pnlDetails.createSequentialGroup().addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(comboBoxTask, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)))
				.addGap(9).addComponent(lblDetailsFor).addGap(10)
				.addComponent(scrollPaneDetails, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)));
		pnlDetails.setLayout(gl_pnlDetails);

		pnlCoverage = new JPanel();
		pnlCoverage.setBorder(new EmptyBorder(15, 15, 15, 15));
		tbPaneMain.addTab("Abdeckung", null, pnlCoverage, null);
		GridBagLayout gbl_pnlCoverage = new GridBagLayout();
		gbl_pnlCoverage.columnWidths = new int[] { 107, 0, 0 };
		gbl_pnlCoverage.rowHeights = new int[] { 0, 0, 22, 0, 0, 0 };
		gbl_pnlCoverage.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_pnlCoverage.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		pnlCoverage.setLayout(gbl_pnlCoverage);

		lblTotalCoverage = new JLabel("Gesamt-Abdeckung:");
		GridBagConstraints gbc_lblTotalCoverage = new GridBagConstraints();
		gbc_lblTotalCoverage.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblTotalCoverage.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalCoverage.gridx = 0;
		gbc_lblTotalCoverage.gridy = 0;
		pnlCoverage.add(lblTotalCoverage, gbc_lblTotalCoverage);

		barTotalCoverage = new JProgressBar();
		barTotalCoverage.setStringPainted(true);
		GridBagConstraints gbc_barTotalCoverage = new GridBagConstraints();
		gbc_barTotalCoverage.fill = GridBagConstraints.HORIZONTAL;
		gbc_barTotalCoverage.insets = new Insets(0, 0, 5, 5);
		gbc_barTotalCoverage.gridx = 0;
		gbc_barTotalCoverage.gridy = 1;
		pnlCoverage.add(barTotalCoverage, gbc_barTotalCoverage);

		separatorCoverage = new JSeparator();
		GridBagConstraints gbc_separatorCoverage = new GridBagConstraints();
		gbc_separatorCoverage.gridwidth = 2;
		gbc_separatorCoverage.insets = new Insets(0, 0, 5, 0);
		gbc_separatorCoverage.fill = GridBagConstraints.BOTH;
		gbc_separatorCoverage.gridx = 0;
		gbc_separatorCoverage.gridy = 2;
		pnlCoverage.add(separatorCoverage, gbc_separatorCoverage);

		lblCoverageInfo = new JLabel(
				"<html>\n\t<p>\n\tUm die Testabdeckung zu erhöhen, empfiehlt es sich, einen weiteren Vesuch mit veränderten\n"
						+ "\t</p>\n\t<p style='margin-top:10'>\n\tAufgabe durchzuführen. Um die Abdeckung optimal zu vergrößern sollten folgende Aufgaben\n"
						+ "\t</p>\n\t<p style='margin-top:10'>\n\tverwendet werden.\n\t</p>\n</html>");
		lblCoverageInfo.setBorder(new EmptyBorder(0, 0, 15, 0));
		GridBagConstraints gbc_lblCoverageInfo = new GridBagConstraints();
		gbc_lblCoverageInfo.insets = new Insets(0, 0, 5, 0);
		gbc_lblCoverageInfo.gridwidth = 2;
		gbc_lblCoverageInfo.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCoverageInfo.gridx = 0;
		gbc_lblCoverageInfo.gridy = 3;
		pnlCoverage.add(lblCoverageInfo, gbc_lblCoverageInfo);

		scrollPaneCoverage = new JScrollPane();
		scrollPaneCoverage.setBorder(new LineBorder(Color.GRAY, 1, true));
		GridBagConstraints gbc_scrollPaneCoverage = new GridBagConstraints();
		gbc_scrollPaneCoverage.gridwidth = 2;
		gbc_scrollPaneCoverage.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPaneCoverage.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneCoverage.gridx = 0;
		gbc_scrollPaneCoverage.gridy = 4;
		pnlCoverage.add(scrollPaneCoverage, gbc_scrollPaneCoverage);

		tblCoverage = new JTable();
		scrollPaneCoverage.setViewportView(tblCoverage);

		pnlIAGraph = new JPanel();
		pnlIAGraph.setBorder(new EmptyBorder(15, 15, 15, 15));
		tbPaneMain.addTab("Strukturgraph", pnlIAGraph);
		pnlIAGraph.setLayout(new BorderLayout(0, 0));

		iaView = iaViewer.addDefaultView(false);
		iaView.setShortcutManager(new RCSGraphShortcutManager());
		iaView.setMouseManager(new RCSGraphMouseManager());

		pnlIAGraph.add(iaView, BorderLayout.CENTER);

		pnlPieGraph = new JPanel();
		pnlPieGraph.setBorder(new EmptyBorder(15, 15, 15, 15));
		tbPaneMain.addTab("Pie-Graph", null, pnlPieGraph, null);
		tbPaneMain.setEnabledAt(5, true);
		pnlPieGraph.setLayout(new BorderLayout(0, 0));

		pieView = pieViewer.addDefaultView(false);
		pieView.setShortcutManager(new RCSGraphShortcutManager());
		pieView.setMouseManager(new RCSGraphMouseManager());
		pieView.setVisible(false);
		pnlPieGraph.add(pieView, BorderLayout.CENTER);

		pieGraphLegend = new PieGraphLegend();
		pieGraphLegend.setBorder(null);
		pieGraphLegend.setVisible(false);
		pieGraphLegend.setPreferredSize(new Dimension(10, 105));
		pieGraphLegend.setSize(pnlPieGraph.getWidth(), 150);
		pnlPieGraph.add(pieGraphLegend, BorderLayout.SOUTH);
		pieGraphLegend.setLayout(null);

		pnlPieGraphComboBoxes = new JPanel();
		pnlPieGraph.add(pnlPieGraphComboBoxes, BorderLayout.NORTH);
		pnlPieGraphComboBoxes.setLayout(new BorderLayout(0, 0));

		comboBoxTaskForPieGraph = new JComboBox<RCSTask>();
		pnlPieGraphComboBoxes.add(comboBoxTaskForPieGraph, BorderLayout.NORTH);
		comboBoxTaskForPieGraph.setSelectedIndex(-1);

		comboBoxUserForPieGraph = new JComboBox<RCSPath>();
		comboBoxUserForPieGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				comboBoxUserForPieGraphActionPerformed(e);
			}
		});
		comboBoxUserForPieGraph.setVisible(false);
		pnlPieGraphComboBoxes.add(comboBoxUserForPieGraph, BorderLayout.CENTER);
		comboBoxTaskForPieGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				comboBoxTaskForPieGraphActionPerformed(e);
			}
		});

	}

	/**
	 * This method will be triggered, if the user clicks on the
	 * miImportRecapoIA-MenuItem to import the website structure through the
	 * import menu.
	 *
	 * @param e
	 *            event
	 */
	protected void miImportRecapoIAActionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML-Dateien", "xml");
		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// check if the selected File is an XML-File that represents a IA
			// export from ReCaPo
			if (FileChecker.checkRoot(chooser.getSelectedFile(), "recapo")) {
				recalys.buildGraphXML(chooser.getSelectedFile());
				miImportResults.setEnabled(true);

				chckbxmntmShowCoverage.setVisible(false);
				chckbxmntmShowCoverage.setSelected(false);
				miSaveSSDlog.setEnabled(false);
			} else {
				JOptionPane.showMessageDialog(this,
						"Die ausgewählte XML-Datei repräsentiert keine Informationsarchitektur, die in ReCaPo erstellt wurde.",
						"XML-Datei inkorrekt", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * This Method will be triggered, if the user clicks on the
	 * miImportResults-MenuItem to import the experiment's results.
	 *
	 * @param e
	 *            event
	 */
	protected void miImportResultsActionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML-Dateien", "xml");
		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// check if the selected File is an XML-File that represents a
			// results export from ReCaPo
			if (FileChecker.checkRoot(chooser.getSelectedFile(), "cardsort")) {
				recalys.importResults(chooser.getSelectedFile());
				recalys.analyse();

				PieGraph pieGraphTest = new PieGraph();
				pieGraphTest.init(recalys.getGraph());

				chckbxmntmShowCoverage.setVisible(true);
				miSaveSSDlog.setEnabled(true);
			} else
				JOptionPane.showMessageDialog(this,
						"Die ausgewählte XML-Datei repräsentiert keine Resultate, die in ReCaPo erstellt wurden.",
						"XML-Datei inkorrekt", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Imports a whole experiment. This method will be triggered if the user
	 * clicks on the miImportExperiment-MenuItem.
	 * 
	 * @param e
	 *            event
	 */
	protected void miImportExperimentActionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("RCS-Dateien", "rcs");
		chooser.setFileFilter(filter);

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			recalys.loadExperiment(chooser.getSelectedFile());
		}
	}

	/**
	 * This method will be triggered, if the user clicks on the miSsd-MenuItem
	 * to change the parameters for the systematic search detection. A
	 * SystematicSearchDetection-Dialog will be shown.
	 * 
	 * @param e
	 *            event
	 */
	protected void miSsdActionPerformed(ActionEvent e) {
		ssdd.updateValues();
		ssdd.setVisible(true);
	}

	/**
	 * This method will be triggered, if the user clicks on to the
	 * miAnalyseAction-MenuItem to start the analysis of the menu item. The
	 * analysis will be executed.
	 * 
	 * @param e
	 *            event
	 */
	protected void miAnalyseActionPerformed(ActionEvent e) {
		recalys.resetAnalysis();
		recalys.analyse();
	}

	/**
	 * Exports the whole experiment into an rcs-file. This method will be
	 * triggerd if the user clicks on the miExport-MenuItem.
	 * 
	 * @param e
	 *            event
	 */
	protected void miExportActionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fileName = chooser.getSelectedFile().getPath();
			if (!fileName.endsWith(".rcs"))
				fileName += ".rcs";
			File file = new File(fileName);

			recalys.saveExperiment(file);
		}
	}

	/**
	 * This method will be triggered, if the user clicks on the miExit-MenuItem
	 * and then will quit the app.
	 *
	 * @param e
	 *            event
	 */
	protected void miExitActionPerformed(ActionEvent e) {
		System.exit(0);
	}

	// TODO Test Menüpunkt löschen am Ende
	protected void miImportRecapoTestActionPerformed(ActionEvent e) {
		File iaFile = new File("example_data/dlrg_ia.xml");
		File resultsFile = new File("example_data/dlrg_results.xml");

		recalys.buildGraphXML(iaFile);
		recalys.importResults(resultsFile);

		recalys.resetAnalysis();
		recalys.analyse();

		PieGraph pieGraphTest = new PieGraph();
		pieGraphTest.init(recalys.getGraph());
		chckbxmntmShowCoverage.setEnabled(true);
	}

	/**
	 * This method will be triggered, if the state of the
	 * chckbxmntmShowLegendItem changed. This method will activate/deactivate
	 * the legend for the PieGraph.
	 *
	 * @param e
	 *            the e
	 */
	protected void chckbxmntmShowLegendItemStateChanged(ItemEvent e) {
		showLegend = chckbxmntmShowLegend.isSelected();
		pieGraphLegend.setVisible(showLegend);
	}

	/**
	 * This method will be triggered, if the user clicks on the
	 * miSaveIAGraphAsPicture-Menu Item.<br/>
	 * This method will open a save dialog to save the current ia-graph.
	 * 
	 * @author Roman Kober
	 * @param e
	 *            the event that triggered.
	 */
	protected void miSaveIAGraphAsPictureActionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		DateFormat df = new SimpleDateFormat("dd.MM.YY-HH:mm:ss");
		fc.setSelectedFile(new File("Strukturgraph_" + df.format(Calendar.getInstance().getTime()) + ".png"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String saveDirectory = fc.getSelectedFile().getPath();
			if (!saveDirectory.endsWith(".png")) {
				saveDirectory = saveDirectory + ".png";
			}
			iaGraph.addAttribute("ui.screenshot", saveDirectory);
		}
	}

	/**
	 * This method will be triggered, if the user clicks on the
	 * miSavePieGraphAsPicture-Menu Item.<br/>
	 * This method will open a save dialog to save the current pie-graph.
	 * 
	 * @author Roman Kober
	 * @param e
	 *            the event that triggered.
	 */
	protected void miSavePieGraphAsPictureActionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		DateFormat df = new SimpleDateFormat("dd.MM.YY-HH:mm:ss");
		fc.setSelectedFile(new File("PieGraph_" + df.format(Calendar.getInstance().getTime()) + ".png"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String saveDirectory = fc.getSelectedFile().getPath();
			if (!saveDirectory.endsWith(".png")) {
				saveDirectory = saveDirectory + ".png";
			}
			pieGraph.addAttribute("ui.screenshot", saveDirectory);
		}
	}

	/**
	 * This method will be triggered if the user clicks on the rdbtnProblem to
	 * show details of problems.
	 *
	 * @param e
	 *            event
	 */
	protected void rdbtnProblemActionPerformed(ActionEvent e) {
		comboBoxProblem.setEnabled(true);
		comboBoxTask.setEnabled(false);
		rdbtnTask.setSelected(false);
		detailsForProblem = true;
		if (comboBoxProblem.getComponentCount() == 0)
			return;
		updateDetailsPanel();
	}

	/**
	 * This method will be triggered if the user clicks on the rdbtnTask to show
	 * details of tasks.
	 *
	 * @param e
	 *            the e
	 */
	protected void rdbtnTaskActionPerformed(ActionEvent e) {
		comboBoxTask.setEnabled(true);
		comboBoxProblem.setEnabled(false);
		rdbtnProblem.setSelected(false);
		detailsForProblem = false;
		if (comboBoxProblem.getComponentCount() == 0)
			return;
		updateDetailsPanel();
	}

	/**
	 * This method will be triggered if the user changes the currently selected
	 * item in the comboBoxProblem to show more details for this problem.
	 *
	 * @param e
	 *            the e
	 */
	protected void comboBoxProblemActionPerformed(ActionEvent e) {
		if (comboBoxProblem.getComponentCount() == 0)
			return;
		updateDetailsPanel();
	}

	/**
	 * This method will be triggered if the user changes the currently selected
	 * item in the comboBoxTask to show more details for this task.
	 *
	 * @param e
	 *            the event
	 */
	protected void comboBoxTaskActionPerformed(ActionEvent e) {
		if (comboBoxTask.getComponentCount() == 0)
			return;
		updateDetailsPanel();
	}

	/**
	 * This method will be triggered if the user changes the currently selected
	 * item in the comboBoxTaskForPieGraph to show the Pie Graph for the current
	 * task.
	 *
	 * @param e
	 *            the event
	 */
	protected void comboBoxTaskForPieGraphActionPerformed(ActionEvent e) {
		if (updateInProgress) {
			return;
		}
		pieGraph.init(recalys.getGraph());
		pieGraph.analyseTask((RCSTask) comboBoxTaskForPieGraph.getSelectedItem());
		pieView.setVisible(true);

		/* User Paths ComboBox */
		newTaskSelected = true;
		comboBoxUserForPieGraph.removeAllItems();
		for (RCSPath path : ((RCSTask) comboBoxTaskForPieGraph.getSelectedItem()).getPaths()) {
			comboBoxUserForPieGraph.addItem(path);
		}
		newTaskSelected = false;
		comboBoxUserForPieGraph.setSelectedIndex(0);

	}

	/**
	 * This method will be triggered if the user changes the currently selected
	 * item in the UserForPieGraph to show the path of a specific user.
	 *
	 * @param e
	 *            the event
	 */
	protected void comboBoxUserForPieGraphActionPerformed(ActionEvent e) {
		if (updateInProgress || newTaskSelected) {
			return;
		}
		if (chckbxmntmShowUserPaths.isSelected()) {
			this.pieGraph.addPathInfo((RCSPath) comboBoxUserForPieGraph.getSelectedItem());
		}
	}

	/**
	 * This method will be triggered, if the state of the
	 * chckbxmntmShowUserPaths changed. This method will activate/deactivate the
	 * comboBox for choosing unique user paths.
	 *
	 * @param e
	 *            the e
	 */
	protected void chckbxmntmShowUserPathsActionPerformed(ActionEvent e) {
		this.comboBoxUserForPieGraph.setVisible(this.chckbxmntmShowUserPaths.isSelected());
		if (this.chckbxmntmShowUserPaths.isSelected()) {
			this.comboBoxUserForPieGraph.setSelectedIndex(0);
		} else {
			this.pieGraph.removeLastUserPath();
		}
	}

	/**
	 * This method will be triggered, if the state of the chbxShowCoverage
	 * changed. This method will activate/deactivate the the unique
	 * visualization of nodes that are covered within the tasks.
	 *
	 * @param e
	 *            the e
	 */
	protected void chckbxmntmShowCoverageStateChanged(ChangeEvent e) {
		if (iaGraph != null) {
			if (chckbxmntmShowCoverage.isSelected()) {
				this.iaGraph.markTaskTargets(this.recalys.getExperiment());
			} else {
				this.iaGraph.removeTaskTargets();
			}
		}
	}

	protected void miResetViewers(ActionEvent e) {

		if (this.pieViewer != null) {
			pieView.getCamera().resetView();
		}
	}

	protected void miResetIAViewActionPerformed(ActionEvent e) {
		if (this.iaView != null) {
			iaView.getCamera().resetView();
		}
	}
	
	protected void miSaveSSDlogActionPerformed(ActionEvent e) {
		if ( txtrSSDLog.getText().equals("")) {
			JOptionPane.showMessageDialog(this,
					"Der SSD-Log ist leer und kann daher nicht gespeichert werden.", "SSD-Log leer", JOptionPane.WARNING_MESSAGE);
		} else {
			JFileChooser fc = new JFileChooser();
			fc.setSelectedFile(new File("ssd-log.txt"));
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				String saveDirectory = fc.getSelectedFile().getPath();
				if (!saveDirectory.endsWith(".txt")) {
					saveDirectory = saveDirectory + ".txt";
				}
				this.recalys.exportSystematicSearchLog(saveDirectory);
			}
		}
		
	}

	/**
	 * Gets the ia graph.
	 *
	 * @return the iaGraph
	 */
	public IAGraph getIaGraph() {
		return iaGraph;
	}

	/**
	 * Gets the pie graph.
	 *
	 * @return the pieGraph
	 */
	public PieGraph getPieGraph() {
		return pieGraph;
	}

	/**
	 * Checks if user paths are active.
	 *
	 * @return true, if user paths are active
	 */
	public boolean isUserPathsActive() {
		return chckbxmntmShowUserPaths.isSelected();
	}

	/*
	 * Generated Components for the GUI
	 */
	private JMenuBar menuBar;
	private JMenu menuImport;
	private JLabel lblRecapo;
	private JMenuItem miImportRecapoIA;
	private JMenuItem miImportResults;
	private JSeparator separatorImport2;
	private JMenuItem miImportExperiment;
	private JMenu menuAnalyse;
	private JMenuItem miSsd;
	private JMenuItem miAnalyse;
	private JMenu menuExport;
	private JMenuItem miExport;
	private JMenu menuExit;
	private JMenuItem miExit;
	private JTabbedPane tbPaneMain;
	private JPanel pnlOverview;
	private JLabel lblTotalSolutionRate;
	private JProgressBar barTotalSolutionRate;
	private JLabel lblTotalCoverageOverview;
	private JProgressBar barTotalCoverageOverview;
	private JLabel lblUserCount;
	private JLabel lblUserCountIndicator;
	private JSeparator separatorOverview;
	private JLabel lblTaskOverview;
	private JScrollPane scrollPaneSurvey;
	private JTable tblTaskSurvey;
	private JPanel pnlProblems;
	private JPanel pnlDetails;
	private JPanel pnlCoverage;
	private JLabel lblProblem;
	private JScrollPane scrollPaneProblems;
	private JTable tblProblems;
	private JLabel lblShowDetails;
	private JRadioButton rdbtnProblem;
	private JRadioButton rdbtnTask;
	private final ButtonGroup buttonGroupDetails = new ButtonGroup();
	private JComboBox<String> comboBoxProblem;
	private JComboBox<String> comboBoxTask;
	private JLabel lblDetailsFor;
	private JTable tblDetails;
	private JLabel lblTotalCoverage;
	private JProgressBar barTotalCoverage;
	private JSeparator separatorCoverage;
	private JLabel lblCoverageInfo;
	private JScrollPane scrollPaneCoverage;
	private JTable tblCoverage;
	private JMenu menuTesten;
	private JMenuItem miImportRecapo;
	private JPanel pnlPieGraph;
	private JComboBox<RCSTask> comboBoxTaskForPieGraph;
	private JPanel pnlIAGraph;
	private JMenuItem miSaveIAGraphAsPicture;
	private ViewPanel iaView;
	private ViewPanel pieView;
	private JMenuItem miSavePieGraphAsPicture;
	private JMenu menuPieGraph;
	private JCheckBoxMenuItem chckbxmntmShowLegend;
	private JSeparator separator;
	private JPanel pnlPieGraphComboBoxes;
	private JComboBox<RCSPath> comboBoxUserForPieGraph;
	private JCheckBoxMenuItem chckbxmntmShowUserPaths;
	private JMenuItem miResetPieGraphView;
	private JMenu menuIAGraph;
	private JSeparator separator_1;
	private JMenuItem miResetIAView;
	private JCheckBoxMenuItem chckbxmntmShowCoverage;
	private JSplitPane splitPane;
	private JTextArea txtrSSDLog;
	private JScrollPane scrollPaneSSDLog;
	private JLabel lblSsdlog;
	private JMenuItem miSaveSSDlog;
	private JScrollPane scrollPaneDetails;

	
}
