package com.veniosg.dir.mvvm.model.search;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.veniosg.dir.android.util.FileUtils;

import org.reactivestreams.Subscription;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;

import io.reactivex.Emitter;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Scheduler;

import static com.veniosg.dir.android.util.FileUtils.isSymlink;
import static com.veniosg.dir.android.util.Logger.log;
import static io.reactivex.BackpressureStrategy.BUFFER;
import static io.reactivex.Flowable.create;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;
import static java.util.Collections.addAll;
import static java.util.Locale.ROOT;

public class Searcher {
    @NonNull
    private final SearcherLiveData observableResults;
    @NonNull
    private final Scheduler ioScheduler;
    @NonNull
    private final Scheduler uiScheduler;
    @NonNull
    private final SearchState searchState = new SearchState();
    @NonNull
    private final FlowableSubscriber<? super String> bfsUpdateSubscriber = new BfsSubscriber();
    private BfsFlowable bfsFlowable;
    private Object lock;

    public Searcher() {
        ioScheduler = io();
        uiScheduler = mainThread();
        observableResults = new SearcherLiveData(this);
    }

    @VisibleForTesting()
    Searcher(@NonNull SearcherLiveData observableResults,
             @NonNull Scheduler ioScheduler, @NonNull Scheduler uiScheduler) {
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
        this.observableResults = observableResults;
    }

    public LiveData<SearchState> getResults() {
        return observableResults;
    }

    public void updateQuery(SearchRequest request) {
        if (bfsFlowable != null) {
            stopSearch();
            searchState.reset();
            emitStateUpdate();
        }

        bfsFlowable = new BfsFlowable(request.searchRoot, request.query);
        create(bfsFlowable, BUFFER)
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler)
                .subscribe(bfsUpdateSubscriber);
    }

    public void stopSearch() {
        unlock();
        if (bfsFlowable != null) bfsFlowable.stopSearching();
    }

    public void resumeSearch() {
        unlock();
    }

    public void pauseSearch() {
        if (lock == null) lock = new Object();
    }

    private void unlock() {
        if (lock != null) {
            try {
                lock.notifyAll();
            } catch (IllegalMonitorStateException e) {
                log(e);
            } finally {
                lock = null;
            }
        }
    }

    private void emitStateUpdate() {
        observableResults.setValue(new SearchState(searchState));
    }

    public static class SearchRequest {
        String searchRoot;
        String query;

        SearchRequest(@NonNull File searchRoot, String query) {
            this.searchRoot = searchRoot.getAbsolutePath();
            this.query = query;
        }

        public static SearchRequest searchRequest(@NonNull File searchRoot, String query) {
            return new SearchRequest(searchRoot, query);
        }
    }

    private class BfsFlowable implements FlowableOnSubscribe<String> {
        private final Deque<File> queue = new LinkedList<>();
        private final String searchRoot;
        private final String query;
        private boolean keepSearching = true;

        BfsFlowable(@NonNull String searchRoot, @NonNull String query) {
            this.searchRoot = searchRoot;
            this.query = query;
        }

        @Override
        public void subscribe(FlowableEmitter<String> emitter) {
            if (query.isEmpty()) {
                emitter.onComplete();
                return;
            }

            try {
                File root = new File(searchRoot);
                addDirectChildren(root, queue);

                while (!queue.isEmpty() && keepSearching) {
                    root = queue.removeFirst();
                    if (!isSymlink(root)) {
                        visit(root, emitter);

                        if (root.isDirectory()) {
                            addDirectChildren(root, queue);
                        }

                        if (lock != null) lock.wait();
                    }
                }

                emitter.onComplete();
            } catch (Exception ex) {
                log(ex);
                emitter.onError(ex);
            }
        }

        void stopSearching() {
            keepSearching = false;
        }

        private void visit(@NonNull File file, Emitter<String> e) {
            if (file.getName().toLowerCase(ROOT).contains(query.toLowerCase(ROOT))) {
                e.onNext(file.getAbsolutePath());
            }
        }

        private void addDirectChildren(File of, Deque<File> into) {
            File[] children = of.listFiles();
            if (children != null) {
                addAll(into, children);
            }
        }
    }

    private class BfsSubscriber implements FlowableSubscriber<String> {
        private Subscription subscription;

        @Override
        public void onSubscribe(Subscription s) {
            subscription = s;
            subscription.request(1);
        }

        @Override
        public void onNext(String s) {
            searchState.addResult(s);
            emitStateUpdate();
            subscription.request(1);
        }

        @Override
        public void onError(Throwable t) {
            searchState.setFinished();
            emitStateUpdate();
            subscription.cancel();
        }

        @Override
        public void onComplete() {
            searchState.setFinished();  // TODO make sure this is called if leaving midway through a search
            emitStateUpdate();
            subscription.cancel();
        }
    }
}
