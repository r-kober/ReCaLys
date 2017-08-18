package de.upb.recalys.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class defines an export file for an experiment that can be reimported
 * into this app
 * 
 * @author danielbrumberg
 * @version 1.0
 * 
 * @deprecated As of Version 1.1 this method is not used anymore, because the
 *             import process for ReCaPo is much easier and a separate import
 *             and export mechanism is not needed anymore
 */
@Deprecated
public class ExperimentFile implements Serializable {

	private static final long serialVersionUID = 8421508883471653786L;
	private double maxProblemRate;
	private double maxReturnRate;
	private int timeToThink; // in milliseconds
	private int nodesToInspect;
	private int userCount;
	private RCSGraph graph;
	private ArrayList<RCSTask> experiment;

	/**
	 * Constructor: Creates a new ExperimentFile
	 * 
	 * @param graph
	 *            the graph of the experiment
	 * @param tasks
	 *            the tasks of the experiment
	 * @param mPR
	 *            the maximum problem-rate of the experiment
	 * @param mRR
	 *            the maximum return-rate of the experiment
	 * @param tTT
	 *            the time to think of the experiment
	 * @param nTI
	 *            the number of nodes to inspect of the experiment
	 * @param uC
	 *            the number of users that partioned in the experiment
	 */
	public ExperimentFile(RCSGraph graph, ArrayList<RCSTask> tasks, double mPR, double mRR, int tTT, int nTI, int uC) {
		this.graph = graph;
		experiment = tasks;
		maxProblemRate = mPR;
		maxReturnRate = mRR;
		timeToThink = tTT;
		nodesToInspect = nTI;
		userCount = uC;
	}

	/**
	 * Returns the maximum problem-rate
	 * 
	 * @return maximum problem-rate
	 */
	public double getMaxProblemRate() {
		return maxProblemRate;
	}

	/**
	 * Returns the maximum return-rate
	 * 
	 * @return maximum return-rate
	 */
	public double getMaxReturnRate() {
		return maxReturnRate;
	}

	/**
	 * Returns the time to think
	 * 
	 * @return time to think
	 */
	public int getTimeToThink() {
		return timeToThink;
	}

	/**
	 * Returns number of nodes to inspect
	 * 
	 * @return number of nodes to inspect
	 */
	public int getNodesToInspect() {
		return nodesToInspect;
	}

	/**
	 * Returns the number of user that partioned in the experiment
	 * 
	 * @return number of users
	 */
	public int getUserCount() {
		return userCount;
	}

	/**
	 * Returns the graph of the experiment
	 * 
	 * @return graph of the experiment
	 */
	public RCSGraph getGraph() {
		return graph;
	}

	/**
	 * Returns the tasks of the experiment
	 * 
	 * @return tasks of the experiment
	 */
	public ArrayList<RCSTask> getTasks() {
		return experiment;
	}
}
