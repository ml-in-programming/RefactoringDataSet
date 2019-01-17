package org.jetbrains.research.groups.ml_methods.dataset_generator;

import JavaExtractor.Common.CommandLineValues;
import JavaExtractor.Common.Common;
import JavaExtractor.ExtractFeaturesTask;
import JavaExtractor.FeaturesEntities.ProgramFeatures;
import com.github.javaparser.ParseException;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.exceptions.UnexpectedEmptyContext;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils;
import org.kohsuke.args4j.CmdLineException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class PathContextExtractor {
    private static final @NotNull String METHODS_FILE_NAME = "methods.csv";

    private static final @NotNull String CLASSES_FILE_NAME = "classes.csv";

    private static final @NotNull String POINTS_FILE_NAME = "points.csv";

    private static final int MAX_PATH_LENGTH = 8;

    private static final int MAX_PATH_WIDTH = 2;

    final @NotNull Dataset dataset;

    public PathContextExtractor(final @NotNull Dataset dataset) {
        this.dataset = dataset;
    }

    public void extract(
        final @NotNull Path targetDir
    ) throws IOException, ParseException, CmdLineException, UnexpectedEmptyContext {
        targetDir.toFile().mkdirs();

        try (
            BufferedWriter writer = Files.newBufferedWriter(targetDir.resolve(METHODS_FILE_NAME), CREATE_NEW);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.RFC4180)
        ) {
            List<PsiMethod> methods = dataset.getMethods();
            for (int methodId = 0; methodId < methods.size(); methodId++) {
                PsiMethod method = methods.get(methodId);

                CommandLineValues cmdValues = new CommandLineValues(
                    "--max_path_length", Integer.toString(MAX_PATH_LENGTH),
                    "--max_path_width", Integer.toString(MAX_PATH_WIDTH)
                );

                ExtractFeaturesTask extractTask =
                    new ExtractFeaturesTask(cmdValues, method.getText());

                ArrayList<ProgramFeatures> methodsContexts = new ArrayList<>(
                    extractTask.extractSingleFile()
                        .stream()
                        .filter(it -> it.getName().equals(splitName(method)))
                        .limit(1)
                        .collect(Collectors.toList())
                );

                String pathContext = extractTask.featuresToString(methodsContexts);
                if (pathContext.isEmpty()) {
                    throw new UnexpectedEmptyContext();
                }

                csvPrinter.printRecord(
                    methodId,
                    MethodUtils.fullyQualifiedName(method),
                    pathContext,
                    getPathToContainingFile(method),
                    method.getNode().getStartOffset()
                );
            }
        }

        try (
            BufferedWriter writer = Files.newBufferedWriter(targetDir.resolve(CLASSES_FILE_NAME), CREATE_NEW);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.RFC4180)
        ) {
            List<PsiClass> classes = dataset.getClasses();
            for (int classId = 0; classId < classes.size(); classId++) {
                PsiClass clazz = classes.get(classId);

                csvPrinter.printRecord(
                    classId,
                    clazz.getQualifiedName(),
                    dataset.getIdsOfMethodsIn(clazz).stream().map(Object::toString).collect(Collectors.joining(" ")),
                    getPathToContainingFile(clazz),
                    clazz.getNode().getStartOffset()
                );
            }
        }

        try (
            BufferedWriter writer = Files.newBufferedWriter(targetDir.resolve(POINTS_FILE_NAME), CREATE_NEW);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.RFC4180)
        ) {
            for (Dataset.Point point : dataset.getPoints()) {
                csvPrinter.printRecord(
                    point.getMethodId(),
                    point.getClassId(),
                    point.getLabel()
                );
            }
        }
    }

    private @NotNull String splitName(final @NotNull PsiMethod method) {
        return Common.splitToSubtokens(method.getName())
            .stream()
            .collect(Collectors.joining(Common.internalSeparator));
    }

    private @NotNull Path getPathToContainingFile(final @NotNull PsiElement element) {
        return Paths.get(element.getProject().getBasePath()).relativize(
            Paths.get(element.getContainingFile().getVirtualFile().getCanonicalPath())
        );
    }
}
