package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.utils.exceptions;

import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

public class UnsupportedDirectoriesLayoutException extends Exception {
    public UnsupportedDirectoriesLayoutException(final @NotNull PsiDirectory directory) {
        super(directory.getName());
    }
}
