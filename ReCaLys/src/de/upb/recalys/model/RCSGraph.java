package de.upb.recalys.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.util.IteratorIterable;

import de.upb.recalys.control.ReCaLys;
import de.upb.recalys.helper.PairOfInt;

/**
 * This class defines the graph that represents the website structure
 * 
 * @author Roman Kober
 * @version 1.1
 */
public class RCSGraph implements Serializable {

	private static final long serialVersionUID = -7924849454654399670L;
	private int elements;
	private int[][] distances;

	/**
	 * A HashMap that maps a pair of two IDs from Nodes to the distance as an
	 * integer of the corresponding two nodes.
	 */
	private HashMap<PairOfInt, Integer> distanceMap;

	private RCSNode root = null;
	private RCSNode levelRoot = null;

	/** A Hashmap of all the nodes in the graph with the IDs as keys. */
	private HashMap<Integer, RCSNode> nodeMap;
	private ReCaLys recalys;

	private String systematicSearchingLog;

	/*
	 * the names of the relevant xml-tags and attributes for the import
	 */
	private final String TITLE = "title", ITEM = "item", ITEM_ID = "itemID", LINK_TO_ITEM_ID = "linkToItemID";

	/**
	 * Constructor: Creates a new graph
	 */
	public RCSGraph() {
		super();
		elements = 0;

		nodeMap = new HashMap<Integer, RCSNode>();
	}

	/**
	 * Builds a new graph structure based on the imported XMl-File
	 * 
	 * @param xmlFile
	 *            the imported XML-file that represents the website structure
	 * @author Roman Kober
	 */
	public void buildGraph(File xmlFile) {
		try {
			SAXBuilder saxBuilder = new SAXBuilder();

			Document document = saxBuilder.build(xmlFile);
			Element docRootElement = document.getRootElement();
			Element rootElement = docRootElement.getChild(ITEM);

			root = new RCSNode(Integer.parseInt(rootElement.getChildText(ITEM_ID)), "Startseite");
			nodeMap.put(root.getID(), root);
			ArrayList<RCSNode> parents = new ArrayList<>();
			parents.add(root);
			IteratorIterable<Element> itemList = rootElement.getDescendants(new ElementFilter("item"));

			// get all the cards and put them into the nodeMap
			for (Element item : itemList) {
				if (item.getChildText(LINK_TO_ITEM_ID).isEmpty()) {
					int id = Integer.parseInt(item.getChildText(ITEM_ID));
					String title = item.getChildText(TITLE);
					RCSNode node = new RCSNode(id, title);
					nodeMap.put(node.getID(), node);
				}
			}
			// build the actual graph with children and parents
			for (Element item : itemList) {
				int id, parentElementID;
				// link
				if (!item.getChildText(LINK_TO_ITEM_ID).isEmpty()) {
					id = Integer.parseInt(item.getChildText(LINK_TO_ITEM_ID));
					parentElementID = Integer
							.parseInt(item.getParentElement().getParentElement().getChildText(ITEM_ID));
				} else { // normal node
					id = Integer.parseInt(item.getChildText(ITEM_ID));
					parentElementID = Integer
							.parseInt(item.getParentElement().getParentElement().getChildText(ITEM_ID));
				}
				if (!nodeMap.get(parentElementID).isChild(nodeMap.get(id))) {
					nodeMap.get(id).addParent(nodeMap.get(parentElementID));
					nodeMap.get(parentElementID).addChildren(nodeMap.get(id));
				}
			}

		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		elements = nodeMap.size();
	}

	/**
	 * Computes the distances between all nodes in the level graph. If a node is
	 * not reachable from another node the distance will be set to -1.
	 */
	public void computeDistances() {
		distanceMap = new HashMap<>();
		int sourceID;
		RCSNode node;
		LinkedList<RCSNode> queue;
		for (Integer id : nodeMap.keySet()) {
			node = nodeMap.get(id);
			node.setExplored(true);
			sourceID = id;
			String sourceLBL = node.getLabel();
			queue = new LinkedList<RCSNode>();
			queue.offer(node);
			distanceMap.put(new PairOfInt(sourceID, sourceID), 0);
			while (!queue.isEmpty()) {
				node = (RCSNode) queue.removeFirst();
				for (RCSNode child : node.getChildren()) {
					if (!child.isExplored()) {
						queue.addLast(child);
						distanceMap.put(new PairOfInt(sourceID, child.getID()),
								distanceMap.get(new PairOfInt(sourceID, node.getID())) + 1);
						child.setExplored(true);
					}
				}
			}
			for (Integer i : nodeMap.keySet()) {
				node = nodeMap.get(i);
				node.setExplored(false);
			}
		}

		// // test output
		// for (Pair pair : distanceMap.keySet()) {
		// System.out.println(pair + ": " + distanceMap.get(pair));
		// }
	}

	/**
	 * This method will build the levelgraph basing on the normal graph.
	 */
	public void buildLevelGraph() {
		// 2 Phasen: Erste Leveln, dann bauen
		root.setLevel(0);
		int level = 0;
		RCSNode node;
		LinkedList<RCSNode> queue = new LinkedList<RCSNode>();

		queue.offer(root);
		while (queue.size() > 0) {
			node = (RCSNode) queue.removeFirst();
			for (RCSNode child : node.getChildren()) {
				if (child.getLevel() == -1) { // not explored yet
					queue.addLast(child);
					child.setLevel(node.getLevel() + 1);
					// System.out.println(child.getLabel()+":
					// Level="+child.getLevel());
				}
			}
		}

		for (Integer id : nodeMap.keySet()) {
			node = nodeMap.get(id);
			for (RCSNode child : node.getChildren()) {
				if (child.getLevel() == node.getLevel() + 1) {
					node.addLevelChildren(child);
					child.addLevelParent(node);
				}
			}
		}
	}

	/**
	 * This method will add degrees to all nodes.
	 */
	public void addNodeDegrees() {
		root.getDegree();
		// System.out.println("Rang der Wurzel: " + root.getDegree());
		// System.out.println("Anzahl Elemente: " + nodeMap.size());
		int links = 0;
		for (Integer id : nodeMap.keySet()) {
			RCSNode node = nodeMap.get(id);
			links += node.getChildren().size();
		}
		// System.out.println("Links: " + links);
	}

	/**
	 * This method will add to each node the number of leaves beneath it in the
	 * levelgraph.
	 */
	public void addLeavesCount() {
		root.getLeavesCount();
	}

	/**
	 * Returns the root of the graph
	 * 
	 * @return root
	 */
	public RCSNode getRoot() {
		return root;
	}

	/**
	 * Returns the node map.
	 *
	 * @return the node map
	 */
	public HashMap<Integer, RCSNode> getNodeMap() {
		return nodeMap;
	}

	/**
	 * Returns a node by its id
	 * 
	 * @param id
	 *            id of the wanted node
	 * @return wanted node
	 */
	public RCSNode getNodeByID(int id) {
		return nodeMap.get(new Integer(id));
	}

	/**
	 * Computes how many nodes must be inspect by the systematic search
	 * detection
	 * 
	 * @return number of nodes to inspect
	 */
	public int computeNodesToInspect() {
		int sumLevel = 0;
		int sumLeaves = 0;
		RCSNode node;
		for (Integer id : nodeMap.keySet()) {
			node = nodeMap.get(id);
			if (node.isLeaf()) {
				sumLevel += node.getLevel();
				sumLeaves++;
			}
		}
		return sumLevel / sumLeaves * 2;
	}

	/**
	 * This method computes the minimal latency for all nodes
	 * 
	 * @param timeToThink
	 *            the time a user will at least need to think at the node
	 */
	public void computeMinimalLatencies(int timeToThink) {
		RCSNode node;
		for (Integer id : nodeMap.keySet()) {
			node = nodeMap.get(id);
			node.computeMinimalLatency(timeToThink);
		}
	}

	/**
	 * Gets the distance between to nodes in the levelgraph. If the node is not
	 * reachable, the distance will be -1.
	 * 
	 * @param source
	 *            the source node
	 * @param target
	 *            the target node
	 * @return distance
	 */
	public int getDistance(int source, int target) {
		int distance = distanceMap.containsKey(new PairOfInt(source, target))
				? distanceMap.get(new PairOfInt(source, target)) : -1;
		return distance;

	}

	/**
	 * Returns the coverage of the experiment for this graph
	 * 
	 * @return coverage of the experiment
	 */
	public double getCoverage() {
		return root.getCoverage();
	}

	/**
	 * Returns the HotList: That means a list of all nodes that should be tested
	 * in a further experiment to increase the coverage by maximum. The nodes in
	 * this list are ordered to increase the coverage most effectivly.
	 * 
	 * @return HotList
	 */
	@SuppressWarnings("rawtypes")
	public LinkedList[] getHotList() {
		LinkedList[] hotList = new LinkedList[2];
		LinkedList<RCSNode> leaves = new LinkedList<RCSNode>();
		LinkedList<Double> gains = new LinkedList<Double>();
		RCSNode node;

		/* get the number of all leaves that are not already targets */
		int numberOfLeaves = root.getLeavesCount();
		for (Integer id : nodeMap.keySet()) {
			node = nodeMap.get(id);
			node.setSimulatedVisits(node.getVisitsOnOptimalPath());
			if (node.isTarget())
				numberOfLeaves--;
		}

		/* compute hotlist */
		for (int i = 0; i < numberOfLeaves; i++) {
			node = root;
			while (!node.isLeaf()) {
				RCSNode nextNode = node.getLevelChildren().get(0);
				for (RCSNode child : node.getLevelChildren()) {
					if (child.getSimulatedCoverage() < nextNode.getSimulatedCoverage())
						nextNode = child;
					else if (child.getSimulatedCoverage() == nextNode.getSimulatedCoverage())
						if (child.getLeavesCount() > nextNode.getLeavesCount())
							nextNode = child;
				}
				node = nextNode;
			}

			RCSNode target = node;
			leaves.offer(target);
			int distFromNode;
			int distFromChild;
			LinkedList<RCSNode> queue = new LinkedList<RCSNode>();
			queue.offer(root);
			root.setSimulatedVisits(root.getSimulatedVisits() + 1);
			while (queue.size() > 0) {
				node = (RCSNode) queue.removeFirst();
				distFromNode = getDistance(node.getID(), target.getID());
				for (RCSNode child : node.getLevelChildren()) {
					distFromChild = getDistance(child.getID(), target.getID());
					if (distFromChild != -1 && distFromNode == (distFromChild + 1)) {
						queue.offer(child);
						child.setSimulatedVisits(child.getSimulatedVisits() + 1);
					}
				}
			}
			gains.offer(root.getSimulatedCoverage());
			// System.out.println("Adding: "+target+", gain to:
			// "+root.getSimulatedCoverage());
		}

		// System.out.println(hotList);
		hotList[0] = leaves;
		hotList[1] = gains;
		return hotList;
	}

	/**
	 * Resets the properties of this graph's nodes: - explored and isTarget to
	 * false - distRating, simulatedVisits, visits, visitsOnOptimalPaths t0 0 -
	 * minimalLatency to -1
	 */
	public void resetGraphProperties() {
		RCSNode node;
		for (Integer id : nodeMap.keySet()) {
			node = nodeMap.get(id);
			node.resetProperties();
		}
		systematicSearchingLog = "";
	}

	/**
	 * Resets the analysis of the experiment on this graph's nodes: - explored
	 * to false - distRating, simulatedVisits, visits, visitsOnOptimalPaths t0 0
	 * - minimalLatency to -1 Initializes the rating-arrays to size countTask
	 * 
	 * @param countTasks
	 *            number of tasks in the experiment
	 */
	public void resetAnalysis(int countTasks) {
		RCSNode node;
		for (Integer id : nodeMap.keySet()) {
			node = nodeMap.get(id);
			node.resetAnalysis();
			node.initRatings(countTasks);
		}
		systematicSearchingLog = "";
	}

	/**
	 * Returns the badlist. This list includes all nodes in order of their malus
	 * 
	 * @return badlist
	 */
	public LinkedList<RCSNode> getBadList() {
		RCSNode node;
		int maxMalus;
		int maxLatencyWeight;
		int idOfMax;
		LinkedList<RCSNode> badList = new LinkedList<RCSNode>();
		for (int i = 0; i < nodeMap.size(); i++) {
			maxMalus = 0;
			maxLatencyWeight = 0;
			idOfMax = -1;
			for (Integer id : nodeMap.keySet()) {
				node = nodeMap.get(id);
				if (!badList.contains(node) && node.getCoverage() > 0) {
					if (node.getMalus() > maxMalus) {
						maxMalus = node.getMalus();
						maxLatencyWeight = node.getLatencyWeight();
						idOfMax = id;
					} else if (node.getMalus() == maxMalus)
						if (node.getLatencyWeight() > maxLatencyWeight) {
							maxMalus = node.getMalus();
							maxLatencyWeight = node.getLatencyWeight();
							idOfMax = id;
						}
				}
			}
			if (idOfMax != -1) {
				badList.offer(getNodeByID(idOfMax));

				// System.out.println(
				// i + ": " + getNodeByID(idOfMax).getLabel() + "> Malus: " +
				// maxMalus + ", LatencyWeight: "
				// + maxLatencyWeight + ", DistRating: " +
				// getNodeByID(idOfMax).getDistRating());

			}
		}
		return badList;
	}

	/**
	 * Adds the detection of systematic searching to the log
	 * 
	 * @param detectionText
	 *            information about the detection
	 */
	public void addDetectionOfSystematicSearching(String detectionText) {
		systematicSearchingLog += detectionText;
	}

	/**
	 * Returns the log about the detection of systematic searching
	 * 
	 * @return systematicSearchingLog
	 */
	public String getSystematicSearchingLog() {
		return systematicSearchingLog;
	}

	/**
	 * Builds a new graph structure based on the rows of the imported csv-file
	 * 
	 * @param importedRows
	 *            rows of the imported csv-file
	 * @deprecated
	 */
	public void buildGraph(String[] importedRows) {

		String[] rows = importedRows;
		int rowsCount = rows.length;
		int columnsCount = countColumns(rows[0]);

		root = new RCSNode(0, "Startseite");
		nodeMap.put(new Integer(root.getID()), root);
		RCSNode[] parents = new RCSNode[columnsCount + 1];
		parents[0] = root;
		int id = 1;
		RCSNode node;

		// Search for CrossLinks
		RCSNode[] crossLinks = new RCSNode[rowsCount * columnsCount];
		for (int i = 0; i < rowsCount; i++) {
			String[] columns = divideIntoColumns(rows[i], columnsCount);
			for (int j = 0; j < columnsCount; j++) {
				if (columns[j].startsWith("id(")) {
					char[] chars = columns[j].toCharArray();
					int bracket = -1;
					boolean b = true;
					for (int k = 3; k < chars.length && b; k++) {
						if (chars[k] == ')') {
							bracket = k;
							b = false;
						}
					}
					int link_id = Integer.parseInt(columns[j].substring(3, bracket));
					String label = columns[j].substring(bracket + 2, columns[j].length());
					crossLinks[link_id] = new RCSNode(id, label);
					nodeMap.put(new Integer(crossLinks[link_id].getID()), crossLinks[link_id]);
					id++;
				}
			}
		}

		// Build Graph
		for (int i = 0; i < rowsCount; i++) {
			String[] columns = divideIntoColumns(rows[i], columnsCount);
			for (int j = 0; j < columnsCount; j++) {
				if (!columns[j].equals("")) {
					if (columns[j].startsWith("id(")) {
						char[] chars = columns[j].toCharArray();
						int bracket = -1;
						boolean b = true;
						for (int k = 3; k < chars.length && b; k++) {
							if (chars[k] == ')') {
								bracket = k;
								b = false;
							}
						}
						int link_id = Integer.parseInt(columns[j].substring(3, bracket));
						node = crossLinks[link_id];
					} else {
						// links
						if (columns[j].startsWith("link(")) {
							char[] chars = columns[j].toCharArray();
							int bracket = -1;
							boolean b = true;
							for (int k = 3; k < chars.length && b; k++) {
								if (chars[k] == ')') {
									bracket = k;
									b = false;
								}
							}
							int link_id = Integer.parseInt(columns[j].substring(5, bracket));
							node = crossLinks[link_id];
						} else {
							node = new RCSNode(id, columns[j]);
							id++;
						}
					}

					// parent is root
					if (j == 0) {
						node.addParent(root);
						root.addChildren(node);
					} else {
						node.addParent(parents[j - 1]);
						parents[j - 1].addChildren(node);
					}
					parents[j] = node;
					nodeMap.put(new Integer(node.getID()), node);
				}
			}
		}
		elements = id;
		// System.out.println(elements);
		// System.out.println(linkCounter+elements);
	}

	/**
	 * Divides the imported rows into columns
	 * 
	 * @param row
	 *            row, that shall be divided
	 * @param columnsCount
	 *            number of columns the row shall be divided in
	 * @return String[] that contains the content of the columns
	 * @deprecated
	 */
	public String[] divideIntoColumns(String row, int columnsCount) {
		char[] chars = row.toCharArray();
		int start = 0;
		int end = 0;
		String[] columns = new String[columnsCount];
		int column = 0;
		for (int i = 0; i < chars.length && column < columnsCount; i++) {
			if (chars[i] == ';') {
				end = i;
				String s = "";
				for (int j = start; j < end; j++)
					s += chars[j];
				columns[column] = s;
				start = i + 1;
				column++;
			}
		}
		columns[columnsCount - 1] = row.substring(end + 1, row.length());

		return columns;
	}

	/**
	 * Counts the columns in a row of the imported csv-file
	 * 
	 * @param row
	 *            row of the imported csv-file
	 * @return number of columns
	 * @deprecated
	 */
	public int countColumns(String row) {
		int columnsCount = 0;
		char[] chars = row.toCharArray();
		for (int i = 0; i < chars.length; i++)
			if (chars[i] == ';')
				columnsCount++;
		columnsCount++;
		return columnsCount;
	}
}
