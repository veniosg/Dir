package com.veniosg.dir.mvvm.model.storage.operation.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.SparseLongArray;

import com.veniosg.dir.R;
import com.veniosg.dir.android.activity.FileManagerActivity;

import java.io.File;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.getActivity;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static java.lang.System.currentTimeMillis;

public class NotificationOperationStatusDisplayer implements OperationStatusDisplayer {
    private static final int LONG_OPERATION_MIN_DURATION_MS = 500;

    private final SparseLongArray startTimes = new SparseLongArray();
    private final NotificationManager notificationManager;
    private final Context context;

    NotificationOperationStatusDisplayer(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        this.context = context.getApplicationContext();
    }

    @Override
    public void showCopyProgress(int operationId, File destDir, File copying, int progress, int max) {
        Notification notification = generateOperationProgressNotification(
                context.getString(R.string.copying),
                context.getString(R.string.notif_copying_item, copying.getName(), destDir.getAbsolutePath()),
                copying,
                progress, max);

        show(operationId, notification);
    }

    @Override
    public void showCopySuccess(int operationId, File destDir) {
        if (isLongOperation(operationId)) {
            String msg = context.getString(R.string.copied);
            Notification notification = generateOperationDoneNotification(destDir, msg);
            show(operationId, notification);
        } else {
            hide(operationId);
        }

        clearOperationTimer(operationId);
    }

    @Override
    public void showCopyFailure(int operationId, File destDir) {
        String msg = context.getString(R.string.copy_error);
        Notification notification = generateOperationDoneNotification(destDir, msg);
        show(operationId, notification);

        clearOperationTimer(operationId);
    }

    @Override
    public void showMoveProgress(int operationId, File destDir, File moving, int progress, int max) {
        Notification notification = generateOperationProgressNotification(
                context.getString(R.string.moving),
                context.getString(R.string.notif_moving_item, moving.getName(), destDir.getAbsolutePath()),
                moving,
                progress, max);

        show(operationId, notification);
    }

    @Override
    public void showMoveSuccess(int operationId, File destDir) {
        if (isLongOperation(operationId)) {
            String msg = context.getString(R.string.moved);
            Notification notification = generateOperationDoneNotification(destDir, msg);
            show(operationId, notification);
        } else {
            hide(operationId);
        }

        clearOperationTimer(operationId);
    }

    @Override
    public void showMoveFailure(int operationId, File destDir) {
        String msg = context.getString(R.string.move_error);
        Notification notification = generateOperationDoneNotification(destDir, msg);
        show(operationId, notification);

        clearOperationTimer(operationId);
    }

    private Notification generateOperationProgressNotification(String title,
                                                               String longText,
                                                               File operatingOn,
                                                               int progress, int max) {
        return new NotificationCompat.Builder(context)
                .setAutoCancel(false)
                .setContentTitle(title)
                .setContentText(operatingOn.getName())
                .setProgress(max, progress, false)
                .setOngoing(true)
                .setPriority(PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_stat_notify_paste)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(longText))
                .setTicker(title)
                .setOnlyAlertOnce(true)
                .build();
    }

    @NonNull
    private Notification generateOperationDoneNotification(File destDir, String msg) {
        return new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentTitle(msg)
                .setContentText(destDir.getAbsolutePath())
                .setContentIntent(browsePendingIntent(destDir))
                .setOngoing(false)
                .setPriority(PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_stat_notify_paste_5)
                .setTicker(msg)
                .build();
    }

    @NonNull
    private PendingIntent browsePendingIntent(File destDir) {
        Intent browseIntent = new Intent(context, FileManagerActivity.class);
        browseIntent.setFlags(
                FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);
        browseIntent.setData(Uri.fromFile(destDir));
        return getActivity(context, 0, browseIntent, FLAG_CANCEL_CURRENT);
    }

    private void show(int operationId, @NonNull Notification notification) {
        notificationManager.notify(operationId, notification);

        initOperationTimer(operationId);
    }

    private void hide(int operationId) {
        notificationManager.cancel(operationId);
    }

    private boolean isLongOperation(int operationId) {
        long now = currentTimeMillis();
        long duration = now - startTimes.get(operationId, now);
        return duration >= LONG_OPERATION_MIN_DURATION_MS;
    }

    private void initOperationTimer(int operationId) {
        if (startTimes.get(operationId) == 0) {
            startTimes.put(operationId, currentTimeMillis());
        }
    }

    private void clearOperationTimer(int operationId) {
        startTimes.delete(operationId);
    }
}
