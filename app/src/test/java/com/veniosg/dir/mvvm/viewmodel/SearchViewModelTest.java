package com.veniosg.dir.mvvm.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.veniosg.dir.mvvm.model.Searcher;
import com.veniosg.dir.mvvm.model.Searcher.SearchConfig;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SearchViewModelTest {
    @Mock
    private Searcher mockSearcher;
    private SearchViewModel viewModel;

    @Before
    public void setUp() {
        initMocks(this);
        viewModel = new SearchViewModel(mockSearcher);
    }

    @Test
    public void delegatesSearchResultsToSearcher() {
        LiveData searcherLiveResults = new MutableLiveData();
        when(mockSearcher.getResults(any(SearchConfig.class))).thenReturn(searcherLiveResults);

        viewModel.init();

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

        verify(mockSearcher).pauseSearch();
    }
}