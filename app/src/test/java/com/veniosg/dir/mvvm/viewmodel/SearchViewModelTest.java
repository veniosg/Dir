package com.veniosg.dir.mvvm.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.veniosg.dir.mvvm.model.search.SearchState;
import com.veniosg.dir.mvvm.model.search.Searcher;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;

import static com.veniosg.dir.mvvm.model.search.Searcher.SearchRequest.request;
import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.internal.verification.VerificationModeFactory.noMoreInteractions;

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
    public void onForegroundedResumesSearch() {
        viewModel.onForegrounded();

        verify(mockSearcher).resumeSearch();
    }

    @Test
    public void onBackgroundedPausesSearch() {
        viewModel.onBackgrounded();

        verify(mockSearcher).pauseSearch();
    }

    @Test
    public void onClearedPausesSearch() {
        viewModel.onCleared();

        verify(mockSearcher).stopSearch();
    }

    @Test
    public void updateQueryDelegatesToSearcher() {
        String newQuery = "query2";
        viewModel.init(searchRoot);

        viewModel.updateQuery(newQuery);

        verify(mockSearcher).updateQuery(refEq(request(searchRoot, newQuery)));
    }

    @Test
    public void onlyStartsOneSearchForSameQuery() {
        String query = "query";
        viewModel.init(searchRoot);

        viewModel.updateQuery(query);
        viewModel.updateQuery(query);

        verify(mockSearcher).updateQuery(refEq(request(searchRoot, query)));
        verify(mockSearcher, noMoreInteractions());
    }
}