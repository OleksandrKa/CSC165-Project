package MyGameEngine;

/* This class allows game to move the camera forward and backward 
 * in the same manner as the W and D keys.
 */

import sage.input.action.AbstractInputAction;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import sage.camera.ICamera;

public class MoveYAxis extends AbstractInputAction {
	private ICamera camera;
	private float speed;

	public MoveYAxis(ICamera cam, float spd) {
		camera = cam;
		speed = spd;
	}

	@Override
	public void performAction(float time, Event event) {
		Vector3D curLoc = new Vector3D(camera.getLocation());
		Vector3D viewDir = camera.getViewDirection().normalize();
		Vector3D newLoc = curLoc.add(viewDir);

		if (event.getValue() < -0.2) {
			newLoc = curLoc.add(viewDir.mult(speed * time));
		} else {
			if (event.getValue() > 0.2) {
				newLoc = curLoc.minus(viewDir.mult(speed * time));
			} else {
				newLoc = curLoc;
			}
		}

		double newX = newLoc.getX();
		double newY = newLoc.getY();
		double newZ = newLoc.getZ();

		Point3D newLocation = new Point3D(newX, newY, newZ);
		camera.setLocation(newLocation);
	}
}
