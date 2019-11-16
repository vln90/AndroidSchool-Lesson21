package com.vleonidov.lesson21_3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * @author Леонидов Василий on 2019-11-12
 */
public class TimerService extends Service {

    private static final String TAG = "TimerService";

    private static final String CHANNEL_ID = "Channel_1";

    private static final int NOTIFICATION_ID = 1;

    private static final String ACTION_CLOSE = "TimerServiceActionClose";

    private CountDownTimer mCountDownTimer;

    private IBinder mLocalBinder = new TimerService.LocalBinder();

    private OnTimerChangedListener mOnTimerChangedListener;

    class LocalBinder extends Binder {
        TimerService getBoundService() {
            return TimerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        Log.d(TAG, "onCreate() called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");

        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            if (ACTION_CLOSE.equals(intent.getAction())) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;

                stopSelf();

                return START_NOT_STICKY;
            }
        }

        startCountdownTimer(100000, 1000);

        startForeground(startId, createNotification("100"));

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy() called");

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    private void updateNotification(String time) {
        Notification notification = createNotification(time);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }

    private Notification createNotification(String time) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent closeServiceIntent = new Intent(this, TimerService.class);
        closeServiceIntent.setAction(ACTION_CLOSE);
        PendingIntent closePendingIntent = PendingIntent.getService(this, 0, closeServiceIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notification_content_title))
                .setContentText(getString(R.string.notification_content_description, time))
                .setOnlyAlertOnce(true)
                .addAction(0, getString(R.string.notification_stop_service_action), closePendingIntent)
                .setContentIntent(pendingIntent);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(getString(R.string.notification_channel_description));
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void startCountdownTimer(long time, long period) {
        mCountDownTimer = new CountDownTimer(time, period) {
            @Override
            public void onTick(long l) {
                Log.d(TAG, "onTick() called with: l = [" + l / 1000 + "]");

                updateNotification(Long.toString(l / 1000));

                if (mOnTimerChangedListener != null) {
                    mOnTimerChangedListener.onTimerChanged(getString(R.string.notification_content_description,
                            Long.toString(l / 1000)));
                }
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish() called");

                stopForeground(true);
            }
        };
        mCountDownTimer.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called with: intent = [" + intent + "]");

        return mLocalBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind() called with: intent = [" + intent + "]");

        return super.onUnbind(intent);
    }

    public void startTimer(@NonNull OnTimerChangedListener onTimerChangedListener) {
        mOnTimerChangedListener = onTimerChangedListener;

        startCountdownTimer(100000, 1000);

        startForeground(1, createNotification("100"));
    }

    interface OnTimerChangedListener {
        void onTimerChanged(String timerText);
    }
}
