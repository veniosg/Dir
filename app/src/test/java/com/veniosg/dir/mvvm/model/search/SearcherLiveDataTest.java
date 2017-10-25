package com.veniosg.dir.mvvm.model.search;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class SearcherLiveDataTest {
    @Mock
    private Searcher mockSearcher;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void liveData_testPausesWhenInactive() {
        SearcherLiveData liveData = new SearcherLiveData(mockSearcher);

        liveData.onInactive();

        verify(mockSearcher).pauseSearch();
    }

    @Test
    public void liveData_testResumesWhenActive() {
        SearcherLiveData liveData = new SearcherLiveData(mockSearcher);

        liveData.onActive();

        verify(mockSearcher).resumeSearch();
    }
}