package bridge;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

/** Concrete Implementor: paints pixels and encodes a PNG without a GUI. */
public final class RasterRenderer implements Renderer {
    private final DrawingFiles files;

    public RasterRenderer(Path directory) {
        files = new DrawingFiles(directory);
    }

    @Override
    public void drawCircle(int radius) throws IOException {
        Dimensions.requireValid(radius, "radius");
        int diameter = 2 * radius;
        writePng("circle.png", diameter, graphics -> graphics.fillOval(
                DrawingStyle.PADDING, DrawingStyle.PADDING, diameter, diameter));
    }

    @Override
    public void drawSquare(int side) throws IOException {
        Dimensions.requireValid(side, "side");
        writePng("square.png", side, graphics -> graphics.fillRect(
                DrawingStyle.PADDING, DrawingStyle.PADDING, side, side));
    }

    private void writePng(String filename, int extent, Consumer<Graphics2D> drawing)
            throws IOException {
        int size = extent + 2 * DrawingStyle.PADDING;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, size, size);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(DrawingStyle.FILL_RGB));
            drawing.accept(graphics);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", bytes)) {
            throw new IOException("No PNG encoder is available");
        }
        files.write(filename, bytes.toByteArray());
    }
}
