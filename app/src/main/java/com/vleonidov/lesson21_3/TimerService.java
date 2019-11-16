package com.vleonidov.lesson21_3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.vleonidov.lesson21_3.MainActivity.MSG_UPDATE_TIMER_TEXT_VIEW;

/**
 * @author Леонидов Василий on 2019-11-12
 */
public class TimerService extends Service {

    private static final String TAG = "TimerService";

    private static final String CHANNEL_ID = "Channel_1";

    private static final int NOTIFICATION_ID = 1;

    private static final String ACTION_CLOSE = "TimerServiceActionClose";

    private CountDownTimer mCountDownTimer;

    public static final int MSG_START_TIMER = 201;

    private Messenger mMessenger = new Messenger(new InternalHandler());

    private Messenger mMainActivityMessenger;

    class InternalHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_START_TIMER:
                    mMainActivityMessenger = msg.replyTo;

                    startTimer();
                    break;
                default:
                    super.handleMessage(msg);
            }
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

                sendMessage(l);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish() called");

                stopForeground(true);
            }
        };
        mCountDownTimer.start();
    }

    private void sendMessage(long l) {
        String timerMessage = getString(R.string.notification_content_description, Long.toString(l / 1000));
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.EXTRA_TIMER, timerMessage);

        Message message = Message.obtain(null, MSG_UPDATE_TIMER_TEXT_VIEW);
        message.setData(bundle);

        try {
            mMainActivityMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called with: intent = [" + intent + "]");

        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind() called with: intent = [" + intent + "]");

        return super.onUnbind(intent);
    }

    public void startTimer() {
        startCountdownTimer(100000, 1000);

        startForeground(1, createNotification("100"));
    }
}
