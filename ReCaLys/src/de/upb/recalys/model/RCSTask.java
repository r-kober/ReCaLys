package de.upb.recalys.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * This class defines a task in an rcs-experiment
 * 
 * @author danielbrumberg
 * @version 1.0
 */
public class RCSTask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8428284891658897938L;
	private double solutionRate;
	private int id;
	private RCSGraph graph;
	private RCSNode source;
	private RCSNode target;
	private ArrayList<RCSPath> paths;
	private HashSet<RCSNode> nodesOnOptimalPaths;
	private String alternativeTargetName;

	/**
	 * Constructor: Creates a new task
	 * 
	 * @param graph
	 *            website structure of the tasks
	 * @param source
	 *            source of the task
	 * @param target
	 *            target of the task
	 * @param alt
	 *            alternative target name
	 * @param id
	 *            id of the task
	 */
	public RCSTask(RCSGraph graph, RCSNode source, RCSNode target, String alt, int id) {
		solutionRate = -1;
		this.graph = graph;
		this.source = source;
		this.target = target;
		this.target.setTarget(true);
		this.id = id;
		alternativeTargetName = alt;
		paths = new ArrayList<RCSPath>();
	}

	/**
	 * Adds a path to the task
	 * 
	 * @param p
	 *            path that shall be added
	 * @return true, if adding was successfull, else false
	 */
	public boolean addPath(RCSPath p) {
		if (p.target == target) {
			p.setTask(this);
			return paths.add(p);
		} else
			return false;
	}

	/**
	 * Returns the alternative name of the target node (e.g. if there was a
	 * description in the task for the wanted item instead of only its name)
	 */
	public String getAlternativeTargetName() {
		return alternativeTargetName;
	}

	public ArrayList<RCSPath> getPaths() {
		return paths;
	}

	/**
	 * Returns the solution-rate for this task
	 * 
	 * @return solution-rate
	 */
	public double getSolutionRate() {
		if (solutionRate < 0) {
			int successfullPaths = 0;
			for (RCSPath path : paths) {
				if (path.isSuccessfull()) {
					successfullPaths++;
				}
			}
			if (paths.size() > 0) {
				solutionRate = successfullPaths / (double) paths.size();
			} else
				solutionRate = 0;
		}
		return solutionRate;
	}

	/**
	 * Detects all nodes on all optimal paths for this task by using an adapted
	 * BFS-algorithm.
	 */
	public void detectNodesOnOptimalPaths() {
		nodesOnOptimalPaths = new HashSet<RCSNode>();
		int distFromNode;
		int distFromChild;
		RCSNode node;
		LinkedList queue = new LinkedList();
		queue.offer(source);
		source.visitsOnOptimalPath();
		nodesOnOptimalPaths.add(source);
		while (queue.size() > 0) {
			node = (RCSNode) queue.removeFirst();
			distFromNode = graph.getDistance(node.getID(), target.getID());
			for (RCSNode child : node.getLevelChildren()) {
				distFromChild = graph.getDistance(child.getID(), target.getID());
				if (distFromChild != -1 && distFromNode == (distFromChild + 1)) {
					queue.offer(child);
					nodesOnOptimalPaths.add(child);
					child.visitsOnOptimalPath();
				}
			}
		}
	}

	/**
	 * Return the set of nodes that lie on all optimal paths for this task
	 * 
	 * @return the set of nodes on all optimal paths for this task
	 */
	public HashSet getNodesOnOptimalPaths() {
		return nodesOnOptimalPaths;
	}

	/**
	 * Returns the target of the task
	 * 
	 * @return target
	 */
	public RCSNode getTarget() {
		return target;
	}

	/**
	 * Starts the analysis of all paths belonging to this task
	 */
	public void analysePaths(int nodesToInspect, double maxProblemRate, double maxReturnRate) {
		for (RCSPath path : paths) {
			path.detectSystematicSearching(nodesToInspect, maxProblemRate, maxReturnRate);
			path.analyse();
		}
	}

	/**
	 * Returns the number of invalid paths belonging to this task
	 * 
	 * @return numberOfInvalidPaths
	 */
	public int getNumberOfInvalidPaths() {
		int i = 0;
		for (RCSPath path : paths) {
			if (path.isInvalid())
				i++;
		}
		return i;
	}

	/**
	 * Resets the analysis
	 */
	public void resetAnalysis() {
		solutionRate = -1;
	}

	/**
	 * Returns the id of this task
	 * 
	 * @return id
	 */
	public int getID() {
		return id;
	}

	/**
	 * Gets the average time taken for the Task.
	 *
	 * @return the average time taken.
	 */
	public int getAverageTimeTaken() {
		if (paths.size() == 0) {
			return 0;
		}

		int avgTime = 0;
		for (RCSPath path : paths) {
			avgTime += path.getDurationInSeconds();
		}
		return avgTime / paths.size();
	}
	
	/**
	 * Gets the average path length for the Task.
	 *
	 * @return the average path length.
	 */
	public int getAveragePathLength(){
		if (paths.size() == 0) {
			return 0;
		}
		int avgLength = 0;
		for (RCSPath path : paths) {
			avgLength += path.size();
		}
		return avgLength / paths.size();
	}

	/**
	 * Returns a String representation of this class.
	 * 
	 * @return Taskname (solutionrate % - averageTime s)
	 */
	@Override
	public String toString() {
		NumberFormat n = NumberFormat.getInstance();
		n.setMaximumFractionDigits(2);
		
		
		return alternativeTargetName + " (Lösungsquote: " + n.format(solutionRate * 100) + "% - \u00D8-Zeit: "
				+ getAverageTimeTaken() + "s - \u00D8-Länge: " +getAveragePathLength()+ " Schritte)";
	}

}
