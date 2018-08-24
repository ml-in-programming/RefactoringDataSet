package org.jetbrains.research.groups.ml_methods.github_repos_searcher.properties;

import org.jetbrains.annotations.NotNull;

public interface Properties {
    @NotNull String getLanguage();

    @NotNull String getMustBeUpdatedSinceDate();

    int getStarsLowerBound();
}
