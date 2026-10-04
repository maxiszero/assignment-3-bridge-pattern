package bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Locale;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

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
        run("vector circle produces valid SVG geometry", () -> withTempDirectory(dir -> {
            new Circle(40, new VectorRenderer(dir)).draw();
            Element svg = readSvg(dir.resolve("circle.svg"));
            equal("104", svg.getAttribute("width"));
            Element circle = (Element) svg.getElementsByTagName("circle").item(0);
            equal("52", circle.getAttribute("cx"));
            equal("52", circle.getAttribute("cy"));
            equal("40", circle.getAttribute("r"));
        }));
        run("vector square produces valid SVG geometry", () -> withTempDirectory(dir -> {
            new Square(80, new VectorRenderer(dir)).draw();
            Element svg = readSvg(dir.resolve("square.svg"));
            Element square = (Element) svg.getElementsByTagName("rect").item(1);
            equal("12", square.getAttribute("x"));
            equal("12", square.getAttribute("y"));
            equal("80", square.getAttribute("width"));
            equal("80", square.getAttribute("height"));
        }));
        run("raster circle is a decodable PNG with circular pixels", () -> withTempDirectory(dir -> {
            new Circle(40, new RasterRenderer(dir)).draw();
            var image = ImageIO.read(dir.resolve("circle.png").toFile());
            equal(104, image.getWidth());
            equal(104, image.getHeight());
            equal(0x2563eb, image.getRGB(52, 52) & 0xffffff);
            equal(0xffffff, image.getRGB(12, 12) & 0xffffff);
            equal(0xffffff, image.getRGB(0, 0) & 0xffffff);
        }));
        run("raster square has the correct size and fill", () -> withTempDirectory(dir -> {
            new Square(80, new RasterRenderer(dir)).draw();
            var image = ImageIO.read(dir.resolve("square.png").toFile());
            equal(104, image.getWidth());
            equal(104, image.getHeight());
            equal(0x2563eb, image.getRGB(13, 13) & 0xffffff);
            equal(0x2563eb, image.getRGB(90, 90) & 0xffffff);
            equal(0xffffff, image.getRGB(0, 0) & 0xffffff);
        }));
        run("the same shapes work with both concrete renderers", () -> withTempDirectory(dir -> {
            Renderer vector = new VectorRenderer(dir.resolve("vector"));
            Renderer raster = new RasterRenderer(dir.resolve("raster"));
            Shape[] shapes = {new Circle(40, vector), new Square(80, vector)};
            for (Shape shape : shapes) {
                shape.draw();
                shape.setRenderer(raster);
                shape.draw();
                shape.setRenderer(vector);
                shape.draw();
            }
            equal(true, Files.isRegularFile(dir.resolve("vector/circle.svg")));
            equal(true, Files.isRegularFile(dir.resolve("vector/square.svg")));
            equal(true, Files.isRegularFile(dir.resolve("raster/circle.png")));
            equal(true, Files.isRegularFile(dir.resolve("raster/square.png")));
        }));
        run("concrete renderers also enforce the size contract", () -> withTempDirectory(dir -> {
            for (Renderer renderer : List.of(new VectorRenderer(dir), new RasterRenderer(dir))) {
                for (int size : new int[] {-1, 0, 1001, Integer.MAX_VALUE}) {
                    throwsType(IllegalArgumentException.class, () -> renderer.drawCircle(size));
                    throwsType(IllegalArgumentException.class, () -> renderer.drawSquare(size));
                }
            }
            try (var entries = Files.list(dir)) {
                equal(0L, entries.count());
            }
        }));
        run("file failures are reported to the caller", () -> withTempDirectory(dir -> {
            Path blocked = Files.writeString(dir.resolve("not-a-directory"), "occupied");
            for (Renderer renderer : List.of(new VectorRenderer(blocked), new RasterRenderer(blocked))) {
                throwsType(IOException.class, () -> new Circle(10, renderer).draw());
                throwsType(IOException.class, () -> new Square(10, renderer).draw());
            }
        }));
        run("null output directories are rejected", () -> {
            throwsType(NullPointerException.class, () -> new VectorRenderer(null));
            throwsType(NullPointerException.class, () -> new RasterRenderer(null));
        });
        run("SVG numbers do not depend on the machine locale", () -> withTempDirectory(dir -> {
            Locale previous = Locale.getDefault();
            try {
                Locale.setDefault(Locale.forLanguageTag("ar-EG"));
                new Circle(40, new VectorRenderer(dir)).draw();
                new Square(80, new VectorRenderer(dir)).draw();
                equal("104", readSvg(dir.resolve("circle.svg")).getAttribute("width"));
                equal("104", readSvg(dir.resolve("square.svg")).getAttribute("width"));
            } finally {
                Locale.setDefault(previous);
            }
        }));
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

    @FunctionalInterface
    private interface DirectoryAction {
        void run(Path directory) throws Exception;
    }

    private static void withTempDirectory(DirectoryAction action) throws Exception {
        Path directory = Files.createTempDirectory("bridge-test-");
        try {
            action.run(directory);
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private static Element readSvg(Path path) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        var document = factory.newDocumentBuilder().parse(path.toFile());
        Element root = document.getDocumentElement();
        equal("svg", root.getTagName());
        return root;
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
