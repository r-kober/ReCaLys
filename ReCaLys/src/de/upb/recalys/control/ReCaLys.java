package de.upb.recalys.control;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import de.upb.recalys.helper.ResourceHandler;
import de.upb.recalys.model.ExperimentFile;
import de.upb.recalys.model.RCSGraph;
import de.upb.recalys.model.RCSNode;
import de.upb.recalys.model.RCSTask;
import de.upb.recalys.view.GUI;

/**
 * This class defines the main class of this app. It controlls the behavior of
 * this app.
 * 
 * @author Roman Kober
 * @version 1.1
 */
public class ReCaLys {

	private boolean complete = false;
	private double maxProblemRate = 0.6;
	private double maxReturnRate = 0.15;
	private int timeToThink = 100; // in milliseconds
	private int nodesToInspect = -1;
	private GUI gui;
	private RCSGraph graph;
	private ArrayList<RCSTask> experiment;
	private LinkedList<RCSNode> badList;
	@SuppressWarnings("rawtypes")
	private LinkedList[] hotList;
	private int userCount = 0;
	private ObjectInputStream ois;

	/**
	 * Constructor: Creates a new ReCaLys-object
	 */
	public ReCaLys() {
		super();

		final ReCaLys recalys = this;
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				gui = new GUI(recalys);
				gui.setVisible(true);
				gui.setIconImage(
						Toolkit.getDefaultToolkit().getImage(ResourceHandler.getURL("/ReCaLys_Logo_Windows.png")));
			}
		});
	}

	/**
	 * Main function that starts the app
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		new ReCaLys();
	}

	/**
	 * Builds the Graph based on the imported XML-Structure-File
	 * 
	 * @param importFile
	 *            the xml-file that contains the website-structure
	 * 
	 * @author Roman Kober
	 */
	public void buildGraphXML(File importFile) {
		graph = new RCSGraph();

		graph.buildGraph(importFile);

		graph.computeDistances();
		graph.buildLevelGraph();
		graph.addNodeDegrees();
		graph.addLeavesCount();
		graph.computeNodesToInspect();
		graph.computeMinimalLatencies(250);

		gui.getIaGraph().init(graph);
		gui.getPieGraph().init(graph);

		gui.setAnalyseMenuEnabled(false);
		gui.setGraphMenuEnabled(true);
	}

	/**
	 * Imports a xml-file that contains the results of an experiment in ReCaPo
	 * and creates an experiment that can be analyzed.
	 * 
	 * @param importFile
	 *            file that shall be imported
	 * @author Roman Kober
	 */
	public void importResults(File importFile) {
		graph.resetGraphProperties();

		complete = false;
		experiment = new ArrayList<>();
		userCount = 0;

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();

			ResultsContentHandler handler = new ResultsContentHandler(this);
			saxParser.parse(importFile, handler);

			gui.updateGUI();
			gui.setAnalyseMenuEnabled(true);
			gui.setExportMenuEnabled(true);
			gui.setPieGraphMenuItemsEnabled(true);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			Logger.getLogger(ReCaLys.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Returns the number of nodes that must be inspected combined for detection
	 * of systematic searching
	 * 
	 * @return number of nodes to inspect combined
	 */
	public int getNodesToInspect() {
		return nodesToInspect;
	}

	/**
	 * Sets the maximal problem-rate for the detection of systematic searching
	 * 
	 * @param rate
	 */
	public void setMaxProblemRate(double rate) {
		maxProblemRate = rate;
	}

	/**
	 * Sets the maximal return-rate for the detection of systematic searching
	 * 
	 * @param rate
	 */
	public void setMaxReturnRate(double rate) {
		maxReturnRate = rate;
	}

	/**
	 * Gives the maximal problem-rate for the detection of systematic searching
	 * 
	 * @return the maximal problem-rate
	 */
	public double getMaxProblemRate() {
		return maxProblemRate;
	}

	/**
	 * Gives the maximal return-rate for the detection of systematic searching
	 * 
	 * @return the maximal return-rate
	 */
	public double getMaxReturnRate() {
		return maxReturnRate;
	}

	/**
	 * Sets in the time a standard user will max. need to think before choosing
	 * a link
	 * 
	 * @param time
	 *            time to think
	 */
	public void setTimeToThink(int time) {
		timeToThink = time;
	}

	/**
	 * Gives the time a standard user will max. need to think before choosing a
	 * link
	 * 
	 * @return time to think
	 */
	public int getTimeToThink() {
		return timeToThink;
	}

	/**
	 * Returns the graph that represents the website structure
	 * 
	 * @return graph
	 */
	public RCSGraph getGraph() {
		return graph;
	}

	/**
	 * Resets the analysis of the experiment on the given graph
	 */
	public void resetAnalysis() {
		graph.resetAnalysis(experiment.size());
	}

	/**
	 * Starts the analysis of the experiment
	 */
	public void analyse() {
		resetAnalysis();
		graph.computeMinimalLatencies(timeToThink);
		nodesToInspect = graph.computeNodesToInspect();

		for (RCSTask task : experiment) {
			task.resetAnalysis();
			task.detectNodesOnOptimalPaths();
			task.analysePaths(nodesToInspect, maxProblemRate, maxReturnRate);
		}

		badList = graph.getBadList();

		hotList = graph.getHotList();
		complete = true;
		updateGUI();
		updateSystematicSearchLog();
	}

	/**
	 * Saves the whole experiment into an rcs-file
	 * 
	 * @param file
	 *            file the experiment shall be saved into
	 * 
	 * @deprecated As of Version 1.1 this method is not used anymore, because
	 *             the import process for ReCaPo is much easier and a separate
	 *             import and export mechanism is not needed anymore
	 */
	@Deprecated
	public void saveExperiment(File file) {
		ExperimentFile ef = new ExperimentFile(graph, experiment, maxProblemRate, maxReturnRate, timeToThink,
				nodesToInspect, userCount);
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(ef);
		} catch (IOException ex) {
			Logger.getLogger(ReCaLys.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fos.close();
			} catch (IOException ex) {
				Logger.getLogger(ReCaLys.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	}

	/**
	 * Imports an experiment
	 * 
	 * @param file
	 *            experiment file
	 * 
	 * @deprecated As of Version 1.1 this method is not used anymore, because
	 *             the import process for ReCaPo is much easier and a separate
	 *             import and export mechanism is not needed anymore
	 */
	@Deprecated
	public void loadExperiment(File file) {
		complete = false;
		ExperimentFile ef;
		FileInputStream fos = null;

		try {
			fos = new FileInputStream(file);
			ois = new ObjectInputStream(fos);
			ef = (ExperimentFile) ois.readObject();
			graph = ef.getGraph();
			experiment = ef.getTasks();
			maxProblemRate = ef.getMaxProblemRate();
			maxReturnRate = ef.getMaxReturnRate();
			nodesToInspect = ef.getNodesToInspect();
			timeToThink = ef.getTimeToThink();
			userCount = ef.getUserCount();
			gui.updateGUI();
			gui.setAnalyseMenuEnabled(true);
			gui.setExportMenuEnabled(true);
			analyse();
		} catch (ClassNotFoundException | IOException ex) {
			Logger.getLogger(ReCaLys.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fos.close();
			} catch (IOException ex) {
				Logger.getLogger(ReCaLys.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	}

	/**
	 * Increases the number of users who attended the experiment by 1
	 */
	public void increaseUserCount() {
		userCount++;
		gui.updateGUI();
	}

	/**
	 * Returns the number of users who attended the experiment
	 * 
	 * @return userCount
	 */
	public int getUserCount() {
		return userCount;
	}

	/**
	 * Gives the total coverage of the experiment
	 * 
	 * @return coverage of the experiment
	 */
	public double getCoverage() {
		return graph.getCoverage();
	}

	/**
	 * Returns the RCS-Experiment in an ArrayList
	 * 
	 * @return experiment
	 */
	public ArrayList<RCSTask> getExperiment() {
		return experiment;
	}

	/**
	 * Gets the solution-rate over all tasks
	 * 
	 * @return totalSolutionRate
	 */
	public double getTotalSolutionRate() {
		double tmp = 0;
		for (RCSTask task : experiment)
			tmp += task.getSolutionRate();
		return tmp / experiment.size();
	}

	/**
	 * Gets the BadList computed during the experiment
	 * 
	 * @return badList
	 */
	public LinkedList<RCSNode> getBadList() {
		return badList;
	}

	/**
	 * Gets the HotList computed during the experiment
	 * 
	 * @return hotList
	 */
	@SuppressWarnings("rawtypes")
	public LinkedList[] getHotList() {
		return hotList;
	}

	/**
	 * Updates the GUI
	 */
	public void updateGUI() {
		gui.updateGUI();
	}

	/**
	 * Returns if the analysis is executed entirely
	 * 
	 * @return
	 */
	public boolean analysisComplete() {
		return complete;
	}

	/**
	 * Updates the Log in the GUI for the detection of systematic searching
	 */
	public void updateSystematicSearchLog() {
		String log = graph.getSystematicSearchingLog();
		gui.setSSDLog(log);

		if (log.equals(""))
			return;
		try {
			PrintWriter pw = new PrintWriter(new FileWriter("ssd-log.txt"));
			pw.print(log);
			pw.flush();
			pw.close();
		} catch (IOException ex) {
			Logger.getLogger(ReCaLys.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Exports the logfile for the detection of systematic searching into the
	 * given save directory.
	 *
	 * @param saveDirectory
	 *            the save directory
	 */
	public void exportSystematicSearchLog(String saveDirectory) {
		String log = graph.getSystematicSearchingLog();

		try {
			PrintWriter pw = new PrintWriter(new FileWriter(saveDirectory));
			pw.print(log);
			pw.flush();
			pw.close();
		} catch (IOException ex) {
			Logger.getLogger(ReCaLys.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
