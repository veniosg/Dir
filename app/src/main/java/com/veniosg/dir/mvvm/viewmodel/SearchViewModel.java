package com.veniosg.dir.mvvm.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import com.veniosg.dir.mvvm.model.search.SearchState;
import com.veniosg.dir.mvvm.model.search.Searcher;

import java.io.File;

import javax.inject.Inject;

import static com.veniosg.dir.mvvm.model.search.Searcher.SearchRequest.request;

public class SearchViewModel extends ViewModel {
    @SuppressWarnings("WeakerAccess")
    @Inject Searcher searcher;
    private LiveData<SearchState> liveResults;
    private File searchRoot;

    @VisibleForTesting
    SearchViewModel(Searcher searcher) {
        this.searcher = searcher;
    }

    public void init(File searchIn) {
        if (liveResults != null) return;

        this.searchRoot = searchIn;
        liveResults = searcher.getResults();
    }

    public void updateQuery(String query) {
        searcher.updateQuery(request(searchRoot, query));
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
