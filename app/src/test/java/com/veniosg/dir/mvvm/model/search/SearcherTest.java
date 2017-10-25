package com.veniosg.dir.mvvm.model.search;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.MutableLiveData;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;

import static com.veniosg.dir.mvvm.model.search.Searcher.SearchRequest.request;
import static io.reactivex.schedulers.Schedulers.trampoline;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class SearcherTest {
    private File testFileRoot;
    private Searcher searcher;

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    @Mock
    private MutableLiveData<SearchState> mockResults;
    private File file1;
    private File file2;
    private File file3;
    private File file4;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        initMocks(this);

        testFileRoot = new File("testRoot");
        testFileRoot.mkdir();
        File dir1 = new File(testFileRoot, "dir1");
        dir1.mkdir();
        File dir2 = new File(testFileRoot, "dir2");
        dir2.mkdir();
        file1 = new File(dir1, "file");
        file1.createNewFile();
        file2 = new File(dir1, "file2");
        file2.createNewFile();
        file3 = new File(dir2, "file");
        file3.createNewFile();
        file4 = new File(testFileRoot, "file");
        file4.createNewFile();

        searcher = new Searcher(mockResults, trampoline(), trampoline());
    }

    @Test
    public void testFindsAll() throws Exception {
        searcher.updateQuery(request(testFileRoot, "file"));

        verifyFoundInOrder(mockResults, file4, file1, file3);
    }

    @Test
    public void testFindsOnlyOne() throws Exception {
        searcher.updateQuery(request(testFileRoot, "file2"));

        verifyFoundInOrder(mockResults, file2);
    }

    @Test
    public void notifiesEmpty() throws Exception {
        SearchState searchState = new SearchState();
        searchState.setFinished();

        searcher.updateQuery(request(testFileRoot, "file12455"));

        verify(mockResults).setValue(refEq(searchState));
    }

    @After
    public void tearDown() throws Exception {
        deleteRecursive(testFileRoot);
    }

    @SuppressWarnings("unchecked")
    private void verifyFoundInOrder(MutableLiveData<SearchState> mockObservable,
                                    File... expectedFiles) {
        InOrder inOrder = inOrder(mockObservable);

        // Search updates verification
        SearchState searchState = new SearchState();
        for (File file : expectedFiles) {
            searchState.addResult(file.getAbsolutePath());
            inOrder.verify(mockObservable).setValue(refEq(searchState));
        }

        // Search completion verification
        searchState.setFinished();
        inOrder.verify(mockObservable).setValue(refEq(searchState));
    }

    private void deleteRecursive(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteRecursive(c);
        }
        if (f.delete()) {
            f.deleteOnExit();
        }
    }
}