package bridge;

import java.io.IOException;

/**
 * Implementor: low-level drawing operations, independent of Shape subclasses.
 * Dimensions are integer logical units in [1, 1000]. Each call replaces the
 * corresponding shape file in the renderer's output directory.
 */
public interface Renderer {
    void drawCircle(int radius) throws IOException;

    void drawSquare(int side) throws IOException;
}
