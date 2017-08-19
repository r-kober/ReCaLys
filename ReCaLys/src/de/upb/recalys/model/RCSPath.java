package de.upb.recalys.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class defines a path a user has gone during an experiment
 * 
 * @author danielbrumberg
 * @version 1.0
 */
public class RCSPath implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -850282812961419262L;
	boolean finalized;
	boolean successfull;
	boolean invalid;
	/**
	 * true if the corresponding task was skipped by the tester. false
	 * otherwise.
	 */
	boolean skipped;
	int minimumDistance;
	int beginOfSystematicSearching;
	RCSGraph graph;
	RCSNode source;
	RCSNode target;
	RCSTask task;
	String userID;

	String duration;

	ArrayList<RCSNode> path;
	ArrayList<Integer> latencies;

	/**
	 * Constructor: Creates a new path
	 * 
	 * @param graph
	 *            graph the path belongs to
	 * @param source
	 *            source node of this path
	 * @param target
	 *            target node of this path
	 */
	public RCSPath(RCSGraph graph, RCSNode source, RCSNode target) {
		this.graph = graph;
		successfull = false;
		finalized = false;
		invalid = false;
		skipped = false;
		this.source = source;
		this.target = target;
		path = new ArrayList<RCSNode>();
		// path.add(this.source);
		latencies = new ArrayList<Integer>();
		beginOfSystematicSearching = -1;
	}

	/**
	 * Adds a node to this path and the time the user needed to think at this
	 * position
	 * 
	 * @param node
	 *            node that shall be added
	 * @param time
	 *            latency at that node
	 * @return true, if adding was successfull, else false
	 */
	public boolean addNode(RCSNode node, int time) {
		if (!finalized) {
			latencies.add(new Integer(time));
			return path.add(node);
		} else
			return false;
	}

	/**
	 * Returns the node at the position index in the path
	 * 
	 * @param index
	 * @return node
	 */
	public RCSNode getNode(int index) {
		return path.get(index);
	}

	/**
	 * Finalize the path so that no more nodes can be added
	 */
	public void setFinalized() {
		// Sets last latency to zero because there cannot be a latency
		latencies.set(latencies.size() - 1, 0);
		finalized = true;
	}

	/**
	 * Returns if this is a successfull path. (A successfull path reaches the
	 * target node.)
	 * 
	 * @return isSuccessfull
	 */
	public boolean isSuccessfull() {
		if (!finalized || invalid)
			return false;
		else
			return (target == path.get(path.size() - 1));
	}

	/**
	 * Analyses the path
	 */
	public void analyse() {
		boolean stop = false;
		int distFromHere;
		int distFromNext;
		RCSNode node;
		HashSet<RCSNode> nodesOnOptimalPaths = task.getNodesOnOptimalPaths();
		for (int i = 0; i < path.size() - 1 && !stop; i++) {
			if (i == beginOfSystematicSearching)
				stop = true;
			else {
				node = path.get(i);
				if (nodesOnOptimalPaths.contains(node)) {
					node.visit();
					distFromHere = graph.getDistance(node.getID(), target.getID());
					distFromNext = graph.getDistance(path.get(i + 1).getID(), target.getID());

					if (distFromNext == -1)
						distFromNext = distFromHere + 1;
					else if ((distFromHere + 1) < distFromNext)
						distFromNext = distFromHere + 1;

					node.increaseDistRating(distFromNext - distFromHere + 1);
					node.addLatency(latencies.get(i));
					node.visitOnTask(task.getID(), distFromNext - distFromHere + 1, latencies.get(i));
				}
			}
		}
	}

	/**
	 * This method is used to detect systematic searching on this path
	 * 
	 * @param nodesToInspect
	 * @param maxProblemRate
	 * @param maxReturnRate
	 */
	public void detectSystematicSearching(int nodesToInspect, double maxProblemRate, double maxReturnRate) {

		if (path.size() < 2 * graph.getDistance(path.get(0).getID(), target.getID()))
			return;
		boolean stop = false;
		String detection = "";
		HashSet<RCSNode> incidences;
		for (int i = nodesToInspect; i < path.size() && !stop; i++) {
			double averageLatency = 0;
			double averageMinLatency = 0;
			int problems = 0;
			int returns = 0;
			double problemRate;
			double returnRate;
			incidences = new HashSet<RCSNode>();
			for (int j = i - nodesToInspect + 1; j < nodesToInspect; j++) {
				averageLatency += latencies.get(j);
				averageMinLatency += path.get(j).getMinimalLatency();
				if (graph.getDistance(path.get(j).getID(), target.getID()) <= graph.getDistance(path.get(j + 1).getID(),
						target.getID()))
					problems++;
				if (!incidences.add(path.get(j))) {
					returns++;
				}

			}
			averageLatency = (double) averageLatency / nodesToInspect;
			averageMinLatency = (double) averageMinLatency / nodesToInspect;
			problemRate = (double) problems / nodesToInspect;
			returnRate = (double) returns / nodesToInspect;
			if (problemRate > maxProblemRate && returnRate > maxReturnRate)

				if (averageLatency <= averageMinLatency) {
					beginOfSystematicSearching = i;
					invalid = true;
					stop = true;
					detection = "*********************************\n";
					detection += "Detection of Systematic Searching:\n";
					detection += "User: " + userID + ", Target: " + target.getLabel() + "\n";
					detection += "at Position: " + i + "(" + path.get(i) + ")\n";
					detection += "averageMinLatency: " + averageMinLatency + "\n";
					detection += "averageLatency: " + averageLatency + "\n";
					detection += "problemRate: " + problemRate;
					detection += ", returnRate: " + returnRate + "" + returns + "\n";
					detection += "*********************************\n";
					graph.addDetectionOfSystematicSearching(detection);
				}
		}
	}

	/**
	 * Set the task this path belongs to
	 * 
	 * @param task
	 *            the task that belongs to this path
	 */
	public void setTask(RCSTask task) {
		this.task = task;
	}

	/**
	 * Gets the corresponding task for this path.
	 *
	 * @return task
	 */
	public RCSTask getTask() {
		return task;
	}

	/**
	 * Sets the user this paths belongs to
	 * 
	 * @param user
	 */
	public void setUser(String user) {
		userID = user;
	}

	/**
	 * Returns the user this path belongs to
	 * 
	 * @return userID
	 */
	public String getUser() {
		return userID;
	}

	/**
	 * Sets the duration as a String from the imported XML in the format:
	 * "HH:MM:SS".
	 *
	 * @param duration
	 *            the new duration.
	 */
	public void setDuration(String duration) {
		this.duration = duration;
	}

	public ArrayList<Integer> getLatencies() {
		return latencies;
	}

	public ArrayList<RCSNode> getPath() {
		return this.path;
	}

	/**
	 * Returns the last node in the path
	 *
	 * @return the last node
	 */
	public RCSNode getEndNode() {
		return path.get(path.size() - 1);
	}

	/**
	 * Returns if the path is invalid because of the detection of systematic
	 * searching
	 * 
	 * @return invalid
	 */
	public boolean isInvalid() {
		return invalid;
	}

	/**
	 * Checks if is skipped.
	 *
	 * @return true, if is skipped
	 */
	public boolean isSkipped() {
		return skipped;
	}

	/**
	 * Sets skipped attribute.
	 *
	 * @param skipped
	 * 
	 */
	public void setSkipped(boolean skipped) {
		this.skipped = skipped;
	}

	/**
	 * Returns the size of the path
	 * 
	 * @return size of path
	 */
	public int size() {
		return path.size();
	}

	public int getDurationInSeconds() {
		int dur = 0;
		String[] time = duration.split(":");
		int mult = 3600;
		for (String d : time) {
			dur += Integer.parseInt(d) * mult;
			mult /= 60;
		}
		return dur;
	}

	@Override
	public String toString() {
		// StringBuffer output = new StringBuffer();
		// for (RCSNode node : path) {
		// output.append(node.getID());
		// output.append("->");
		// }
		// output.delete(output.lastIndexOf("->"), output.length());
		// return output.toString();

		return userID + " - " + (path.size() - 1) + (path.size() == 2 ? " Schritt - " : " Schritte - ") + duration
				+ " - " + (isSuccessfull() ? "erfolgreich" : isSkipped() ? "übersprungen" : "nicht erfolgreich");
	}
}
