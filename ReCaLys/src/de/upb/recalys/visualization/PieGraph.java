package de.upb.recalys.visualization;

import java.util.ArrayList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import de.upb.recalys.helper.ResourceHandler;
import de.upb.recalys.model.RCSGraph;
import de.upb.recalys.model.RCSNode;
import de.upb.recalys.model.RCSPath;
import de.upb.recalys.model.RCSTask;
import de.upb.recalys.visualization.algorithms.MarkAllSimplePaths;

/**
 * The Class PieGraph implements a representation of a Pie Graph for a specific
 * ReCaPo experiment task.
 */
public class PieGraph extends AbstractRCSGraph {

	private int maxEdgeVisits = 0, maxNodeVisits = 0;
	private RCSTask currentTask;

	/* all attribute names and values of attributes */
	private final String EDGE_VISITS = "edgeVisits", NODE_VISITS = "nodeVisits", NOMINATED = "nominated",
			RIGHT_PATH = "rightPath", WRONG_PATH = "wrongPath", WENT_BACK = "wentBack", SKIPPED = "skipped",
			LOOP = "loop", BACK_ON_RIGHT_PATH = "backOnRightPath", SIMPLE_PATH = "simplePath", USER_PATH = "userPath";

	private final int MAX_EDGE_SIZE = 25, MIN_EDGE_SIZE = 2;
	private final int MAX_NODE_SIZE = 80, MIN_NODE_SIZE = 16;

	/**
	 * Instantiates a new pie graph.
	 */
	public PieGraph() {
		super("Pie Graph");
		this.addAttribute("ui.stylesheet", ResourceHandler.getURL("/stylesheets/PieGraph.css"));
		this.addAttribute("layout.quality", 4);
	}

	@Override
	public void init(RCSGraph rcsGraph) {
		super.init(rcsGraph);
		this.addAttribute("ui.stylesheet", ResourceHandler.getURL("/stylesheets/PieGraph.css"));
	}

	/**
	 * Import data from a {@link RCSTask}. This method imports all the data for
	 * the pie-values and sizes for the elements.
	 *
	 * @param task
	 *            the task
	 */
	public void analyseTask(RCSTask task) {
		this.currentTask = task;

		MarkAllSimplePaths masp = new MarkAllSimplePaths();
		masp.init(this, this.start, this.getNode(Integer.toString(currentTask.getTarget().getID())));
		masp.compute();

		ArrayList<RCSPath> paths = task.getPaths();

		for (RCSPath path : paths) {
			// System.out.println("new path:");

			/*
			 * increase the nominated count for the end node if the path is not
			 * skipped. If the path is skipped then increase the skipped count.
			 */
			Node endNode = this.getNode(Integer.toString(path.getEndNode().getID()));
			if (path.isSkipped()) {
				increaseNodeSkipped(endNode);
			} else {
				increaseNodeNominated(endNode);
			}

			// Special case with only one node in the path.
			if (path.size() == 1) {
				path.getNode(0);
				increaseNodeVisits(this.getNode(Integer.toString(path.getNode(0).getID())));
				// System.out.println("only 1 node in path");
				continue;
			}

			String sourceID, targetID = Integer.toString(path.getNode(0).getID());
			Node source, target = this.getNode(targetID);
			Edge currentEdge = null, lastEdge;
			increaseNodeVisits(target);
			ArrayList<RCSNode> nodelist = path.getPath();

			RCSNode node;
			for (int i = 1; i < nodelist.size(); i++) {
				node = nodelist.get(i);

				sourceID = targetID;
				targetID = Integer.toString(node.getID());

				source = target;
				target = this.getNode(targetID);

				increaseNodeVisits(target);
				// Loops should not be stored as the last edge because the next
				// step could be the way back.
				if (targetID.equals(sourceID)) {
					increaseNodeLoop(source);
					increaseEdgeVisits(source.getEdgeToward(target));
					continue;
				}

				lastEdge = currentEdge;
				currentEdge = target.getEdgeFrom(source);

				increaseEdgeVisits(currentEdge);

				if (currentEdge == null) {
					// paths that are not in the structure but are possible
					// through "Ebene höher" in ReCaPo
					increaseWentBack(source);
					// System.out.println("went back");
				} else if (lastEdge != null && target.equals(lastEdge.getSourceNode())) {
					// went back
					// System.out.println("went back");
					increaseWentBack(source);
				} else {
					// only paths that were chosen actively by the test person
					if (currentEdge.hasAttribute(SIMPLE_PATH)) {
						// went down the right path
						// System.out.println("went right");
						increaseWentRight(source);
					} else if (target.hasAttribute(SIMPLE_PATH) && !source.hasAttribute(SIMPLE_PATH)) {
						// went back on a potential correct path from a node
						// that was not on a simple path
						// System.out.println("went back on right path");
						increaseBackOnRightPath(source);
					} else {
						// went down the wrong path
						// System.out.println("wrong path");
						increaseWentWrong(source);
					}
				}
			}
		}

		setEdgeSizes();
		setNodeSizes();
		setPieValues();
		hideUnusedElements();

	}

	/**
	 * Adds the edge labels for a specific path and ui.classes for the used
	 * nodes and edges to be visualized in a specific way. The label consists of
	 * the path indexes and the comprehending latencies.
	 *
	 * @param rcsPath
	 *            the rcs path
	 */
	public void addPathInfo(RCSPath rcsPath) {
		removeLastUserPath();

		ArrayList<RCSNode> path = rcsPath.getPath();
		ArrayList<Integer> latencies = rcsPath.getLatencies();
		if (path.size() <= 1) {
			GraphTools.addUIClass(this.start, USER_PATH);
		} else {
			Node currentNode = this.getNode(Integer.toString(path.get(0).getID()));

			Node lastNode;
			Edge currentEdge;

			String stayIndices = "";
			String stayTimes = "";

			for (int i = 1; i < path.size(); i++) {
				lastNode = currentNode;
				currentNode = this.getNode(Integer.toString(path.get(i).getID()));
				GraphTools.addUIClass(currentNode, USER_PATH);

				boolean back;

				if (currentNode.equals(lastNode)) {
					back = false;
					stayIndices += i + "+";
					stayTimes += (latencies.get(i - 1) / 1000) + "+";
					continue;
				} else if (lastNode.hasEdgeToward(currentNode)) {
					back = false;
					currentEdge = lastNode.getEdgeToward(currentNode);
				} else if (currentNode.hasEdgeToward(lastNode)) {
					back = true;
					currentEdge = currentNode.getEdgeToward(lastNode);
				} else {
					return;
				}

				String currentLabel = currentEdge.getAttribute("ui.label") != null
						? currentEdge.getAttribute("ui.label") : "";
				if (!currentLabel.isEmpty()) {
					currentLabel += ", ";
				}

				currentLabel += stayIndices + i + (back ? "z" : "") + "(" + stayTimes + latencies.get(i - 1) / 1000
						+ "s)";

				currentEdge.setAttribute("ui.label", currentLabel);

				GraphTools.addUIClass(currentEdge, USER_PATH);

				stayTimes = "";
				stayIndices = "";
			}
		}
	}

	/**
	 * Removes the edge labels from all edges and ui.classes from the last user
	 * path that was selected.
	 */
	public void removeLastUserPath() {
		for (Edge edge : this.getEachEdge()) {
			edge.setAttribute("ui.label", "");
			GraphTools.removeUIClass(edge, USER_PATH);
		}
	}

	/**
	 * Sets the edge sizes depending on the number of visits for all edges.
	 */
	private void setEdgeSizes() {
		int visits;
		double size;
		for (Edge edge : this.getEachEdge()) {
			if (edge.hasAttribute(EDGE_VISITS)) {
				visits = edge.getAttribute(EDGE_VISITS);
				size = Math.max(MIN_EDGE_SIZE, 2 * (Math.ceil(((double) visits * MAX_EDGE_SIZE) / maxEdgeVisits / 2)));

				edge.addAttribute("ui.style", "size: " + size + ";");
			}
		}
	}

	/**
	 * Sets the node sizes depending on the number of visits for all nodes.
	 */
	private void setNodeSizes() {
		int visits;
		double size;
		for (Node node : this.getEachNode()) {
			if (node.hasAttribute(NODE_VISITS)) {
				visits = node.getAttribute(NODE_VISITS);
				size = Math.max(MIN_NODE_SIZE, 5 * (Math.ceil(((double) visits * MAX_NODE_SIZE) / maxNodeVisits / 5)));

				node.addAttribute("ui.style", "size: " + size + ";");
				if (size < MIN_NODE_SIZE + 5) {
					node.addAttribute("layout.weight", (1.5 - size / (MAX_NODE_SIZE * 2)));
				}
			}
		}
	}

	/**
	 * Hide unused elements.
	 */
	private void hideUnusedElements() {
		hideLoops();
		for (Edge edge : this.getEachEdge()) {
			if (!edge.hasAttribute(EDGE_VISITS)) {
				edge.addAttribute("ui.hide");
			}
		}

		for (Node node : this.getEachNode()) {
			if (!node.hasAttribute(NODE_VISITS)) {
				node.addAttribute("ui.hide");
			}
		}

	}

	/**
	 * Sets the pie values for all nodes.
	 */
	private void setPieValues() {
		for (Node node : this.getEachNode()) {
			if (node.hasAttribute(NODE_VISITS)) {
				int nodeVisits = node.getAttribute(NODE_VISITS);

				double rightPath = getIntAttributeAsDouble(node, RIGHT_PATH) / nodeVisits;
				double wrongPath = getIntAttributeAsDouble(node, WRONG_PATH) / nodeVisits;
				double wentBack = getIntAttributeAsDouble(node, WENT_BACK) / nodeVisits;
				double backOnRightPath = getIntAttributeAsDouble(node, BACK_ON_RIGHT_PATH) / nodeVisits;
				double loop = getIntAttributeAsDouble(node, LOOP) / nodeVisits;
				double nominated = getIntAttributeAsDouble(node, NOMINATED) / nodeVisits;
				double skipped = getIntAttributeAsDouble(node, SKIPPED) / nodeVisits;

				node.setAttribute("ui.pie-values", rightPath, wrongPath, wentBack, backOnRightPath, loop, nominated,
						skipped);
			}
		}
	}

	/**
	 * Gets an int attribute as double from the specified node.
	 *
	 * @param node
	 *            the node
	 * @param attribute
	 *            the attribute name
	 * @return the int attribute as double
	 */
	private double getIntAttributeAsDouble(Node node, String attribute) {
		if (node.hasAttribute(attribute)) {
			int value = node.getAttribute(attribute);
			return (double) value;
		} else {
			return 0.0;
		}
	}

	/**
	 * Increase {@code edgeVisits} attribute for the specified edge.
	 *
	 * @param edge
	 *            the edge
	 * @return the new value
	 */
	private int increaseEdgeVisits(Edge edge) {
		int edgeVisits = increaseIntAttribute(edge, EDGE_VISITS);

		if (edgeVisits > maxEdgeVisits) {
			maxEdgeVisits = edgeVisits;
		}
		return edgeVisits;
	}

	/**
	 * Increase {@code nodeVisits} attribute for the specified node.
	 *
	 * @param node
	 *            the node
	 * @return the new value
	 */
	private int increaseNodeVisits(Node node) {
		int nodeVisits = increaseIntAttribute(node, NODE_VISITS);

		if (nodeVisits > maxNodeVisits) {
			maxNodeVisits = nodeVisits;
		}
		return nodeVisits;
	}

	/**
	 * Increase {@code nodenominated} attribute for the specified node.
	 *
	 * @param node
	 *            the node
	 * @return the new value
	 */
	private int increaseNodeNominated(Node node) {
		int nominations = increaseIntAttribute(node, NOMINATED);

		return nominations;
	}

	/**
	 * Increase {@code nodeSkipped} attribute for the specified node.
	 *
	 * @param node
	 *            the node
	 * @return the new value
	 */
	private int increaseNodeSkipped(Node node) {
		int skipped = increaseIntAttribute(node, SKIPPED);

		return skipped;
	}

	/**
	 * Increase {@code nodeLoop} attribute for the specified node.
	 *
	 * @param node
	 *            the node
	 * @return the new value
	 */
	private int increaseNodeLoop(Node node) {
		int loop = increaseIntAttribute(node, LOOP);

		return loop;
	}

	/**
	 * Increase {@code wentBack} attribute for the specified node.
	 *
	 * @param node
	 *            the node
	 * @return the new value
	 */
	private int increaseWentBack(Node node) {
		int wentBack = increaseIntAttribute(node, WENT_BACK);

		return wentBack;
	}

	/**
	 * Increase {@code wentRight} attribute for the specified node.
	 *
	 * @param node
	 *            the node
	 * @return the new value
	 */
	private int increaseWentRight(Node node) {
		int rightPath = increaseIntAttribute(node, RIGHT_PATH);

		return rightPath;
	}

	/**
	 * Increase {@code wentWrong} attribute for the specified node.
	 *
	 * @param node
	 *            the node
	 * @return the new value
	 */
	private int increaseWentWrong(Node node) {
		int wrongPath = increaseIntAttribute(node, WRONG_PATH);

		return wrongPath;
	}

	/**
	 * Increase {@code backOnRightPath} attribute for the specified node.
	 *
	 * @param node
	 *            the node
	 * @return the new value
	 */
	private int increaseBackOnRightPath(Node node) {
		int backOnRightPath = increaseIntAttribute(node, BACK_ON_RIGHT_PATH);

		return backOnRightPath;
	}
}
