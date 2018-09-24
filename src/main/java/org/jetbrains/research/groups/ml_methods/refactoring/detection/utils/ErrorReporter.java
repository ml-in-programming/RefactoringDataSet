package org.jetbrains.research.groups.ml_methods.refactoring.detection.utils;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import static java.lang.System.exit;

public class ErrorReporter {

    public static void exitWithError(@NotNull String errorMessage, @NotNull Throwable e, @NotNull Class<?> aClass) {
        reportError(errorMessage, e, aClass);
        System.err.println("Application terminated.");
        exit(-1);
    }

    public static void printExceptionInformation(@NotNull Throwable e) {
        System.err.println("Error type: " + e.getClass().getCanonicalName());
        System.err.println("Reason: " + e.getMessage());
    }

    public static void reportError(@NotNull String errorMessage, @NotNull Throwable e, @NotNull Class<?> aClass) {
        System.err.println(errorMessage);
        Logger.getLogger(aClass).error(errorMessage, e);
        printExceptionInformation(e);
    }
}
