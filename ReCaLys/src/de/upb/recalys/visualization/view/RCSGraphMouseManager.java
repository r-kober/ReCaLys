package de.upb.recalys.visualization.view;

import java.awt.event.MouseEvent;

import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.util.DefaultMouseManager;

/**
 * The Custom Mouse Manager for GraphStream Viewers.
 */
public class RCSGraphMouseManager extends DefaultMouseManager {

	double xLastViewCenter, yLastViewCenter;
	double xBeginDrag = 0, yBeginDrag = 0;
	Camera camera;

	@Override
	public void mousePressed(MouseEvent event) {
		super.mousePressed(event);
		camera = this.view.getCamera();
		xLastViewCenter = camera.getViewCenter().x;
		yLastViewCenter = camera.getViewCenter().y;

		xBeginDrag = event.getX();
		yBeginDrag = event.getY();
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		if (curElement != null) {
			elementMoving(curElement, event);
		} else {
			double xDelta = (event.getX() - xBeginDrag) * camera.getViewPercent() * camera.getGraphDimension() * 0.002f;
			double yDelta = (event.getY() - yBeginDrag) * camera.getViewPercent() * camera.getGraphDimension() * 0.002f;

			camera.setViewCenter(xLastViewCenter - xDelta, yLastViewCenter + yDelta, 0);
		}
	}

}
