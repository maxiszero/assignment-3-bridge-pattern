package bridge;

import java.io.IOException;
import java.util.Objects;

/** Abstraction: owns the replaceable reference that forms the bridge. */
public abstract class Shape {
    private Renderer renderer;

    protected Shape(Renderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    public final void setRenderer(Renderer renderer) {
        this.renderer = Objects.requireNonNull(renderer, "renderer");
    }

    protected final Renderer renderer() {
        return renderer;
    }

    public abstract void draw() throws IOException;
}
