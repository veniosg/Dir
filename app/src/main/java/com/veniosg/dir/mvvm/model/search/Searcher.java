package com.veniosg.dir.mvvm.model.search;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import io.reactivex.Emitter;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Scheduler;

import static com.veniosg.dir.android.util.Logger.log;
import static io.reactivex.BackpressureStrategy.BUFFER;
import static io.reactivex.Flowable.create;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;
import static java.util.Collections.addAll;

public class Searcher {
    private final SearcherLiveData observableResults;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;
    private final SearchState searchState = new SearchState();
    private final FlowableSubscriber<? super String> bfsUpdateSubscriber = new BfsSubscriber();
    private BfsFlowable bfsFlowable;
    private Object lock;

    public Searcher() {
        this.ioScheduler = io();
        this.uiScheduler = mainThread();
        this.observableResults = new SearcherLiveData(this);
    }

    @VisibleForTesting()
    Searcher(SearcherLiveData observableResults, Scheduler ioScheduler, Scheduler uiScheduler) {
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
        this.observableResults = observableResults;
    }

    public LiveData<SearchState> getResults() {
        return observableResults;
    }

    public void pauseSearch() {
        if (lock == null) lock = new Object();
    }

    public void resumeSearch() {
        if (lock != null) {
            lock.notifyAll();
            lock = null;
        }
    }

    public void stopSearch() {
        resumeSearch();
        if (bfsFlowable != null) bfsFlowable.stopSearching();
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

        public static SearchRequest request(@NonNull File searchRoot, String query) {
            return new SearchRequest(searchRoot, query);
        }
    }

    private class BfsFlowable implements FlowableOnSubscribe<String> {
        private final Deque<File> queue = new ArrayDeque<>();
        private final String searchRoot;
        private final String query;
        private boolean keepSearching = true;

        BfsFlowable(@NonNull String searchRoot, @NonNull String query) {
            this.searchRoot = searchRoot;
            this.query = query;
        }

        @Override
        public void subscribe(FlowableEmitter<String> e) {
            try {
                File root = new File(searchRoot);
                queue.add(root);

                while (!queue.isEmpty() && keepSearching) {
                    root = queue.removeFirst();
                    visit(root, e);

                    if (root.isDirectory()) {
                        File[] children = root.listFiles();
                        if (children != null) {
                            addAll(queue, children);
                        }
                    }

                    if (lock != null) lock.wait();
                }

                e.onComplete();
            } catch (Exception ex) {
                log(ex);
                e.onError(ex);
            }
        }

        private void visit(@NonNull File file, Emitter<String> e) {
            if (file.getName().equalsIgnoreCase(query)) e.onNext(file.getAbsolutePath());
        }

        void stopSearching() {
            keepSearching = false;
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
            searchState.setFinished();
            emitStateUpdate();
            subscription.cancel();
        }
    }
}
