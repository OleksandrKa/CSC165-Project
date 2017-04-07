package csc165_lab3;

import sage.scene.bounding.BoundingVolume;
import sage.scene.shape.Sphere;

public interface ICollectible {
	BoundingVolume worldBound();
}