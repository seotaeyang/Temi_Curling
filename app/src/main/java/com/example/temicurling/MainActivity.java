package com.example.temicurling;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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

public class MainActivity extends AppCompatActivity {
    private ForwardAction forwardAction;
    private Handler handler;
    Robot robot = Robot.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handler와 ForwardAction 초기화
        handler = new Handler();
        forwardAction = new ForwardAction(handler, robot);
        int ms = 10000;

        // 버튼 클릭 이벤트 설정
        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 첫 번째 움직임 시작
                forwardAction.moveForwardForDuration(ms);

                // 첫 번째 움직임 후 지연을 주고 두 번째 움직임 시작
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        forwardAction.moveForwardForDuration(ms);
                    }
                }, ms + 500); // 첫 번째 움직임 지속 시간 + 추가 지연
            }
        });
    }
}