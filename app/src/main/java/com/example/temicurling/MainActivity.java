package com.example.temicurling;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private String currentRobotId = null;
    private ValueEventListener currentListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_stone1).setOnClickListener(v -> selectRobot("Stone1"));
        findViewById(R.id.btn_stone2).setOnClickListener(v -> selectRobot("Stone2"));
        findViewById(R.id.btn_stone3).setOnClickListener(v -> selectRobot("Stone3"));
        findViewById(R.id.btn_stone4).setOnClickListener(v -> selectRobot("Stone4"));
        findViewById(R.id.btn_stone5).setOnClickListener(v -> selectRobot("Stone5"));
        findViewById(R.id.btn_stone6).setOnClickListener(v -> selectRobot("Stone6"));
    }

    private void selectRobot(String robotId) {
        currentRobotId = robotId;
        // Update the RobotController or Firebase listener to the new robotId
        // You might need to reinitialize or update the existing RobotController
        setupFirebaseListeners(robotId);
    }
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
                        long time = Long.parseLong(timeString);

                        // Implement the logic to act on the data
                        // ...

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
