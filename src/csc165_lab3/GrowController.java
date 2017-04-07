package csc165_lab3;

import sage.scene.Controller;
import sage.scene.SceneNode;
import graphicslib3D.Matrix3D;

public class GrowController extends Controller {
	private double growRate = 1.05; // Grow 5%
	private double cycleTime = 2000.0; // default cycle time
	private double totalTime;

	public void setCycleTime(double c) {
		cycleTime = c;
	}

	public void update(double time) {
		double grow = (growRate * time);
		Matrix3D scale = new Matrix3D();
		scale.scale(grow, grow, grow);
		for (SceneNode node : controlledNodes) {
			Matrix3D curScale = node.getLocalScale();
			curScale.concatenate(scale);
			node.setLocalScale(curScale);
		}
	}
}
