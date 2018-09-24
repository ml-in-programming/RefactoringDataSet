package org.jetbrains.research.groups.ml_methods.refactoring.detection.results;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

public class RepositoryDetectionSuccess extends RepositoryDetectionResult {
    @NotNull
    private transient final ResultsStatistics statistics;
    @NotNull
    private final List<CommitDetectionSuccess> commitDetectionSuccesses;
    @NotNull
    private final List<CommitDetectionFailed> commitDetectionFailures;

    public RepositoryDetectionSuccess(@NotNull URL repository,
                                      @NotNull String branch,
                                      @NotNull List<CommitDetectionSuccess> commitDetectionSuccesses,
                                      @NotNull List<CommitDetectionFailed> commitDetectionFailures) {
        super(repository, branch);
        this.commitDetectionSuccesses = commitDetectionSuccesses;
        this.commitDetectionFailures = commitDetectionFailures;
        statistics = new ResultsStatistics(commitDetectionSuccesses, commitDetectionFailures);
    }

    @NotNull
    public List<CommitDetectionSuccess> getCommitDetectionSuccesses() {
        return commitDetectionSuccesses;
    }

    @NotNull
    public List<CommitDetectionFailed> getCommitDetectionFailures() {
        return commitDetectionFailures;
    }

    @Override
    public String toString() {
        String out = super.toString();
        out += "Result: success\n";
        out += statistics;
        return out;
    }

}
