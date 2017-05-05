package MyGameEngine;

/* This class allows movement of the View camera around its V axis, 
 * the same as the Left/Right arrow keys.
 */

import sage.input.action.AbstractInputAction;
import sage.scene.SceneNode;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import sage.camera.ICamera;
import sage.terrain.*;

public class MoveYaw extends AbstractInputAction {
    private OrbitCameraController controller;
    private float rotAmount = 1.0f;
    private static final String leftKey = "Event: component = Left | value = 1.0";
    private static final String rightKey = "Event: component = Right | value = 1.0";

    public MoveYaw(OrbitCameraController cc) {
        this.controller = cc;
    }

    @Override
    public void performAction(float time, Event event) {
        float camAzimuth = controller.getAzimuth();

        if (event.toString().equals(rightKey) || event.getValue() < -0.25) {
            camAzimuth += -rotAmount;
            camAzimuth = camAzimuth % 360;
            controller.setAzimuth(camAzimuth);
            //controller.getAvatar().rotate(-rotAmount, new Vector3D(0.0, 1.0, 0.0));

        } else if (event.toString().equals(leftKey)
                || (event.getValue() > 0.25 && event.getComponent().toString().equals("X Rotation"))) {
            camAzimuth += rotAmount;
            camAzimuth = camAzimuth % 360;
            controller.setAzimuth(camAzimuth);
            //controller.getAvatar().rotate(rotAmount, new Vector3D(0.0, 1.0, 0.0));
        }
    }
}