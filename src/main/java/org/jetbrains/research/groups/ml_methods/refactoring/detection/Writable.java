package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import java.io.IOException;
import java.nio.file.Path;

public interface Writable {
    void write(Path outputFilePath) throws IOException;
}
