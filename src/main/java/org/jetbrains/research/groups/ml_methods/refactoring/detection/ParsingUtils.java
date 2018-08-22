package org.jetbrains.research.groups.ml_methods.refactoring.detection;

import java.net.URL;

class ParsingUtils {
    static String getProjectName(URL repositoryUrl) {
        String[] splitBySlash = repositoryUrl.toString().split("/");
        return splitBySlash[splitBySlash.length - 1].split("\\.")[0];
    }

    static String getClassNameFromQualifiedName(String qualifiedClassName) {
        String[] splitByDot = qualifiedClassName.split("\\.");
        return splitByDot[splitByDot.length - 1];
    }

    static String getClassPackageFromQualifiedName(String qualifiedClassName) {
        return qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf("."));
    }
}
