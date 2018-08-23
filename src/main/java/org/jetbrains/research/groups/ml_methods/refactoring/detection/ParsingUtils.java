package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import java.net.URL;

class ParsingUtils {
    static String getProjectName(URL repositoryUrl) {
        String[] splitBySlash = repositoryUrl.toString().split("/");
        return splitBySlash[splitBySlash.length - 1].split("\\.")[0];
    }

    static String getHttpLink(URL repositoryUrl) {
        return repositoryUrl.toString().substring(0, repositoryUrl.toString().lastIndexOf("."));
    }
}
