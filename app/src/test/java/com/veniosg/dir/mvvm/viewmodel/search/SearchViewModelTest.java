package com.veniosg.dir.mvvm.viewmodel.search;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.veniosg.dir.mvvm.model.search.SearchState;
import com.veniosg.dir.mvvm.model.search.Searcher;
import com.veniosg.dir.mvvm.viewmodel.search.SearchViewModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;

import static com.veniosg.dir.mvvm.model.search.Searcher.SearchRequest.searchRequest;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SearchViewModelTest {
    @Mock
    private Searcher mockSearcher;
    private SearchViewModel viewModel;
    private final File searchRoot = new File("/");

    @Before
    public void setUp() {
        initMocks(this);
        viewModel = new SearchViewModel(mockSearcher);
    }

    @Test
    public void delegatesSearchResultsToSearcher() {
        LiveData<SearchState> searcherLiveResults = new MutableLiveData<>();
        when(mockSearcher.getResults()).thenReturn(searcherLiveResults);

        viewModel.init(searchRoot);

        assertEquals(searcherLiveResults, viewModel.getLiveResults());
    }

    @Test
    public void onClearedCleansUp() {
        viewModel.onCleared();

        verify(mockSearcher).stopSearch();
    }

    @Test
    public void updateQueryDelegatesToSearcher() {
        String newQuery = "query2";
        viewModel.init(searchRoot);

        viewModel.updateQuery(newQuery);

        verify(mockSearcher).updateQuery(refEq(searchRequest(searchRoot, newQuery)));
    }

    @Test
    public void onlyStartsOneSearchForSameQuery() {
        String query = "query";
        viewModel.init(searchRoot);

        boolean firstSearchStarted = viewModel.updateQuery(query);
        boolean secondSearchStarted = viewModel.updateQuery(query);

        assertTrue(firstSearchStarted);
        assertFalse(secondSearchStarted);
        verify(mockSearcher).updateQuery(refEq(searchRequest(searchRoot, query)));
    }
}