package org.jetbrains.research.groups.ml_methods.dataset_generator;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.dataset_generator.writer.CsvWriter;

import java.io.IOException;

public class FeaturesExtractor {
    private final @NotNull CsvWriter writer;

    private final @NotNull ProjectInfo projectInfo;

    public FeaturesExtractor(
        final @NotNull CsvWriter writer,
        final @NotNull ProjectInfo projectInfo
    ) {
        this.writer = writer;
        this.projectInfo = projectInfo;
    }

    public void extractFeatures(final @NotNull PsiMethod method) throws IOException {
        writer.write(method, method.getContainingClass(), true, 0.);

        for (PsiClass target : projectInfo.possibleTargets(method)) {
            writer.write(method, target, false, 0.);
        }
    }
}
