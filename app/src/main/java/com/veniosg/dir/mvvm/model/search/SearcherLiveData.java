package com.veniosg.dir.mvvm.model.search;

import android.arch.lifecycle.LiveData;

public class SearcherLiveData extends LiveData<SearchState> {
    private final Searcher searcher;

    SearcherLiveData(Searcher searcher) {
        this.searcher = searcher;
    }

    @Override
    protected void setValue(SearchState value) {
        super.setValue(value);
    }

    @Override
    protected void onActive() {
        searcher.resumeSearch();
    }

    @Override
    protected void onInactive() {
        searcher.pauseSearch();
    }
}
