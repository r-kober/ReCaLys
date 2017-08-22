package de.upb.recalys.visualization.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

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

	Graph subGraph;
	HashMap<String, Edge> backwardEdges;
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

		subGraph = new SingleGraph("sub");

		subGraph.setStrict(false);
		subGraph.setAutoCreate(true);

		currentPath = new LinkedList<>();
		nodesOnCurrentPath = new LinkedList<>();
		backwardEdges = new HashMap<>();
		nodesOnCurrentPath.add(start);
		masp(start);
		checkBackwardsEdges();

		setNodeAndEdgeClasses(graph, subGraph);
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
	public void masp(Node node) {
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
						subGraph.addEdge(edgeOnPath.getId(), edgeOnPath.getSourceNode().getId(),
								edgeOnPath.getTargetNode().getId(), true);
						edgeOnPath.addAttribute(SIMPLE_PATH);
						edgeOnPath.getSourceNode().addAttribute(SIMPLE_PATH);
					}
				} else if (nodesOnCurrentPath.contains(endNode)) {
					// System.out.println("later");
					// save backwards edge for later
					if (node.hasAttribute(BACKWARDS_EDGE)) {
						ArrayList<Edge> backwardsEdgesOnNode = node.getAttribute(BACKWARDS_EDGE);
						backwardsEdgesOnNode.add(edge);
					} else {
						ArrayList<Edge> backwardsEdgesOnNode = new ArrayList<>(node.getOutDegree());
						backwardsEdgesOnNode.add(edge);
						node.addAttribute(BACKWARDS_EDGE, backwardsEdgesOnNode);
					}
					backwardEdges.put(edge.getId(), edge);

				} else if (endNode.hasAttribute(BACKWARDS_EDGE)) {
					// System.out.println(BACKWARDS_EDGE);
					// Backwards edges should be checked again
					ArrayList<Edge> backwardsEdgesOnNode = endNode.getAttribute(BACKWARDS_EDGE);
					ArrayList<Integer> indicesToRemove = new ArrayList<>();
					for (int i = 0; i < backwardsEdgesOnNode.size(); i++) {
						Edge bEdge = backwardsEdgesOnNode.get(i);
						if (bEdge.getTargetNode().hasAttribute(SIMPLE_PATH)
								&& !nodesOnCurrentPath.contains(bEdge.getTargetNode())) {
							// found new simple path
							for (Edge edgeOnPath : currentPath) {
								subGraph.addEdge(edgeOnPath.getId(), edgeOnPath.getSourceNode().getId(),
										edgeOnPath.getTargetNode().getId(), true);
								edgeOnPath.addAttribute(SIMPLE_PATH);
								edgeOnPath.getSourceNode().addAttribute(SIMPLE_PATH);
							}
							subGraph.addEdge(bEdge.getId(), bEdge.getSourceNode().getId(),
									bEdge.getTargetNode().getId(), true);
							bEdge.addAttribute(SIMPLE_PATH);

							indicesToRemove.add(i);
							backwardEdges.remove(bEdge.getId());
						}
					}
					for (int i = 0; i < indicesToRemove.size(); i++) {
						backwardsEdgesOnNode.remove(indicesToRemove.get(i) - i);
					}
					if (backwardsEdgesOnNode.isEmpty()) {
						endNode.removeAttribute(BACKWARDS_EDGE);
					}
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
	 * Check if backwards edges in the subgraph add a simple path. If so the
	 * backwards edge will be added to the subgraph and get the attribute
	 * "simplePath" in the graph. This should be called after
	 * {@link MarkAllSimplePaths#masp(Node)}.
	 */
	private void checkBackwardsEdges() {
		Edge edge;
		for (String id : backwardEdges.keySet()) {
			edge = backwardEdges.get(id);
			// if (subGraph.getNode(edge.getSourceNode().getId()) != null
			// && subGraph.getNode(edge.getTargetNode().getId()) != null) {
			// System.out.println(edge.getSourceNode().getAttribute("ui.label") + "->"
			// + edge.getTargetNode().getAttribute("ui.label") + "zu Teilgraph
			// hinzugefügt");
			// }

			if (subGraph.getNode(edge.getSourceNode().getId()) != null
					&& subGraph.getNode(edge.getTargetNode().getId()) != null) {
				// save targetNode from subgraph with all edges

				Node removedNode = edge.getTargetNode();
				ArrayList<Edge> removedEdges = new ArrayList<>(removedNode.getDegree());
				for (Edge e : removedNode.getEachEdge()) {
					removedEdges.add(e);
				}

				// Remove targetNode from subgraph
				subGraph.removeNode(edge.getTargetNode().getId());

				if (checkIsWay(start.getId(), edge.getSourceNode().getId())) {
					subGraph.addEdge(edge.getId(), edge.getSourceNode().getId(), edge.getTargetNode().getId(), true);
				}

				// Put removed elements back in the subgraph
				subGraph.addNode(removedNode.getId());
				for (Edge e : removedEdges) {
					subGraph.addEdge(e.getId(), e.getSourceNode().getId(), e.getTargetNode().getId(), true);
				}
			}
		}

	}

	/**
	 * Checks if there is a way from start to target in the subGraph.
	 *
	 * @param startID
	 *            the start ID
	 * @param targetID
	 *            the target ID
	 * @return true, if a way from start to target is found<br>
	 *         false otherwise
	 */
	private boolean checkIsWay(String startID, String targetID) {
		Node targetNode = subGraph.getNode(targetID);
		Iterator<Node> dfsIterator = subGraph.getNode(startID).getDepthFirstIterator();
		while (dfsIterator.hasNext()) {
			if (targetNode.equals(dfsIterator.next())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the ui.class attribute for all nodes and edges, that should be rendered
	 * specially. Especially the nodes and edges from all simple paths.
	 *
	 * @param graph
	 *            the whole graph.
	 * @param subGraph
	 *            the subGraph that represents all simple paths.
	 */
	private void setNodeAndEdgeClasses(Graph graph, Graph subGraph) {
		for (Edge edge : subGraph.getEachEdge()) {
			graph.getEdge(edge.getId()).addAttribute("ui.class", SIMPLE_PATH);
			graph.getEdge(edge.getId()).addAttribute(SIMPLE_PATH);
		}

		for (Node node : subGraph.getEachNode()) {
			graph.getNode(node.getId()).addAttribute("ui.class", SIMPLE_PATH);
			graph.getNode(node.getId()).addAttribute(SIMPLE_PATH);
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
	 * Gets the subgraph containing only nodes and edges that would be in a simple
	 * path..
	 *
	 * @return the subgraph
	 */
	public Graph getSubGraph() {
		return subGraph;
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
