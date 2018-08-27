package org.jetbrains.research.groups.ml_methods.refactoring.detection.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;

public class ParsingUtils {
    @NotNull
    public static String getProjectName(@NotNull URL repositoryUrl) {
        String[] splitBySlash = repositoryUrl.toString().split("/");
        return splitBySlash[splitBySlash.length - 1].split("\\.")[0];
    }

    @NotNull
    public static String getHttpLink(@NotNull URL repositoryUrl) {
        return repositoryUrl.toString().substring(0, repositoryUrl.toString().lastIndexOf("."));
    }

    @NotNull
    public static String getPathFromClassQualifiedName(@NotNull String classQualifiedName,
                                                       @NotNull String oldPathToFile) {
        String fileName = oldPathToFile.substring(oldPathToFile.lastIndexOf(File.separator) + 1).split("\\.")[0];
        return classQualifiedName.replace(".", File.separator).split(fileName)[0] + fileName + ".java";
    }
}
