package de.upb.recalys.visualization;

import java.util.ArrayList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import de.upb.recalys.helper.ResourceHandler;
import de.upb.recalys.model.RCSGraph;
import de.upb.recalys.model.RCSTask;

/**
 * The Class IAGraph implements a representation of a website informational
 * architecture.
 */
public class IAGraph extends AbstractRCSGraph {

	/**
	 * Instantiates a new IA graph.
	 */
	public IAGraph() {
		super("IA Graph");
	}

	@Override
	public void init(RCSGraph rcsGraph) {
		super.init(rcsGraph);
		this.addAttribute("ui.stylesheet", ResourceHandler.getURL("/stylesheets/IAgraph.css"));
		this.addAttribute("layout.quality", 4);
//		this.addAttribute("layout.stabilization-limit", 0.9);
//		this.addAttribute("layout.force", 2);
		for (Node node : this.getEachNode()) {
			node.addAttribute("layout.weight", 1.5);
		}
	}

	/**
	 * Sets the "ui.class" attribute from the nodes that are targets to
	 * "target".
	 *
	 * @param tasks
	 *            The ArrayList of all tasks
	 */
	public void markTaskTargets(ArrayList<RCSTask> tasks) {
		for (RCSTask task : tasks) {
			// get the target node for the current task
			Node target = this.getNode(Integer.toString(task.getTarget().getID()));
			target.addAttribute("ui.class", "target");
		}
	}

	public void removeTaskTargets() {
		for (Node node : this.nodeArray) {
			if (node!=null && "target".equals(node.getAttribute("ui.class"))) {
				node.removeAttribute("ui.class");
			}
		}
	}

	/**
	 * Gets the graph.
	 *
	 * @return the graph
	 */
	public Graph getGraph() {
		return this;
	}

}