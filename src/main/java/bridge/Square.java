package bridge;

import java.io.IOException;

/** Refined Abstraction: square geometry without any output-format logic. */
public final class Square extends Shape {
    private final int side;

    public Square(int side, Renderer renderer) {
        super(renderer);
        this.side = Dimensions.requireValid(side, "side");
    }

    @Override
    public void draw() throws IOException {
        renderer().drawSquare(side);
    }
}
