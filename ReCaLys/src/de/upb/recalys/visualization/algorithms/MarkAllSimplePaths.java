package de.upb.recalys.visualization.algorithms;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import de.upb.recalys.visualization.GraphTools;

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
	Stack<Edge> stack;
	LinkedList<Node> currentPath;

	private final String VISITED = "visited", HAS_PATH = "hasPath", FORBIDDEN_EDGE = "forbiddeEdge",
			LEAF_NODE = "leafNode", SIMPLE_PATH = "simplePath", CURRENT_TARGET = "currentTarget";

	/**
	 * {@inheritDoc org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)}
	 * <br/>
	 * This init method sets the start to the node with index 0 and the target
	 * to the Node with the index |V|-1.
	 */
	@Override
	public void init(Graph graph) {
		this.init(graph, graph.getNode(0), graph.getNode(graph.getNodeCount() - 1));
	}

	/**
	 * This method should be used instead of
	 * {@link MarkAllSimplePaths#init(Graph)}. The Difference is that you can
	 * determine the start and target yourself.
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

		stack = new Stack<>();
		currentPath = new LinkedList<>();
		// subGraph.addNode(start.getId());

		dfs(start, null);

		setNodeAndEdgeClasses(graph, subGraph);
	}

	/**
	 * A recursive implementation of Depth First Search. The algorithm searches
	 * there for cycles and all paths to the {@link MarkAllSimplePaths#target}.
	 * This result is stored in a subgraph that contains only the edges and
	 * nodes that would be in a simple path.
	 *
	 * @param node
	 *            the current node that the algorithm starts from
	 */
	public void dfs(Node node, Node lastNode) {
		int backwardsEdgeCount = 0;
		node.addAttribute(VISITED);

		for (Edge currentEdge : node.getEachLeavingEdge()) {
			if (currentEdge.getTargetNode().equals(lastNode)) {

				// Backward edges should always be visited last
				stack.push(currentEdge);

				backwardsEdgeCount++;
				// System.out.println(currentEdge + ": " + "has backwards
				// edge");
				continue;
			}

			// System.out.print(currentEdge + ": ");
			Node currentTargetNode = currentEdge.getTargetNode();
			subGraph.addEdge(currentEdge.getId(), node.getId(), currentTargetNode.getId(), true);

			// Add current Node to the currentPath
			if (subGraph.getNode(node.getId()).hasAttribute(HAS_PATH)) {
				currentPath.clear();
			} else {
				currentPath.add(node);
			}

			if (currentEdge.hasAttribute(FORBIDDEN_EDGE)) {
				// this edge can be part of a cycle
				// System.out.println("forbidden edge");
			} else if (currentTargetNode.hasAttribute(LEAF_NODE)) {
				subGraph.removeNode(currentTargetNode.getId());
				// System.out.println("Leaf Node found and removed again");
			} else if (currentEdge.isLoop()) {
				subGraph.removeEdge(currentEdge.getId());
				// System.out.println("loop");
			} else if (currentTargetNode.getId().equals(target.getId())) {
				// found target
				setHasPath(subGraph);
				// subGraph.getNode(currentSourceNode.getId()).addAttribute(LIFE_NODE);
				// System.out.println("new Path to target found");
			} else if (subGraph.getNode(currentTargetNode.getId()).hasAttribute(HAS_PATH)) {
				// found candidate for new Path
				if (!checkIsWay(subGraph.getNode(currentTargetNode.getId()), subGraph.getNode(node.getId()))) {
					// no cycle from the candidate in the subgraph
					setHasPath(subGraph);
					// System.out.println("new Path to " + currentTargetNode + "
					// found");
					// Backwardsedge im currentTargetNode extra prüfen
					for (Edge edge : currentTargetNode.getEachLeavingEdge()) {
						if (subGraph.getEdge(edge.getId()) == null
								&& subGraph.getNode(edge.getTargetNode().getId()) != null) {
							String initialEdgeID = subGraph
									.getEdge(edge.getTargetNode().getEdgeToward(edge.getSourceNode().getId()).getId())
									.getId();

							subGraph.removeEdge(initialEdgeID);
							if (!checkIsWay(subGraph.getNode(edge.getTargetNode().getId()),
									subGraph.getNode(edge.getSourceNode().getId()))) {
								subGraph.addEdge(edge.getId(), edge.getSourceNode().getId(),
										edge.getTargetNode().getId(), true);
								// System.out.println(edge + " backwardsedge is
								// simple");

							}
							// add removed edge back into the subgraph
							subGraph.addEdge(initialEdgeID, edge.getTargetNode().getId(), edge.getSourceNode().getId(),
									true);

						}
					}
					setHasPath(subGraph);

				} else {
					// cycle from the candidate in the subgraph
					// System.out.println("cycle with " + currentTargetNode + "
					// and " + node + " found");
					subGraph.removeEdge(currentEdge.getId());
					for (Node nodeOnCurrentPath : currentPath) {
						nodeOnCurrentPath.removeAttribute(VISITED);
						// System.out.println("\t" + nodeOnCurrentPath + "
						// Visited Attribute removed ");

					}

				}
			} else if (!currentTargetNode.hasAttribute(VISITED)) {
				// not yet visited node
				// System.out.println("not yet visited node");
				dfs(currentTargetNode, node);
			} else {
				// already visited node with no relevant attributes
				subGraph.removeEdge(currentEdge.getId());
				// System.out.println("already visited and nothing relevant");
			}
		}

		if (backwardsEdgeCount >= 1) {
			// backwards edge handling
			int handledBackwardsEdges = 0;
			for (int i = 1; i <= backwardsEdgeCount; i++) {

				Edge backwardsEdge = stack.pop();
				// System.out.print(backwardsEdge);
				if (backwardsEdge.getSourceNode().hasAttribute(HAS_PATH)
						&& backwardsEdge.getTargetNode().hasAttribute(HAS_PATH)) {
					Edge initialEdge = backwardsEdge.getTargetNode()
							.getEdgeToward(backwardsEdge.getSourceNode().getId());

					subGraph.removeEdge(initialEdge.getId());

					if (checkIsWay(subGraph.getNode(backwardsEdge.getTargetNode().getId()),
							subGraph.getNode(target.getId()))) {
						subGraph.addEdge(backwardsEdge.getId(), backwardsEdge.getSourceNode().getId(),
								backwardsEdge.getTargetNode().getId(), true);
						handledBackwardsEdges++;
						// System.out.println("backwardsedge is simple");

					}
					// add removed edge back into the subgraph
					subGraph.addEdge(initialEdge.getId(), initialEdge.getSourceNode().getId(),
							initialEdge.getTargetNode().getId(), true);
					setHasPath(subGraph);
				} else if (backwardsEdge.getSourceNode().hasAttribute(HAS_PATH)) {
					// not possible because if I found a path than it used the
					// source
					// System.out.println(" source has path");
				} else if (backwardsEdge.getTargetNode().hasAttribute(HAS_PATH)) {
					// do nothing. This will be handled possibly from the source
					// node if there is another path to it.
					// System.out.println(" target has path");
				} else {
					backwardsEdge.setAttribute(FORBIDDEN_EDGE);
					// System.out.println(" forbidden");
				}
			}
			backwardsEdgeCount -= handledBackwardsEdges;
		}

		if (0 == Toolkit.leavingWeightedDegree(subGraph.getNode(node.getId()), "") && node.hasAttribute(VISITED)) {
			// leaf node found and remove it from subgraph
			subGraph.removeNode(node.getId());
			node.addAttribute(LEAF_NODE);
			// System.out.println(node + ": leaf node found and removed it from
			// subgraph");
		}

		if (!node.hasAttribute(VISITED) && !node.hasAttribute(HAS_PATH)) {
			subGraph.removeNode(node.getId());
			// System.out.println(node + ": node with outgoing edge that is a
			// cycle path removed");
		}
		if (!currentPath.isEmpty()) {
			currentPath.removeLast();
		}

	}

	/**
	 * Sets the attribute "hasPath" to all edges and nodes of the graph.
	 *
	 * @param graph
	 *            the graph that the attribute is added to.
	 */
	private void setHasPath(Graph graph) {
		for (Edge edge : graph.getEachEdge()) {
			edge.setAttribute(HAS_PATH);
		}
		for (Node node : graph.getEachNode()) {
			node.setAttribute(HAS_PATH);
		}
	}

	/**
	 * Checks if there is a way from start to target in the graph.
	 *
	 * @param start
	 *            the node the algorithm starts from
	 * @param target
	 *            the node to which a way from the start is searched
	 * @return true, if a way from start to target is found<br/>
	 *         false otherwise
	 */
	private boolean checkIsWay(Node start, Node target) {
		Iterator<Node> dfsIterator = start.getDepthFirstIterator();
		while (dfsIterator.hasNext()) {
			if (target.equals(dfsIterator.next())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the ui.class attribute for all nodes and edges, that should be
	 * rendered specially. Especially the nodes and edges from all simple paths.
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
			edge.removeAttribute(FORBIDDEN_EDGE);
		}
		for (Node node : graph.getEachNode()) {
			node.removeAttribute("ui.class");
			node.removeAttribute(VISITED);
			node.removeAttribute(LEAF_NODE);
			node.removeAttribute(HAS_PATH);
			node.removeAttribute(SIMPLE_PATH);
		}
		for (Edge edge : target.getEachLeavingEdge()) {
			edge.setAttribute(FORBIDDEN_EDGE);
		}
		start.setAttribute("ui.class", "start");
		target.setAttribute("ui.class", "target");

	}

	/**
	 * Gets the subgraph containing only nodes and edges that would be in a
	 * simple path..
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
