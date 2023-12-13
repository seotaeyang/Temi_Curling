package com.example.temicurling;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.robotemi.sdk.Robot;
public class ForwardAction {
    private Handler handler;
    private Robot robot;

    public ForwardAction(Handler handler, Robot robot) {
        this.handler = handler;
        this.robot = robot;
    }

    public void moveForwardForDuration(int durationMillis) {
        // 500ms 간격으로 작업을 반복하기 위한 Runnable
        Runnable skidJoyRunnable = new Runnable() {
            float k = 1;

            @Override
            public void run() {
                // run 반복 시 a만큼 감소
                float a = 0.05F;

                robot.skidJoy(k, 0);
                k = k - a;

                // 500ms 후에 다시 실행
                handler.postDelayed(this, 500);
            }
        };

        // 500ms 후에 직진을 시작
        handler.postDelayed(skidJoyRunnable, 500);

        // 직진 완료까지 대기
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(skidJoyRunnable);
            }
        }, durationMillis);
    }
}
