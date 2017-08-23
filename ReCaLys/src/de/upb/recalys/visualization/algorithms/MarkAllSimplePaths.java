package de.upb.recalys.visualization.algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Implementation of a algorithm to mark all edges and nodes that would be on a
 * simple path and store these in a subgraph.
 * 
 * @see Algorithm
 * @version 1.0
 * @author Roman Kober
 */
public class MarkAllSimplePaths implements Algorithm {

	Graph graph;
	Node start, target;

	Node[] parent;
	Iterator<Edge>[] iterator;
	int depth[];
	Node next;
	int maxDepth;

	LinkedList<Edge> currentPath;
	LinkedList<Node> nodesOnCurrentPath;

	private final String VISITED = "visited", SIMPLE_PATH = "simplePath", CURRENT_TARGET = "currentTarget",
			BACKWARDS_EDGE = "backwardsEdge";

	/**
	 * {@inheritDoc org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)}
	 * <br>
	 * This init method sets the start to the node with index 0 and the target to
	 * the Node with the index |V|-1.
	 *
	 * @param graph
	 *            the graph
	 */
	@Override
	public void init(Graph graph) {
		this.init(graph, graph.getNode(0), graph.getNode(graph.getNodeCount() - 1));
	}

	/**
	 * This method should be used instead of {@link MarkAllSimplePaths#init(Graph)}.
	 * The Difference is that you can determine the start and target yourself.
	 * 
	 * @see MarkAllSimplePaths#init(Graph)
	 *
	 * @param graph
	 *            the graph
	 * @param start
	 *            the start node
	 * @param target
	 *            the target node
	 */
	public void init(Graph graph, Node start, Node target) {
		this.graph = graph;
		this.start = start;
		setTarget(target);

		// System.out.println("start: " + start + "\ttarget: " + target);
	}

	@Override
	public void compute() {
		resetNodesAndEdges();

		currentPath = new LinkedList<>();
		nodesOnCurrentPath = new LinkedList<>();
		masp(start);

		setNodeAndEdgeClasses(graph);
	}

	/**
	 * A recursive implementation of Depth First Search. The algorithm searches
	 * there for all paths to the {@link MarkAllSimplePaths#target}. This result is
	 * stored in a subgraph that contains only the edges and nodes that would be in
	 * a simple path. After this {@link MarkAllSimplePaths#checkBackwardsEdges()}
	 * should be called.
	 *
	 * @param node
	 *            the current node that the algorithm starts from
	 */
	private void masp(Node node) {
		node.addAttribute(VISITED);
		nodesOnCurrentPath.add(node);
		for (Edge edge : node.getEachLeavingEdge()) {
			Node endNode = edge.getTargetNode();
			currentPath.add(edge);

			// System.out.println("(" + edge.getSourceNode().getAttribute("ui.label") + "->"
			// + edge.getTargetNode().getAttribute("ui.label") + ") ");

			if (!edge.isLoop()) {
				if (endNode.hasAttribute(CURRENT_TARGET)
						|| (endNode.hasAttribute(SIMPLE_PATH) && !nodesOnCurrentPath.contains(endNode))) {
					// System.out.println(SIMPLE_PATH);
					// found new simple path
					endNode.addAttribute(SIMPLE_PATH);
					for (Edge edgeOnPath : currentPath) {
						edgeOnPath.addAttribute(SIMPLE_PATH);
						edgeOnPath.getSourceNode().addAttribute(SIMPLE_PATH);
					}
				} else if (nodesOnCurrentPath.contains(endNode)) {
					// System.out.println("later");
					// save backwards edge for later
					for (Iterator<Edge> iterator = currentPath.descendingIterator(); iterator.hasNext();) {
						Edge bEdge = (Edge) iterator.next();
						// System.out.println(bEdge);
						if (bEdge.getSourceNode().equals(endNode)) {
							// System.out.println("found start of circle");
							break;
						} else {
							if (bEdge.getSourceNode().hasAttribute(BACKWARDS_EDGE)) {
								ArrayList<Edge> backwardsEdgesOnNode = node.getAttribute(BACKWARDS_EDGE);
								backwardsEdgesOnNode.add(bEdge);
							} else {
								ArrayList<Edge> backwardsEdgesOnNode = new ArrayList<>(
										bEdge.getSourceNode().getOutDegree());
								backwardsEdgesOnNode.add(bEdge);
								bEdge.getSourceNode().addAttribute(BACKWARDS_EDGE, backwardsEdgesOnNode);
							}
						}
					}
				} else if (endNode.hasAttribute(BACKWARDS_EDGE)) {
					// System.out.println(BACKWARDS_EDGE);
					// Backwards edges should be checked recursively again on all adjacent backwards
					// edges
					bEdgeDFS(endNode);
				} else if (!endNode.hasAttribute(VISITED)) {
					// System.out.println(VISITED);
					masp(endNode);
				}

			}
			currentPath.removeLast();
		}
		nodesOnCurrentPath.removeLast();
	}

	/**
	 * A DFS Implementation for backwardsedges. Uses only edges that are stored as
	 * backwards edges in nodes. Hence edges that were visited already but ended in
	 * nodes that were on the current path that was used. These edges could lead to
	 * a simple path if the path to them don't include the endNode of the edge.
	 *
	 * @param node
	 *            the node the dfs is recursively called on.
	 */
	private void bEdgeDFS(Node node) {
		nodesOnCurrentPath.add(node);

		ArrayList<Edge> backwardsEdgesOnNode = node.getAttribute(BACKWARDS_EDGE);
		for (Edge edge : backwardsEdgesOnNode) {
			Node endNode = edge.getTargetNode();
			if (!nodesOnCurrentPath.contains(endNode)) {
				currentPath.add(edge);
				if (endNode.hasAttribute(SIMPLE_PATH)) {
					for (Edge edgeOnPath : currentPath) {
						edgeOnPath.addAttribute(SIMPLE_PATH);
						edgeOnPath.getSourceNode().addAttribute(SIMPLE_PATH);
						if (edgeOnPath.hasAttribute(BACKWARDS_EDGE)) {
							ArrayList<Edge> arrayList = edgeOnPath.getSourceNode().getAttribute(BACKWARDS_EDGE);
							arrayList.remove(edgeOnPath);
							if (arrayList.isEmpty()) {
								edgeOnPath.getSourceNode().removeAttribute(BACKWARDS_EDGE);
							}
						}
					}
				}
				if (endNode.hasAttribute(BACKWARDS_EDGE)) {
					bEdgeDFS(endNode);
				}
				currentPath.removeLast();
			}
		}
		nodesOnCurrentPath.removeLast();
	}

	/**
	 * Sets the ui.class attribute for all nodes and edges, that should be rendered
	 * specially. Especially the nodes and edges from all simple paths.
	 *
	 * @param graph
	 *            the whole graph.
	 */
	private void setNodeAndEdgeClasses(Graph graph) {
		for (Edge edge : graph.getEachEdge()) {
			if (edge.hasAttribute(SIMPLE_PATH)) {
				edge.addAttribute("ui.class", SIMPLE_PATH);
			}
		}

		for (Node node : graph.getEachNode()) {
			if (node.hasAttribute(SIMPLE_PATH)) {
				node.addAttribute("ui.class", SIMPLE_PATH);
			}
		}
		start.setAttribute("ui.class", "start, " + SIMPLE_PATH);
		target.setAttribute("ui.class", "target, " + SIMPLE_PATH);
	}

	/**
	 * Reset node and edge classes and attributes.
	 */
	public void resetNodesAndEdges() {
		for (Edge edge : graph.getEachEdge()) {
			edge.removeAttribute("ui.class");
			edge.removeAttribute(SIMPLE_PATH);
		}
		for (Node node : graph.getEachNode()) {
			node.removeAttribute("ui.class");
			node.removeAttribute(VISITED);
			node.removeAttribute(SIMPLE_PATH);
			node.removeAttribute(BACKWARDS_EDGE);
		}
		start.setAttribute("ui.class", "start");
		target.setAttribute("ui.class", "target");

	}

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	public Node getTarget() {
		return target;
	}

	/**
	 * Sets the target.
	 *
	 * @param target
	 *            the new target
	 */
	public void setTarget(Node target) {
		if (this.target != null) {
			this.target.removeAttribute(CURRENT_TARGET);
		}
		this.target = target;
		this.target.addAttribute(CURRENT_TARGET);
	}
}
