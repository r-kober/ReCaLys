package de.upb.recalys.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.upb.recalys.model.RCSGraph;
import de.upb.recalys.model.RCSNode;
import de.upb.recalys.model.RCSPath;
import de.upb.recalys.model.RCSTask;

/**
 * This class defines a ContentHandler that imports the results from the
 * xml-path-file with the help of SAX.
 * 
 * @author Roman Kober
 * @version 1.0
 * 
 */
public class ResultsContentHandler extends DefaultHandler {

	private ArrayList<RCSTask> experiment;
	private RCSGraph graph;
	private HashMap<Integer, RCSNode> nodeMap;
	private ReCaLys recalys;
	private int indexTask;

	private HashSet<String> userSet;
	private String user;
	private RCSNode currentNode;
	private RCSNode skippedNode;
	private RCSPath newPath;
	private int time;

	private boolean skipped;
	private boolean unknown;
	private int itemCount;

	/*
	 * the names of the relevant xml-tags and attributes for the import of the tasks
	 */
	@SuppressWarnings("unused")
	private final String EXP_USER_ID = "exp_user_id", WANTED_ITEM = "wanted_item", VALUE = "value", ITEM_ID = "itemID",
			RESULT_TASK_ID = "resultTaskID", ITEM = "item", ROOT = "Dummy", STATUS = "status", SKIPPED = "skipped",
			UNKNOWN = "unkown", MS = "ms", DURATION = "duration", RELATIVE_DURATION = "relativeDuration";

	/**
	 * Constructor: Creates a new PathContentHandler.
	 *
	 * @param recalys
	 *            object of the main class of this app
	 */
	public ResultsContentHandler(ReCaLys recalys) {
		this.recalys = recalys;
		this.experiment = recalys.getExperiment();
		this.graph = recalys.getGraph();
		this.nodeMap = this.graph.getNodeMap();
		userSet = new HashSet<String>();
		indexTask = 0;
		skippedNode = new RCSNode(-1, "skipped");
	}

	/**
	 * Searches for appearance of wanted Items and then creates a list of all wanted
	 * items.
	 *
	 * @param uri
	 *            the uri
	 * @param localName
	 *            the local name
	 * @param qName
	 *            the q name
	 * @param atrbts
	 *            the atrbts
	 * @throws SAXException
	 *             the SAX exception
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atrbts) throws SAXException {
		if (qName.equals(EXP_USER_ID)) {
			user = atrbts.getValue(VALUE);
			if (userSet.add(user)) {
				recalys.increaseUserCount();
			}
			currentNode = graph.getRoot();
		} else if (qName.equals(WANTED_ITEM)) {
			// get all relevant parameters for a new Task
			String taskDescription = atrbts.getValue(VALUE);
			// int id = Integer.parseInt(atrbts.getValue(RESULT_TASK_ID));
			int itemID = Integer.parseInt(atrbts.getValue(ITEM_ID));
			RCSNode target = graph.getNodeByID(itemID);
			RCSNode source = graph.getRoot();

			String status = atrbts.getValue(STATUS);
			skipped = status.equals(SKIPPED) ? true : false;
			unknown = status.equals(UNKNOWN) ? true : false;

			String duration = atrbts.getValue(DURATION);

			// find out if the task exists already in the experiment list
			boolean existsAlready = false;
			for (RCSTask task : experiment) {
				if (taskDescription.equals(task.getAlternativeTargetName())) {
					existsAlready = true;
					break;
				}
			}

			// add new Task if it is really a new task
			if (!existsAlready) {
				RCSTask newTask = new RCSTask(graph, source, target, taskDescription, indexTask);
				experiment.add(newTask);
				indexTask++;
			}

			// initialize new path and add it to the current task
			RCSTask task;
			for (int i = 0; i < experiment.size(); i++) {
				task = experiment.get(i);
				if (taskDescription.equals(task.getAlternativeTargetName())) {
					newPath = new RCSPath(graph, graph.getRoot(), target);
					newPath.setTask(task);
					newPath.setUser(user);
					newPath.setDuration(duration);
					return;
				}
			}
		} else if (qName.equals(ITEM)) {
			String nodeLabel = atrbts.getValue(VALUE);
			Integer itemID = Integer.parseInt(atrbts.getValue(ITEM_ID));
			time = Integer.parseInt(atrbts.getValue(RELATIVE_DURATION)) / 1000;
			itemCount++;
			if (nodeLabel.equals(ROOT)) {
				currentNode = graph.getRoot();
			} else {
				currentNode = nodeMap.get(itemID);
				return;
			}
		}
	}

	/**
	 * Searches for appearance of wanted Items and then creates a list of all wanted
	 * items. Is triggered if a new element starts.
	 *
	 * @param uri
	 *            the uri
	 * @param localName
	 *            the local name
	 * @param qName
	 *            the q name
	 * @throws SAXException
	 *             the SAX exception
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals(WANTED_ITEM)) {
			/*
			 * skipped tasks that have no path are not imported, because they have no
			 * relevant data. The tester didn't even try to complete the task. Paths that
			 * have an "unknown" status are also not imported.
			 */
			if (skipped && itemCount == 0 || unknown) {
				newPath = null;
				currentNode = null;
				time = 0;
				itemCount = 0;
				return;
			}
			// Add the path to the task
			newPath.getTask().addPath(newPath);

			newPath.setSkipped(skipped);
			newPath.setFinalized();
			newPath = null;
			currentNode = null;
			time = 0;
			itemCount = 0;

		} else if (qName.equals("item")) {
			newPath.addNode(currentNode, time);
		}
	}

}
