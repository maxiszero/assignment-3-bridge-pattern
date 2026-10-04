package bridge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;

/** Concrete Implementor: describes geometry as resolution-independent SVG. */
public final class VectorRenderer implements Renderer {
    private final DrawingFiles files;

    public VectorRenderer(Path directory) {
        files = new DrawingFiles(directory);
    }

    @Override
    public void drawCircle(int radius) throws IOException {
        Dimensions.requireValid(radius, "radius");
        int center = radius + DrawingStyle.PADDING;
        String element = String.format(Locale.ROOT,
                "<circle cx=\"%d\" cy=\"%d\" r=\"%d\" fill=\"#%06x\"/>",
                center, center, radius, DrawingStyle.FILL_RGB);
        writeSvg("circle.svg", 2 * center, element);
    }

    @Override
    public void drawSquare(int side) throws IOException {
        Dimensions.requireValid(side, "side");
        String element = String.format(Locale.ROOT,
                "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"#%06x\"/>",
                DrawingStyle.PADDING, DrawingStyle.PADDING, side, side, DrawingStyle.FILL_RGB);
        writeSvg("square.svg", side + 2 * DrawingStyle.PADDING, element);
    }

    private void writeSvg(String filename, int size, String element) throws IOException {
        String svg = String.format(Locale.ROOT, """
                <svg xmlns="http://www.w3.org/2000/svg" width="%d" height="%d" viewBox="0 0 %d %d">
                  <rect width="100%%" height="100%%" fill="white"/>
                  %s
                </svg>
                """, size, size, size, size, element);
        files.write(filename, svg.getBytes(StandardCharsets.UTF_8));
    }
}
