package de.upb.recalys.visualization;

import java.util.ArrayList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import de.upb.recalys.model.RCSGraph;
import de.upb.recalys.model.RCSNode;

/**
 * The Class AbstractRCSGraph is base for all graphs that represent RCS Data in
 * some way.
 * 
 * @author Roman Kober
 */
public abstract class AbstractRCSGraph extends SingleGraph {

	/** The start node. */
	protected Node start;

	/**
	 * @see SingleGraph#SingleGraph(String)
	 */
	public AbstractRCSGraph(String id) {
		super(id);
		this.setStrict(false);
		this.addAttribute("ui.quality");
		this.addAttribute("ui.antialias");
	}

	/**
	 * @see SingleGraph#SingleGraph(String, boolean, boolean)
	 */
	public AbstractRCSGraph(String id, boolean strictChecking, boolean autoCreate) {
		super(id, strictChecking, autoCreate);
		this.addAttribute("ui.quality");
		this.addAttribute("ui.antialias");
	}

	/**
	 * @see SingleGraph#SingleGraph(String, boolean, boolean, int, int)
	 */
	public AbstractRCSGraph(String id, boolean strictChecking, boolean autoCreate, int initialNodeCapacity,
			int initialEdgeCapacity) {
		super(id, strictChecking, autoCreate, initialNodeCapacity, initialEdgeCapacity);
		this.addAttribute("ui.quality");
		this.addAttribute("ui.antialias");
	}

	/**
	 * Inits the graph with the {@link RCSGraph} data.
	 *
	 * @param rcsGraph
	 *            the {@link RCSGraph} with the data.
	 */
	public void init(RCSGraph rcsGraph) {
		this.clear();
		this.addAttribute("ui.quality");
		this.addAttribute("ui.antialias");
		addNodeLinks(rcsGraph.getRoot());
		this.start = this.getNode(Integer.toString(rcsGraph.getRoot().getID()));
		this.start.addAttribute("ui.class", "start");
		this.hideLoops();
	}

	/**
	 * Adds the node links recursively.
	 *
	 * @param node
	 *            the source from the links to add. First call should be from
	 *            the root node.
	 */
	private void addNodeLinks(RCSNode node) {
		RCSNode currentNode = node;
		ArrayList<RCSNode> childrenList = currentNode.getChildren();
		String currentNodeID = Integer.toString(currentNode.getID());
		this.addNode(currentNodeID).addAttribute("ui.label", currentNode.getLabel());

		String childID;

		// add edges to the children of the current node
		for (RCSNode child : childrenList) {
			childID = Integer.toString(child.getID());
			this.addNode(childID);
			this.addEdge(currentNodeID + "," + childID, currentNodeID, childID, true);
		}

		// recursive call on all children that are not called yet
		for (RCSNode child : childrenList) {
			childID = Integer.toString(child.getID());
			if (!this.getNode(childID).hasAttribute("ui.label")) {
				addNodeLinks(child);
			}
		}

	}

	/**
	 * Hide loops in the graph.
	 */
	protected void hideLoops() {
		for (Edge edge : this.getEachEdge()) {
			if (edge.isLoop()) {
				edge.addAttribute("ui.class", "loop");
				edge.addAttribute("ui.hide");
			}
		}
	}

	/**
	 * Increase an int attribute from an element.
	 *
	 * @param element
	 *            the element which attribute gets increased
	 * @param attribute
	 *            the attribute to increase
	 * @return the new value of the attribute or 0 if the element is null
	 */
	protected int increaseIntAttribute(Element element, String attribute) {
		if (element == null) {
			return 0;
		}
		int attributeValue = 0;
		if (element.hasAttribute(attribute)) {
			attributeValue = element.getAttribute(attribute);
		}
		attributeValue++;

		element.setAttribute(attribute, attributeValue);
		return attributeValue;
	}

	/**
	 * Sets the labels to the node ID. This is mainly used for testing purposes
	 * 
	 */
	public void setLabelsToID() {
		for (Node node : this) {
			node.setAttribute("ui.label",
					node.getId()/* + "/" + node.getAttribute("depth") */);
		}
	}

	/**
	 * Returns the start node.
	 *
	 * @return the start
	 */
	public Node getStart() {
		return start;
	}

}
