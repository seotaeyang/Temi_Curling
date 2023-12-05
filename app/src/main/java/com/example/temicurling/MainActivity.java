package com.example.temicurling;

<<<<<<< Updated upstream
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //
=======
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.TtsRequest;

public class MainActivity extends AppCompatActivity {
    Button btn;
    Handler handler;
    Robot robot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.startbutton);
        handler = new Handler(Looper.getMainLooper());
        robot = Robot.getInstance();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Temi를 90도 회전시키는 함수 호출
                robot.turnBy(90, 1.0f);

                // 회전 완료까지 대기
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 2초 대기
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
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

                                // 직진 완료까지 대기 (예: 10초)
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        handler.removeCallbacks(skidJoyRunnable);
                                    }
                                }, 10000); // 총 이동시간
                            }
                        }, 3000); // 회전 후 대기 시간
                    }
                }, 0);
            }
        });
    }
>>>>>>> Stashed changes
}