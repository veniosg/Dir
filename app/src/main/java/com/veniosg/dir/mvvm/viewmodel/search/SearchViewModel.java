package com.veniosg.dir.mvvm.viewmodel.search;

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

    @SuppressWarnings("unused")
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

    /**
     * Start a new search, using the passed query. If the current search uses the same query, this does nothing.
     * @param query The text to search filenames for.
     * @return True if this triggers a new search, false if not.
     */
    public boolean updateQuery(@NonNull String query) {
        if (!query.equals(currentQuery)) {
            searcher.updateQuery(searchRequest(searchRoot, query));
            currentQuery = query;
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Avoid stray search workers
        searcher.stopSearch();
    }

    public LiveData<SearchState> getLiveResults() {
        return liveResults;
    }
}
