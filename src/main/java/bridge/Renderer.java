package bridge;

import java.io.IOException;

/** Implementor: low-level drawing operations, independent of Shape subclasses. */
public interface Renderer {
    void drawCircle(int radius) throws IOException;

    void drawSquare(int side) throws IOException;
}
