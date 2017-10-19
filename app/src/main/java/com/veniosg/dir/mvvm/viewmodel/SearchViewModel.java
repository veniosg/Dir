package com.veniosg.dir.mvvm.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.veniosg.dir.mvvm.model.FileHolder;
import com.veniosg.dir.mvvm.model.Searcher;
import com.veniosg.dir.mvvm.model.Searcher.SearchConfig;

import javax.inject.Inject;

public class SearchViewModel extends ViewModel {
    private Searcher searcher;
    private LiveData<FileHolder> liveResults;

    @Inject
    SearchViewModel(Searcher searcher) {
        this.searcher = searcher;
    }

    public void init() {
        if (liveResults != null) return;

        liveResults = searcher.getResults(new SearchConfig());
    }

    public void onForegrounded() {
        searcher.resumeSearch();
    }

    public void onBackgrounded() {
        searcher.pauseSearch();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Safety in case onBackgrounded() wasn't called, so we avoid stray search workers
        searcher.pauseSearch();
    }

    public LiveData<FileHolder> getLiveResults() {
        return liveResults;
    }
}
