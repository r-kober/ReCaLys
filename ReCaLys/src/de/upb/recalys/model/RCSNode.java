/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.upb.recalys.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class defines the nodes that represent the pages in a website structure.
 * 
 * @author danielbrumberg
 * @version 1.0
 */
public class RCSNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4673374729212236698L;
	private boolean explored;
	private boolean isTarget = false;
	private int degree = -1;
	private int distRating = 0;
	private int[] distRatingsPerTask;
	private int id;
	private int level;
	private int leavesCount = -1;
	private int simulatedVisits;
	private int visits;
	private int visitsOnOptimalPaths;
	private int minimalLatency = -1;
	private String label;
	private ArrayList<RCSNode> children;
	private ArrayList<RCSNode> parents;
	private ArrayList<RCSNode> levelChildren;
	private ArrayList<RCSNode> levelParents;
	private ArrayList<Integer> latencies;
	private ArrayList<Integer>[] latenciesPerTask;

	private HashMap<Integer, Integer> distances;

	/**
	 * Constructor: Creates a new Node
	 * 
	 * @param id
	 *            individual id of the node
	 * @param label
	 *            label of the node
	 */
	public RCSNode(int id, String label) {
		this.id = id;
		this.label = label;
		level = -1; // level not set
		explored = false;
		children = new ArrayList<RCSNode>();
		parents = new ArrayList<RCSNode>();
		levelChildren = new ArrayList<RCSNode>();
		levelParents = new ArrayList<RCSNode>();
		latencies = new ArrayList<Integer>();

		distances = new HashMap<>();
	}

	/**
	 * Returns this node's id
	 * 
	 * @return id
	 */
	public int getID() {
		return id;
	}

	/**
	 * Returns this node's label
	 * 
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns this node's list of children
	 * 
	 * @return list of children
	 */
	public ArrayList<RCSNode> getChildren() {
		return children;
	}

	/**
	 * Returns this node's list of parents
	 * 
	 * @return list of parents
	 */
	public ArrayList<RCSNode> getParents() {
		return parents;
	}

	/**
	 * Returns this node's list of children in the levelgraph
	 * 
	 * @return list of children
	 */
	public ArrayList<RCSNode> getLevelChildren() {
		return levelChildren;
	}

	/**
	 * Returns this node's list of parents in the levelgraph
	 * 
	 * @return list of parents
	 */
	public ArrayList<RCSNode> getLevelParents() {
		return levelParents;
	}

	/**
	 * Adds a child to the node's children list
	 * 
	 * @param child
	 *            child that shall be added
	 * @return true, if adding was possible, else false
	 */
	public boolean addChildren(RCSNode child) {
		return children.add(child);
	}

	/**
	 * Adds a parent to the node's children list
	 * 
	 * @param parent
	 *            parent that shall be added
	 * @return true, if adding was possible, else false
	 */
	public boolean addParent(RCSNode parent) {
		return parents.add(parent);
	}

	/**
	 * Adds a child to the node's levelgraph children list
	 * 
	 * @param child
	 *            child that shall be added
	 * @return true, if adding was possible, else false
	 */
	public boolean addLevelChildren(RCSNode child) {
		return levelChildren.add(child);
	}

	/**
	 * Adds a parent to the node's levelgraph children list
	 * 
	 * @param parent
	 *            parent that shall be added
	 * @return true, if adding was possible, else false
	 */
	public boolean addLevelParent(RCSNode parent) {
		return levelParents.add(parent);
	}

	/**
	 * Returns if the node was already explored
	 * 
	 * @return is explored
	 */
	public boolean isExplored() {
		return explored;
	}

	/**
	 * Sets the boolean explored to true or false
	 * 
	 * @param e
	 *            explored
	 */
	public void setExplored(boolean e) {
		explored = e;
	}

	/**
	 * Returns true if the node has got more than one parent
	 * 
	 * @return isMultipleLinked
	 */
	public boolean isMultipleLinked() {
		if (parents.size() > 1)
			return true;
		else
			return false;
	}

	/**
	 * Sets the level of this node in the levelgraph
	 * 
	 * @param level
	 *            level of this node in the levelgraph
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Returns the level of this node
	 * 
	 * @return level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Returns this node's degree. The degree of a node is the number of
	 * successors the node has in the levelgraph plus himself.
	 * 
	 * @return degree
	 */
	public int getDegree() {
		if (degree > -1)
			return degree;
		else if (levelChildren.size() > 0) {
			int tempDegree = 0;
			for (RCSNode child : getLevelChildren()) {
				tempDegree += child.getDegree();
			}
			degree = tempDegree + 1;
			return degree;
		} else
			return 1;
	}

	/**
	 * Returns the number of leaves under this node in the levelgraph. If the
	 * node itself is a leaf the value will be 1.
	 * 
	 * @return leavesCount
	 */
	public int getLeavesCount() {
		if (leavesCount > -1)
			return leavesCount;
		else if (levelChildren.size() > 0) {
			int temp = 0;
			for (RCSNode child : getLevelChildren()) {
				temp += child.getLeavesCount();
			}
			leavesCount = temp;
			return leavesCount;
		} else if (isLeaf())
			return 1;
		else
			return 0;
	}

	/**
	 * Returns the coverage of this node in this experiment
	 * 
	 * @return coverage if this node
	 */
	public double getCoverage() {
		return (double) visitsOnOptimalPaths / (double) getLeavesCount();
	}

	/**
	 * Returns the simulated coverage of this node in this experiment
	 * 
	 * @return simulated coverage if this node
	 */
	public double getSimulatedCoverage() {
		return (double) simulatedVisits / (double) getLeavesCount();
	}

	/**
	 * Returns wether this node is a leaf in the levelgraph
	 * 
	 * @return true if node is a leaf, false else
	 */
	public boolean isLeaf() {
		if (levelChildren.isEmpty())
			return true;
		else
			return false;
	}

	/**
	 * Computes the minimal latency in milliseconds an average user would need
	 * to choose the next link at this node.
	 * 
	 * @param time
	 *            milliseconds the user needs to think
	 */
	public void computeMinimalLatency(int time) {
		int labelWords = 0;
		int wordsPerSecond = 2;
		int timeToThink = time;

		for (RCSNode child : getChildren()) {
			String s = child.getLabel();
			char[] chars = s.toCharArray();
			labelWords++;
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] == ' ' || chars[i] == '_')
					if ((i + 1) < chars.length)
						if (chars[i + 1] != ' ' && chars[i + 1] != '_')
							labelWords++;
			}
		}

		minimalLatency = (labelWords / wordsPerSecond) * 1000 + timeToThink * children.size();

	}

	/**
	 * Returns the minimal latency in milliseconds an average user would need to
	 * choose the next link at this node
	 * 
	 * @return minimalLatency
	 */
	public int getMinimalLatency() {
		return minimalLatency;
	}

	/**
	 * Increase the counter for visits of this node
	 */
	public void visit() {
		visits++;
	}

	/**
	 * Returns the number of real visits of this node over all paths
	 * 
	 * @return number of visits
	 */
	public int getVisits() {
		return visits;
	}

	/**
	 * Increase the counter for visits on optimal paths of this node
	 */
	public void visitsOnOptimalPath() {
		visitsOnOptimalPaths++;
	}

	/**
	 * Returns the number of visits on optimal paths of this node over all paths
	 * 
	 * @return number of visits on optimal paths of this node
	 */
	public int getVisitsOnOptimalPath() {
		return visitsOnOptimalPaths;
	}

	/**
	 * Increases the current DistRating by 0, 1 or 2
	 * 
	 * @param value
	 *            the value the DistRating of the node is increased by
	 */
	public void increaseDistRating(int value) {
		if (value == 0 || value == 1 || value == 2)
			distRating += value;
		else
			System.err.println("Increase of DistRating not valid");
	}

	/**
	 * Adds the latency of the appearance of this node on an certain path to the
	 * ArrayList of latencies
	 * 
	 * @param latency
	 *            latency that will be added in milliseconds
	 * @return true, if adding was successfull; false, if not
	 */
	public boolean addLatency(int latency) {
		return latencies.add(new Integer(latency));
	}

	/**
	 * Gives the malus of this node
	 * 
	 * @return malus of this node
	 */
	public int getMalus() {
		return distRating * degree;
	}

	/**
	 * Return the LatencyWeight of this node where the LatencyWeight is the
	 * median over all latency over all appearances of this nodes in all paths
	 * 
	 * @return latency-weight
	 */
	public int getLatencyWeight() {
		if (latencies.size() == 0)
			return 0;
		int middle = latencies.size() / 2;
		if (latencies.size() % 2 == 1)
			return latencies.get(middle);
		else
			return (latencies.get(middle - 1) + latencies.get(middle)) / 2;
	}

	/**
	 * Sets the node as a target node for a task
	 * 
	 * @param target
	 *            true, if node is target; false else
	 */
	public void setTarget(boolean target) {
		isTarget = target;
	}

	/**
	 * Returns if the node is a target for a task
	 * 
	 * @return isTarget
	 */
	public boolean isTarget() {
		return isTarget;
	}

	/**
	 * Sets the count of simulated visits
	 * 
	 * @param visits
	 *            the new count of simulated visits
	 */
	public void setSimulatedVisits(int visits) {
		simulatedVisits = visits;
	}

	/**
	 * Returns the number of simulated visits
	 * 
	 * @return number of simulated visits
	 */
	public int getSimulatedVisits() {
		return simulatedVisits;
	}

	/**
	 * Returns the label of the node as a String
	 * 
	 * @return label
	 */
	public String toString() {
		return label;
	}

	/**
	 * Resets the properties of the node: - explored and isTarget to false -
	 * distRating, simulatedVisits, visits, visitsOnOptimalPaths t0 0 -
	 * minimalLatency to -1
	 */
	public void resetProperties() {
		explored = false;
		isTarget = false;
		distRating = 0;
		simulatedVisits = 0;
		visits = 0;
		visitsOnOptimalPaths = 0;
		minimalLatency = -1;
		latencies = new ArrayList<Integer>();
	}

	/**
	 * Resets the properties of the node: - explored to false - distRating,
	 * simulatedVisits, visits, visitsOnOptimalPaths t0 0 - minimalLatency to -1
	 */
	public void resetAnalysis() {
		explored = false;
		distRating = 0;
		simulatedVisits = 0;
		visits = 0;
		visitsOnOptimalPaths = 0;
		minimalLatency = -1;
		latencies = new ArrayList<Integer>();
	}

	/**
	 * Returns true if the given node is a child of this node
	 * 
	 * @param node
	 *            for that we want to check, if it is a child of this node
	 * @return isChild
	 */
	public boolean isChild(RCSNode node) {
		for (RCSNode n : getChildren()) {
			if (node.equals(n))
				return true;
		}
		return false;
	}

	/**
	 * Returns the dist-rating of this node
	 * 
	 * @return distRating
	 */
	public int getDistRating() {
		return distRating;
	}

	/**
	 * Initializes the per-task-ratings
	 * 
	 * @param countTasks
	 *            number of tasks
	 */
	public void initRatings(int countTasks) {
		distRatingsPerTask = new int[countTasks];
		latenciesPerTask = new ArrayList[countTasks];

		for (int i = 0; i < countTasks; i++) {
			distRatingsPerTask[i] = 0;
			latenciesPerTask[i] = new ArrayList();
		}
	}

	/**
	 * Adds a visit for this node on a certain path. A node is visited if it is
	 * on a path that belongs to this task.
	 * 
	 * @param taskID
	 *            id of the task
	 * @param distValue
	 *            value the visits should be increased by
	 * @param latency
	 *            latency of this visit
	 */
	public void visitOnTask(int taskID, int distValue, int latency) {
		distRatingsPerTask[taskID] += distValue;
		latenciesPerTask[taskID].add(latency);
	}

	/**
	 * Returns this node's DistRating for a specific task
	 * 
	 * @param taskID
	 *            id of the task
	 * @return DistRating
	 */
	public int getDistRatingForTask(int taskID) {
		return distRatingsPerTask[taskID];
	}

	/**
	 * Returns this node's Latency Weight for a specific task
	 * 
	 * @param taskID
	 *            id of the task
	 * @return latency weight
	 */
	public double getLatencyWeightForTask(int taskID) {
		ArrayList<Integer> latencies = latenciesPerTask[taskID];
		if (latencies.size() == 0)
			return 0;
		int middle = latencies.size() / 2;
		if (latencies.size() % 2 == 1)
			return latencies.get(middle);
		else
			return (latencies.get(middle - 1) + latencies.get(middle)) / 2;
	}
}
