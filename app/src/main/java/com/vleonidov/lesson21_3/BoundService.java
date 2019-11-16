package com.vleonidov.lesson21_3;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * @author Леонидов Василий on 2019-11-16
 */
public class BoundService extends Service {

    private static final String TAG = "BoundService";

    public static final String ACTION_CLOSE = "ACTION_CLOSE";

    private IBinder mLocalBinder = new LocalBinder();

    class LocalBinder extends Binder {
        BoundService getBoundService() {
            return BoundService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate() called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");

        if (intent != null && ACTION_CLOSE.equals(intent.getAction())) {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called with: intent = [" + intent + "]");

        return mLocalBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);

        Log.d(TAG, "onRebind() called with: intent = [" + intent + "]");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind() called with: intent = [" + intent + "]");

        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy() called");
    }

    public void doSomeWork() {
        Log.d(TAG, "doSomeWork() called");
    }

    public void doAnotherWork() {
        Log.d(TAG, "doAnotherWork() called");
    }
}
