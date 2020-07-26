package com.accessifiers.filebrowser.mvvm.model.search;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class SearchState {
    private boolean finished = false;
    private final List<String> results = new LinkedList<>();

    SearchState() {}

    SearchState(SearchState from) {
        finished = from.finished;
        results.addAll(from.results);
    }

    void addResult(String path) {
        results.add(path);
    }

    void setFinished() {
        finished = true;
    }

    void reset() {
        finished = false;
        results.clear();
    }

    public List<String> results() {
        return unmodifiableList(results);
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public String toString() {
        return "SearchState{" +
                "finished=" + finished +
                ", results=" + results +
                '}';
    }
}