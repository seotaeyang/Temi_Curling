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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class MainActivity extends AppCompatActivity implements
        OnRobotReadyListener,
        View.OnClickListener {
    Robot robot;
    Button startbutton;

    //Firebase의 데이터베이스
    private DatabaseReference mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Firebase Database 인스턴스 가져오기
        mDatabase = FirebaseDatabase.getInstance().getReference("/path/to/resource");

        // 데이터 변경을 감지하는 ValueEventListener 설정
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // 데이터가 변경될 때 호출됨
                // dataSnapshot에서 원하는 데이터를 가져와 사용
                String value = dataSnapshot.getValue(String.class);
                Log.d("FirebaseData", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // 데이터 가져오기를 실패했을 때 호출됨
                Log.w("FirebaseData", "Failed to read value.", error.toException());
            }
        });
        startbutton = findViewById(R.id.startbutton);
        robot = Robot.getInstance();
        startbutton.setOnClickListener(this);
    }
    @Override
    public void onClick(View view){
        Class exampleContext = null;
        if (view.getId() == R.id.startbutton) {
            exampleContext = MainActivity1.class;
        }
        Intent intent = new Intent(getApplicationContext(), exampleContext);
        startActivity(intent);
    }
    @Override
    protected void onStart(){
        super.onStart();
        robot.addOnRobotReadyListener(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        robot.removeOnRobotReadyListener(this);
    }
    @Override
    public void onRobotReady(boolean isReady){
        if(isReady){
            try{
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
                robot.onStart(activityInfo);
            } catch (PackageManager.NameNotFoundException e){
                throw new RuntimeException(e);
            }
        }
    }
}