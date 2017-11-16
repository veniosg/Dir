package com.veniosg.dir.mvvm.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.veniosg.dir.mvvm.model.search.SearchState;
import com.veniosg.dir.mvvm.model.search.Searcher;

import java.io.File;

import static com.veniosg.dir.mvvm.model.search.Searcher.SearchRequest.searchRequest;

public class SearchViewModel extends ViewModel {
    private Searcher searcher;
    private LiveData<SearchState> liveResults;
    private File searchRoot;
    private String currentQuery;

    public SearchViewModel() {
        searcher = new Searcher();
    }

    @VisibleForTesting
    SearchViewModel(Searcher searcher) {
        this.searcher = searcher;
    }

    public void init(File searchIn) {
        if (liveResults != null) return;

        this.searchRoot = searchIn;
        liveResults = searcher.getResults();
    }

    public void updateQuery(@NonNull String query) {
        if (!query.equals(currentQuery)) {
            searcher.updateQuery(searchRequest(searchRoot, query));
            currentQuery = query;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Safety in case onBackgrounded() wasn't called, to avoid stray search workers
        searcher.stopSearch();
    }

    public LiveData<SearchState> getLiveResults() {
        return liveResults;
    }
}
