package com.accessifiers.filebrowser.test.acceptance;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.accessifiers.filebrowser.android.activity.SearchActivity;
import com.accessifiers.filebrowser.test.actor.Android;
import com.accessifiers.filebrowser.test.actor.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;
import static com.accessifiers.filebrowser.test.TestUtils.cleanDirectory;
import static com.accessifiers.filebrowser.test.injector.ActorInjector.android;
import static com.accessifiers.filebrowser.test.injector.ActorInjector.user;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchActivityTest {
    @Rule
    public IntentsTestRule<SearchActivity> activityRule = new IntentsTestRule<>(
            SearchActivity.class, false, false);
    private final User user = user(activityRule);
    private final Android android = android(activityRule);
    private final File testDirectory = new File(getExternalStorageDirectory(), "testDir");
    private final File testChildDirectory = new File(testDirectory, "testChildDir");
    private final File testChildFile = new File(testDirectory, "testChildFile");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        testDirectory.mkdir();
        testChildDirectory.mkdir();
        testChildFile.createNewFile();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws Exception {
        cleanDirectory(testDirectory);
        testDirectory.delete();
    }

    @Test
    public void searchesFiles() throws Exception {
        user.launches().searchIn(testDirectory);

        user.types().searchQuery("test");
        user.types().imeAction();

        user.sees().searchResult(testChildDirectory);
        user.sees().searchResult(testChildFile);
    }

    @Test
    public void searchesInCurrentDirectoryOnly() throws Exception {
        user.launches().searchIn(testChildDirectory);

        user.types().searchQuery("test");
        user.cannotSee().visibleSearchResult(testChildDirectory);
        user.cannotSee().visibleSearchResult(testChildFile);
    }

    @Test
    public void showsCorrectHint() throws Exception {
        user.launches().searchIn(testDirectory);

        user.sees().searchHintFor(testDirectory);
    }

    @Test
    public void showsEmptyViewOnlyAfterQuery() throws Exception {
        user.launches().searchIn(testDirectory);
        user.cannotSee().searchEmptyView();

        user.types().searchQuery("querythatwillnotmatchanyfiles");
        user.types().imeAction();
        user.sees().searchEmptyView();

        userDeletesQuery();
        user.types().searchQuery(testChildFile.getName());
        user.types().imeAction();
        user.cannotSee().searchEmptyView();

        userDeletesQuery();
        user.types().imeAction();
        user.sees().searchEmptyView();
    }

    @Test
    public void launchesDirectoryWhenResultIsSelected() throws Exception {
        user.launches().searchIn(testDirectory);

        user.types().searchQuery(testChildDirectory.getName());
        user.types().imeAction();
        user.selects().searchResult(testChildDirectory);

        user.sees().fileInPath(testChildDirectory);
    }

    @Test
    public void launchesFileWhenResultIsSelected() throws Exception {
        user.launches().searchIn(testDirectory);

        user.types().searchQuery(testChildFile.getName());
        user.types().imeAction();
        user.selects().searchResult(testChildFile);

        android.launched().viewFileIntent(testChildFile);
    }

    @Test
    public void updatesResultsWhenQueryUpdated() throws Exception {
        user.launches().searchIn(testDirectory);

        user.types().searchQuery(testChildDirectory.getName());
        user.types().imeAction();
        user.sees().searchResult(testChildDirectory);

        userDeletesQuery();
        user.types().searchQuery(testChildFile.getName());
        user.types().imeAction();
        user.sees().searchResult(testChildFile);
        user.cannotSee().visibleSearchResult(testChildDirectory);
    }

    private void userDeletesQuery() {
        user.types().noSearchQuery();
    }
}
