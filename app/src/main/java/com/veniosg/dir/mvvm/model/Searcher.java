package com.veniosg.dir.mvvm.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

public class Searcher {
    private final MutableLiveData<FileHolder> result = new MutableLiveData<>();

    public Searcher() {
        // TODO move Utils.searchIn logic here
    }

    public LiveData<FileHolder> getResults(SearchConfig config) {
        return result;
    }

    public void pauseSearch() {

    }

    public void resumeSearch() {

    }

    public static class SearchConfig {

    }
}
