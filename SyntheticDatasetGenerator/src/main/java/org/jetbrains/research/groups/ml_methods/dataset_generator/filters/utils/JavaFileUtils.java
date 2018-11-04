package org.jetbrains.research.groups.ml_methods.dataset_generator.filters.utils;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

public class JavaFileUtils {
    private JavaFileUtils() {}

    public static @NotNull PsiDirectory getDirectoryWithRootPackageFor(final @NotNull PsiJavaFile file) {
        String packageName = file.getPackageName();
        String[] packageSequence;

        if ("".equals(packageName)) {
            packageSequence = new String[0];
        } else {
            packageSequence = packageName.split("\\.");
        }

        ArrayUtils.reverse(packageSequence);

        PsiDirectory directory = file.getParent();
        if (directory == null) {
            throw new IllegalStateException("File has no parent directory");
        }

        for (String packagePart : packageSequence) {
            if (!packagePart.equals(directory.getName())) {
                throw new IllegalStateException("Directories structure doesn't match with package");
            }

            directory = directory.getParentDirectory();
            if (directory == null) {
                throw new IllegalStateException("Directories structure doesn't match with package");
            }
        }

        return directory;
    }
}
