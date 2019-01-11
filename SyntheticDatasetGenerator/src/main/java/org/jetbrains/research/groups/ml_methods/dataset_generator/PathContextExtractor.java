package org.jetbrains.research.groups.ml_methods.dataset_generator;

import JavaExtractor.Common.CommandLineValues;
import JavaExtractor.Common.Common;
import JavaExtractor.ExtractFeaturesTask;
import JavaExtractor.FeaturesEntities.ProgramFeatures;
import com.github.javaparser.ParseException;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.utils.MethodUtils;
import org.kohsuke.args4j.CmdLineException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class PathContextExtractor {
    private static final @NotNull String METHODS_FILE_NAME = "methods.csv";

    private static final @NotNull String CLASSES_FILE_NAME = "classes.csv";

    private static final @NotNull String POINTS_FILE_NAME = "points.csv";

    private static final int MAX_PATH_LENGTH = 8;

    private static final int MAX_PATH_WIDTH = 2;

    final @NotNull ProjectInfo projectInfo;

    public PathContextExtractor(final @NotNull ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    public void extract(
        final @NotNull Path targetDir
    ) throws IOException, ParseException, CmdLineException {
        targetDir.toFile().mkdirs();

        Set<PsiClass> classes =
            Stream.concat(
                projectInfo.getMethodsAfterFiltration()
                    .stream()
                    .flatMap(it -> projectInfo.possibleTargets(it).stream()),
                projectInfo.getMethodsAfterFiltration()
                    .stream()
                    .map(PsiMember::getContainingClass)
            ).collect(Collectors.toSet());

        Set<PsiMethod> methods =
            classes.stream()
                .flatMap(it -> Arrays.stream(it.getMethods()))
                .collect(Collectors.toSet());

        Map<PsiMethod, Integer> idOfMethod = new HashMap<>();

        Map<PsiClass, Integer> idOfClass = new HashMap<>();

        try (
            BufferedWriter writer = Files.newBufferedWriter(targetDir.resolve(METHODS_FILE_NAME), CREATE_NEW);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.RFC4180)
        ) {
            int methodId = 0;
            for (PsiMethod method : methods) {
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
                if (!pathContext.isEmpty()) {
                    csvPrinter.printRecord(
                        methodId,
                        MethodUtils.fullyQualifiedName(method),
                        pathContext,
                        getPathToContainingFile(method),
                        method.getNode().getStartOffset()
                    );

                    idOfMethod.put(method, methodId);

                    methodId++;
                }
            }
        }

        try (
            BufferedWriter writer = Files.newBufferedWriter(targetDir.resolve(CLASSES_FILE_NAME), CREATE_NEW);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.RFC4180)
        ) {
            int classId = 0;
            for (PsiClass clazz : classes) {
                List<Integer> idsOfMethods = new ArrayList<>();
                for (PsiMethod method : clazz.getMethods()) {
                    if (!idOfMethod.containsKey(method)) {
                        continue;
                    }

                    idsOfMethods.add(idOfMethod.get(method));
                }

                if (idsOfMethods.isEmpty()) {
                    continue;
                }

                csvPrinter.printRecord(
                    classId,
                    clazz.getQualifiedName(),
                    idsOfMethods.stream().map(Object::toString).collect(Collectors.joining(" ")),
                    getPathToContainingFile(clazz),
                    clazz.getNode().getStartOffset()
                );

                idOfClass.put(clazz, classId);
                classId++;
            }
        }

        try (
            BufferedWriter writer = Files.newBufferedWriter(targetDir.resolve(POINTS_FILE_NAME), CREATE_NEW);
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.RFC4180)
        ) {
            for (PsiMethod method : projectInfo.getMethodsAfterFiltration()) {
                csvPrinter.printRecord(
                    idOfMethod.get(method),
                    idOfClass.get(method.getContainingClass()),
                    1
                );

                for (PsiClass target : projectInfo.possibleTargets(method)) {
                    if (!idOfClass.containsKey(target)) {
                        continue;
                    }

                    csvPrinter.printRecord(
                        idOfMethod.get(method),
                        idOfClass.get(target),
                        0
                    );
                }
            }
        }
    }

    private @NotNull String splitName(final @NotNull PsiMethod method) {
        return Common.splitToSubtokens(method.getName())
            .stream()
            .collect(Collectors.joining(Common.internalSeparator));
    }

    private @NotNull Path getPathToContainingFile(final @NotNull PsiElement element) {
        return Paths.get(projectInfo.getProject().getBasePath()).relativize(
            Paths.get(element.getContainingFile().getVirtualFile().getCanonicalPath())
        );
    }
}
