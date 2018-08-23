package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

class ParsingUtils {
    @NotNull
    static String getProjectName(@NotNull URL repositoryUrl) {
        String[] splitBySlash = repositoryUrl.toString().split("/");
        return splitBySlash[splitBySlash.length - 1].split("\\.")[0];
    }

    @NotNull
    static String getHttpLink(@NotNull URL repositoryUrl) {
        return repositoryUrl.toString().substring(0, repositoryUrl.toString().lastIndexOf("."));
    }
}
