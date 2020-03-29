package com.airhacks.wad.control;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public interface FileScanner {
    static Optional<Path> scanForWarFile(Path baseDir) throws IOException {
        Path dir = Paths.get(baseDir + "/target");
        return Files.find(dir, 1, ((path, basicFileAttributes) -> {
            File file = path.toFile();
            return !file.isDirectory() && file.getName().endsWith(".war");
        })).findFirst();
    }
}
