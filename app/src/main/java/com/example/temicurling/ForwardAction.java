package com.example.temicurling;

import android.os.Handler;
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

    public void moveForwardForDuration(int durationMillis) {
        Runnable skidJoyRunnable = new Runnable() {
            @Override
            public void run() {
                float a = (0.5F / durationMillis * 1000);
                robot.skidJoy(currentSpeed, 0);
                currentSpeed = currentSpeed - a;

                if (currentSpeed > 0) {
                    handler.postDelayed(this, 500);
                }
            }
        };

        handler.postDelayed(skidJoyRunnable, 500);

        handler.postDelayed(() -> handler.removeCallbacks(skidJoyRunnable), durationMillis);
    }
}