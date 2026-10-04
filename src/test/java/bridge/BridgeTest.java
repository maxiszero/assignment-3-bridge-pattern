package bridge;

import java.util.ArrayList;
import java.util.List;

/** Dependency-free tests: failures throw AssertionError even without -ea. */
public final class BridgeTest {
    private static int passed;

    private BridgeTest() { }

    public static void main(String[] args) throws Exception {
        run("circle keeps its radius when its renderer changes", () -> {
            RecordingRenderer first = new RecordingRenderer();
            RecordingRenderer second = new RecordingRenderer();
            Shape circle = new Circle(40, first);
            circle.draw();
            circle.setRenderer(second);
            circle.draw();
            equal(List.of("circle:40"), first.drawings);
            equal(List.of("circle:40"), second.drawings);
        });
        run("square keeps its side when its renderer changes", () -> {
            RecordingRenderer first = new RecordingRenderer();
            RecordingRenderer second = new RecordingRenderer();
            Shape square = new Square(80, first);
            square.draw();
            square.setRenderer(second);
            square.draw();
            equal(List.of("square:80"), first.drawings);
            equal(List.of("square:80"), second.drawings);
        });
        run("invalid dimensions are rejected", () -> {
            for (int size : new int[] {Integer.MIN_VALUE, -1, 0, 1001, Integer.MAX_VALUE}) {
                throwsType(IllegalArgumentException.class,
                        () -> new Circle(size, new RecordingRenderer()));
                throwsType(IllegalArgumentException.class,
                        () -> new Square(size, new RecordingRenderer()));
            }
        });
        run("boundary dimensions are accepted", () -> {
            RecordingRenderer renderer = new RecordingRenderer();
            new Circle(1, renderer).draw();
            new Square(1000, renderer).draw();
            equal(List.of("circle:1", "square:1000"), renderer.drawings);
        });
        run("null renderer is rejected without losing the previous renderer", () -> {
            throwsType(NullPointerException.class, () -> new Circle(10, null));
            throwsType(NullPointerException.class, () -> new Square(10, null));
            RecordingRenderer renderer = new RecordingRenderer();
            Shape circle = new Circle(10, renderer);
            throwsType(NullPointerException.class, () -> circle.setRenderer(null));
            circle.draw();
            equal(List.of("circle:10"), renderer.drawings);
        });
        System.out.println("PASS: " + passed + " tests");
    }

    private static void run(String name, CheckedAction test) throws Exception {
        test.run();
        passed++;
        System.out.println("PASS " + name);
    }

    private static void equal(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    private static void throwsType(Class<? extends Exception> type, CheckedAction action)
            throws Exception {
        try {
            action.run();
        } catch (Exception error) {
            if (type.isInstance(error)) {
                return;
            }
            throw error;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }

    private static final class RecordingRenderer implements Renderer {
        private final List<String> drawings = new ArrayList<>();

        @Override
        public void drawCircle(int radius) {
            drawings.add("circle:" + radius);
        }

        @Override
        public void drawSquare(int side) {
            drawings.add("square:" + side);
        }
    }
}
