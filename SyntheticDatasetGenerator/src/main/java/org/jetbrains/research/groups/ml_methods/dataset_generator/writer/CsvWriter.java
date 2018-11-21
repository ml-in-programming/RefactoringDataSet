package org.jetbrains.research.groups.ml_methods.dataset_generator.writer;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils.fullyQualifiedName;

public class CsvWriter implements Closeable {
    private final @NotNull CSVPrinter csvPrinter;

    public CsvWriter(final @NotNull String pathToFile) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(pathToFile));

        try {
            csvPrinter = new CSVPrinter(
                writer,
                CSVFormat.DEFAULT.withHeader(
                    "Original method name",
                    "Target class",
                    "LABEL",
                    "TRGT_CLS_MTHDS_CALLS_RATIO"
                )
            );
        } catch (IOException e) {
            writer.close();
            throw e;
        }
    }

    public void write(
        final @NotNull PsiMethod method,
        final @NotNull PsiClass target,
        final boolean label,
        final double feature) throws IOException {
        csvPrinter.printRecord(
            fullyQualifiedName(method),
            target.getQualifiedName(),
            label ? 1 : 0,
            feature
        );
    }

    @Override
    public void close() throws IOException {
        csvPrinter.close(true);
    }
}
