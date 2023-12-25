package com.example.temicurling;

import android.os.Handler;
import android.util.Log;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;


public class ForwardAction {
    private Handler handler;
    private Robot robot;
    private float currentSpeed = 1.0F;

    public ForwardAction(Handler handler, Robot robot) {
        this.handler = handler;
        this.robot = robot;
    }

    public void moveForwardForDuration(float durationMillis) {
        currentSpeed = 1.0F;
        Runnable skidJoyRunnable = new Runnable() {
            @Override
            public void run() {
                float a = (0.5F * 1000 / durationMillis);
                if (currentSpeed > 0){
                    currentSpeed = Math.max(0, currentSpeed - a);
                    robot.skidJoy(currentSpeed, 0);
                    handler.postDelayed(this, 500);
                }
                Log.d("ForwardAction", "currentspeed: " + currentSpeed);

            }
        };
        handler.postDelayed(skidJoyRunnable, 500);

        handler.postDelayed(() -> handler.removeCallbacks(skidJoyRunnable), (long)durationMillis);
    }
}