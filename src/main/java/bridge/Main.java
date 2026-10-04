package bridge;

import java.io.IOException;
import java.nio.file.Path;

/** Client: selects implementations at runtime, then uses the Shape API. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java -cp build/classes bridge.Main [output-directory]");
            System.exit(2);
        }
        Path directory = Path.of(args.length == 0 ? "output" : args[0]);
        try {
            demonstrate(directory);
        } catch (IOException error) {
            System.err.println("Drawing failed: " + error.getMessage());
            System.exit(1);
        }
    }

    private static void demonstrate(Path directory) throws IOException {
        Renderer vector = new VectorRenderer(directory);
        Renderer raster = new RasterRenderer(directory);
        Shape circle = new Circle(40, vector);
        Shape square = new Square(80, vector);

        System.out.println("1. VectorRenderer: draw a circle and a square as SVG.");
        circle.draw();
        square.draw();

        System.out.println("2. Switch the same Shape objects to RasterRenderer.");
        circle.setRenderer(raster);
        square.setRenderer(raster);
        circle.draw();
        square.draw();

        System.out.println("3. Switch the same circle back to VectorRenderer.");
        circle.setRenderer(vector);
        circle.draw();

        System.out.println("No Shape was recreated; radius = 40 and side = 80 throughout.");
        System.out.println("Created circle.svg, square.svg, circle.png and square.png in:");
        System.out.println(directory.toAbsolutePath().normalize());
    }
}
