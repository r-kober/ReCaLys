package de.upb.recalys.visualization.view;

import java.awt.event.KeyEvent;

import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.DefaultShortcutManager;

/**
 * A custom Shortcut Manager for GraphStream Viewers.
 */
public class RCSGraphShortcutManager extends DefaultShortcutManager {

	// Events

	/**
	 * A key has been pressed.
	 * 
	 * @param event
	 *            The event that generated the key.
	 */
	@Override
	public void keyPressed(KeyEvent event) {
		Camera camera = view.getCamera();

		if (event.getKeyChar() == '+') {
			camera.setViewPercent(Math.max(0.0001, camera.getViewPercent() * 0.9));
		} else if (event.getKeyChar() == '-') {
			camera.setViewPercent(Math.min(1.0,camera.getViewPercent() * 1.1));
		} else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
			double delta = 0;

			if ((event.getModifiers() & KeyEvent.SHIFT_MASK) != 0)
				delta = camera.getGraphDimension() * 0.1f;
			else
				delta = camera.getGraphDimension() * 0.01f;

			delta *= camera.getViewPercent();

			Point3 p = camera.getViewCenter();
			camera.setViewCenter(p.x - delta, p.y, 0);
		} else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {

			double delta = 0;

			if ((event.getModifiers() & KeyEvent.SHIFT_MASK) != 0)
				delta = camera.getGraphDimension() * 0.1f;
			else
				delta = camera.getGraphDimension() * 0.01f;

			delta *= camera.getViewPercent();

			Point3 p = camera.getViewCenter();
			camera.setViewCenter(p.x + delta, p.y, 0);

		} else if (event.getKeyCode() == KeyEvent.VK_UP) {
			double delta = 0;

			if ((event.getModifiers() & KeyEvent.SHIFT_MASK) != 0)
				delta = camera.getGraphDimension() * 0.1f;
			else
				delta = camera.getGraphDimension() * 0.01f;

			delta *= camera.getViewPercent();

			Point3 p = camera.getViewCenter();
			camera.setViewCenter(p.x, p.y + delta, 0);
		} else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
			double delta = 0;

			if ((event.getModifiers() & KeyEvent.SHIFT_MASK) != 0)
				delta = camera.getGraphDimension() * 0.1f;
			else
				delta = camera.getGraphDimension() * 0.01f;

			delta *= camera.getViewPercent();

			Point3 p = camera.getViewCenter();
			camera.setViewCenter(p.x, p.y - delta, 0);
		}
	}

	/**
	 * A key has been typed.
	 * 
	 * @param event
	 *            The event that generated the key.
	 */
	public void keyTyped(KeyEvent event) {
		if (event.getKeyChar() == 'R' || event.getKeyChar() == '0') {
			view.getCamera().resetView();
		}
		// else if( event.getKeyChar() == 'B' )
		// {
		// view.setModeFPS( ! view.getModeFPS() );
		// }
	}

}
