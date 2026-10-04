package bridge;

import java.io.IOException;

/** Refined Abstraction: circle geometry without any output-format logic. */
public final class Circle extends Shape {
    private final int radius;

    public Circle(int radius, Renderer renderer) {
        super(renderer);
        this.radius = Dimensions.requireValid(radius, "radius");
    }

    @Override
    public void draw() throws IOException {
        renderer().drawCircle(radius);
    }
}
