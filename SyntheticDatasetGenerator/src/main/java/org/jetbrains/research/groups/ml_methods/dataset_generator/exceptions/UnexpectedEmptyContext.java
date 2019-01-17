package org.jetbrains.research.groups.ml_methods.dataset_generator.exceptions;

import org.jetbrains.annotations.NotNull;

public class UnexpectedEmptyContext extends Exception {
    public UnexpectedEmptyContext(final @NotNull String methodName) {
        super("Empty context for method: " + methodName);
    }
}
