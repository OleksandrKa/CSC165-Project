var JavaPackages = new JavaImporter(
	Packages.java.awt.Color,
	Packages.sage.scene.shape.Line,
	Packages.graphicslib3D.Point3D);
with (JavaPackages)
{
	var origin = new Point3D(0, 0, 0);
	var xEnd = new Point3D(100, 0, 0);
	var yEnd = new Point3D(0, 100, 0);
	var zEnd = new Point3D(0, 0, 100);
	var xAxis = new Line(origin, xEnd, Color.red, 2);
	var yAxis = new Line(origin, yEnd, Color.green, 2);
	var zAxis = new Line(origin, zEnd, Color.blue, 2);
}