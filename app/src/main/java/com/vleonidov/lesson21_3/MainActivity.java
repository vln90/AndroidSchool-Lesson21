package com.vleonidov.lesson21_3;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TimerService";

    private TextView mTimerTextView;

    private boolean mIsServiceBound;

    private Messenger mServiceMessenger;
    private Messenger mMainActivityMessenger = new Messenger(new InternalMainActivityHandler());

    public static final int MSG_UPDATE_TIMER_TEXT_VIEW = 202;
    public static final String EXTRA_TIMER = "EXTRA_TIMER";

    class InternalMainActivityHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TIMER_TEXT_VIEW:
                    Log.d(TAG, "handleMessage() called with: msg = [" + msg + "]");

                    Bundle bundle = msg.getData();
                    String timerText = bundle.getString(EXTRA_TIMER);

                    mTimerTextView.setText(timerText);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected() called with: componentName = [" + componentName + "], iBinder = [" + iBinder + "]");

            mServiceMessenger = new Messenger(iBinder);
            mIsServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected() called with: componentName = [" + componentName + "]");

            mIsServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimerTextView = findViewById(R.id.timer_text_view);

        findViewById(R.id.start_service_button).setOnClickListener(this);
        findViewById(R.id.stop_service).setOnClickListener(this);
        findViewById(R.id.bind_service).setOnClickListener(this);
        findViewById(R.id.unbind_service).setOnClickListener(this);
        findViewById(R.id.do_work_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_service_button:

                Intent intentStartService = new Intent(MainActivity.this, TimerService.class);
                startService(intentStartService);
                break;
            case R.id.stop_service:
                Intent intentStopServiceByAction = new Intent(MainActivity.this, TimerService.class);
                intentStopServiceByAction.setAction(BoundService.ACTION_CLOSE);
                startService(intentStopServiceByAction);
                break;
            case R.id.bind_service:
                Intent bindIntent = new Intent(this, TimerService.class);
                bindService(bindIntent, mServiceConnection, BIND_AUTO_CREATE);
                break;
            case R.id.unbind_service:
                if (mIsServiceBound) {
                    unbindService(mServiceConnection);

                    mIsServiceBound = false;
                }
                break;
            case R.id.do_work_button:
                if (mIsServiceBound) {
                    Message message = Message.obtain(null, TimerService.MSG_START_TIMER);
                    message.replyTo = mMainActivityMessenger;
                    try {
                        mServiceMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
