package com.example.temicurling;

import android.content.Intent;
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
        Intent i = new Intent(getApplicationContext(), TemiFaceActivity.class);
        i.putExtra("robotId", robotId);
        startActivity(i);

//        currentRobotId = robotId;
//        // Update the RobotController or Firebase listener to the new robotId
//        // You might need to reinitialize or update the existing RobotController
//        setupFirebaseListeners(robotId);
    }
}
