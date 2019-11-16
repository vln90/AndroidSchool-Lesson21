package com.vleonidov.lesson21_3;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BoundService";

    private boolean mIsServiceBound;
    private TimerService mBoundService;

    private TextView mTimerTextView;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected() called with: componentName = [" + componentName + "], iBinder = [" + iBinder + "]");

            mBoundService = ((TimerService.LocalBinder) iBinder).getBoundService();
            mIsServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected() called with: componentName = [" + componentName + "]");

            mBoundService = null;
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

//                Intent intent = new Intent(MainActivity.this, TimerService.class);
//                startService(intent);
                Intent intentStartService = new Intent(MainActivity.this, TimerService.class);
                startService(intentStartService);
                break;
            case R.id.stop_service:
//                Intent intentStopService = new Intent(MainActivity.this, BoundService.class);
//                stopService(intentStopService);
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
                    mBoundService = null;
                }
                break;
            case R.id.do_work_button:
                if (mIsServiceBound) {
//                    mBoundService.doSomeWork();
                    mBoundService.startTimer(new TimerService.OnTimerChangedListener() {
                        @Override
                        public void onTimerChanged(String timerText) {
                            mTimerTextView.setText(timerText);
                        }
                    });
                }
                break;
        }
    }
}
