package pt.ulisboa.tecnico.cnv.raytracer.shapes;

import pt.ulisboa.tecnico.cnv.raytracer.*;

public class Disc extends Shape {
    private final Point center;
    private final Vector normal;
    private final double radius;

    public Disc(Point center, Vector normal, double radius) {
        this.center = center;
        this.normal = normal;
        this.radius = radius;

        Log.warn("Disc shape is not supported. This shape will be ignored.");
    }

    @Override
    public RayHit intersect(Ray ray) {
        return null;
    }
}
