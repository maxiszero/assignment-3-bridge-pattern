package bridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Common filesystem policy, shared by both concrete renderers. */
final class DrawingFiles {
    private final Path directory;

    DrawingFiles(Path directory) {
        this.directory = Objects.requireNonNull(directory, "directory");
    }

    void write(String filename, byte[] content) throws IOException {
        Files.createDirectories(directory);
        Files.write(directory.resolve(filename), content);
    }
}
