package org.jetbrains.research.groups.ml_methods.github_repos_searcher.properties;

import org.jetbrains.annotations.NotNull;

public class DefaultProperties implements Properties {
    private static final @NotNull DefaultProperties INSTANCE = new DefaultProperties();

    private DefaultProperties() { }

    public @NotNull
    static DefaultProperties getInstance() {
        return INSTANCE;
    }

    public @NotNull String getLanguage() {
        return "Java";
    }

    public @NotNull String getMustBeUpdatedSinceDate() {
        return "2018-01-01";
    }

    public int getStarsLowerBound() {
        return 1000;
    }
}
