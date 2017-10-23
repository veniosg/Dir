package com.veniosg.dir.mvvm.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.veniosg.dir.mvvm.model.search.SearchState;
import com.veniosg.dir.mvvm.model.search.Searcher;

import javax.inject.Inject;

public class SearchViewModel extends ViewModel {
    private Searcher searcher;
    private LiveData<SearchState> liveResults;

    @Inject
    SearchViewModel(Searcher searcher) {
        this.searcher = searcher;
    }

    public void init() {
        if (liveResults != null) return;

        liveResults = searcher.getResults();
    }

    public void updateQuery(String query) {
        searcher.updateQuery(query);
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
        searcher.stopSearch();
    }

    public LiveData<SearchState> getLiveResults() {
        return liveResults;
    }
}
