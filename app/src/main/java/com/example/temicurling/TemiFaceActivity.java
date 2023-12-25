package com.example.temicurling;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.robotemi.sdk.Robot;

public class TemiFaceActivity extends AppCompatActivity {
    //    Mainactivity에 있던 내용 그대로 옮겨왔습니다.
    private String currentRobotId;
    private RobotController robotController;
    private ValueEventListener currentListener = null;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temi_face);

//        화면 전환될 때 로봇 이름 데이터를 받아옵니다.
        Intent i = getIntent();
        String tempRobotId = i.getStringExtra("robotId");
        int n = Character.getNumericValue(tempRobotId.charAt(tempRobotId.length()-1));

        ConstraintLayout bg = findViewById(R.id.background);
        if (n%2==0) {
            bg.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        }
        else {
            bg.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow));
        }

//        selectRobot에 있던 내용입니다.
        currentRobotId = tempRobotId;
        Robot robot = Robot.getInstance();
        Handler handler = new Handler();
        RobotController robotController = new RobotController(handler, robot);
        // You might need to reinitialize or update the existing RobotController
        setupFirebaseListeners(tempRobotId);
    }

//    Mainactivity에 있던 내용 그대로 옮겨왔습니다.
//    out값에 따라 얼굴이 바뀌는 로직을 추가했습니다.
    private void setupFirebaseListeners(String robotId) {
        // Remove the old listener if it exists
        if (currentListener != null && currentRobotId != null) {
            DatabaseReference oldRef = FirebaseDatabase.getInstance().getReference("temi/" + currentRobotId);
            oldRef.removeEventListener(currentListener);
        }

        // Reference to the new robot's data
        DatabaseReference robotDataRef = FirebaseDatabase.getInstance().getReference("temi/" + robotId);

        // Create a new ValueEventListener
        currentListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String angleString = dataSnapshot.child("angle").getValue(String.class);
                String distanceString = dataSnapshot.child("distance").getValue(String.class);
                String outString = dataSnapshot.child("out").getValue(String.class);
                String timeString = dataSnapshot.child("time").getValue(String.class);

                if (angleString != null && distanceString != null && outString != null && timeString != null) {
                    try {
                        float angle = Float.parseFloat(angleString);
                        float distance = Float.parseFloat(distanceString);
                        boolean out = Boolean.parseBoolean(outString);
                        float time = Float.parseFloat(timeString);

                        // Implement the logic to act on the data
                        // ...

//                        @@ 박창선 얼굴 변경 로직 @@
//                        out값이 true이면 우는 얼굴, false이면 웃는 얼굴
                        ImageView face = findViewById(R.id.face);
                        if (out) {
                            face.setImageResource(R.drawable.cry);
                        }
				        else {
                            face.setImageResource(R.drawable.smile);
                        }
//                        @@ 박창선 얼굴 변경 로직 @@

                    } catch (NumberFormatException e) {
                        Log.e("MainActivity", "Error parsing Firebase data for robot " + robotId, e);
                    }
                } else {
                    Log.w("MainActivity", "Waiting for complete data for robot " + robotId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", "Failed to read value for robot " + robotId + ": " + databaseError.toException());
            }
        };

        // Attach the listener to the new reference
        robotDataRef.addValueEventListener(currentListener);
        currentRobotId = robotId;  // Update the current robot ID
    }
}