package com.veniosg.dir.test.acceptance;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.veniosg.dir.android.activity.SearchActivity;
import com.veniosg.dir.test.actor.Android;
import com.veniosg.dir.test.actor.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;
import static com.veniosg.dir.test.TestUtils.cleanDirectory;
import static com.veniosg.dir.test.injector.ActorInjector.android;
import static com.veniosg.dir.test.injector.ActorInjector.user;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchActivityTest {
    @Rule
    public ActivityTestRule<SearchActivity> activityRule = new ActivityTestRule<>(
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
    public void showsHintForPath() throws Exception {
        user.launches().dir();
        user.selects().fileInList(testDirectory);
        user.selects().searchAction();

        user.sees().searchHintFor(testDirectory);
    }

    @Test
    public void showsCorrectEmptyViews() throws Exception {
        user.launches().searchIn(testDirectory);
        user.sees().searchHintView();

        user.types().searchQuery("querythatwillnotmatchanyfiles");
        user.sees().searchEmptyView();

        userDeletesQuery();
        user.sees().searchHintView();
    }

    @Test
    public void launchesDirectoryWhenResultIsSelected() throws Exception {
        user.launches().searchIn(testDirectory);

        user.types().searchQuery(testChildDirectory.getName());
        user.selects().searchResult(testChildDirectory);

        user.sees().fileInPath(testChildDirectory);
    }

    @Test
    public void launchesFileWhenResultIsSelected() throws Exception {
        user.launches().searchIn(testDirectory);

        user.types().searchQuery(testChildFile.getName());
        user.selects().searchResult(testChildFile);

        android.launched().viewFileIntent(testChildFile);
    }

    @Test
    public void updatesResultsWhenQueryUpdated() throws Exception {
        user.launches().searchIn(testDirectory);

        user.types().searchQuery(testChildDirectory.getName());
        user.sees().searchResult(testChildDirectory);

        user.types().searchQuery(testChildFile.getName());
        user.sees().searchResult(testChildFile);
        user.cannotSee().visibleSearchResult(testChildDirectory);
    }

    private void userDeletesQuery() {
        user.types().searchQuery("");
    }
}
