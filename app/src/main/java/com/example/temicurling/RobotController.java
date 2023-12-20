package com.example.temicurling;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.navigation.model.SpeedLevel;
import com.robotemi.sdk.navigation.model.*;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


@RequiresApi(api = Build.VERSION_CODES.N)
public class RobotController implements OnDetectionStateChangedListener {
    // 로봇의 현재 상태를 나타내는 변수
    private int angle = 90;
    private float distance = 0.0F;
    private boolean out = false;
    private long time = 0L;
    private ForwardAction forwardAction;
    private Robot robot;
    private Handler handler;
    Queue<Integer> queue = new LinkedList<>();


    // 로봇과의 통신을 위한 메서드 및 변수

    public RobotController(Handler handler, Robot robot) {
        this.robot = robot;
        this.handler = handler;
        this.forwardAction = new ForwardAction(handler, robot);
        robot.addOnDetectionStateChangedListener(this);
        setupFirebaseListeners();
    }

    private void setupFirebaseListeners(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        for (int i = 1; i <= 6; i++){
            final int robotNumber = i; // 로봇의 각 번호
            databaseReference.child("robot" + robotNumber).addValueEventListener(new ValueEventListener(){
                @Override
                public void onDataChange(DataSnapshot dataSnapshot){
                    try {
                        String angleString = dataSnapshot.child("angle").getValue(String.class);
                        String distanceString = dataSnapshot.child("distance").getValue(String.class);
                        String outString = dataSnapshot.child("out").getValue(String.class);
                        String timeString = dataSnapshot.child("time").getValue(String.class);

                        int angle = Integer.parseInt(angleString);
                        float distance = Float.parseFloat(distanceString);
                        boolean out = Boolean.parseBoolean(outString);
                        float time = Float.parseFloat(timeString);


                        if (out) {
                            resetPosition(robotNumber); // Reset position for specific robot
                        } else {
                            addActionToQueue(robotNumber, angle, distance, time);
                        }
                    } catch (NumberFormatException e){
                        Log.e("RobotController", "Error parsing Firebase data", e);
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error){
                    Log.e("Firebase", "DatabaseError: " + error.getMessage());
                }
            });
        }
    }


    // 로봇의 움직임을 시작하는 메서드
    public void startMovement(int durationMillis) {
        forwardAction.moveForwardForDuration(durationMillis);
    }

    public void resetPosition(int robotNumber) {
        robot.goTo("Home");
    }

    // distance 를 durationMillis로 바꾸는 메소드
    private int convertDistanceToDurationMillis(float distance){
        //공식은 바뀔 수 있음. 일단 distance 10은 10초간 이동하는 것으로 대입
        return (int)(distance * 1000);
    }

    private void rotateRobot(int robotNumber, int angle){
       Robot specificRobot = getRobotInstance(robotNumber);
       if(specificRobot != null){
           specificRobot.turnBy(angle, 1.0f);
       }
    }

    private void addActionToQueue(int robotNumber, int angle, float distance, float time){
        Map<String, Object> actionData = new HashMap<>();
        actionData.put("robotNumber", robotNumber);
        actionData.put("angle", angle);
        actionData.put("distance", distance);
        actionData.put("time", time);
        actionQueue.offer(actionData);
        processNextAction();
    }

    private void processNextAction(){
        if (!actionQueue.isEmpty()) {
            Map<String, Object> actionData = actionQueue.poll();
            executeAction(actionData);
        }
    }
    private PriorityQueue<Map<String,Object>> actionQueue = new PriorityQueue<>(
            Comparator.comparingInt(a -> (Integer) a.get("time"))
    );
    private void executeAction(Map<String, Object> actionData){
        int robotNumber = (Integer) actionData.get("robotNumber");
        int angle = (Integer) actionData.get("angle");
        float distance = (Float) actionData.get("distance");

        rotateRobot(robotNumber, angle);
        forwardAction.moveForwardForDuration(convertDistanceToDurationMillis(distance));
    }


    private Robot getRobotInstance(int robotNumber){
        switch (robotNumber){
            case 1://수정중
        }
        return null;
    }
    @Override
    public void onDetectionStateChanged(int state){
        switch(state){
            case OnDetectionStateChangedListener.DETECTED:
                robot.stopMovement();
                robot.turnBy(angle, 1.0F);

                int durationMillis = convertDistanceToDurationMillis(distance);
                handler.postDelayed(() -> forwardAction.moveForwardForDuration(durationMillis), 5000);
                break;

            case OnDetectionStateChangedListener.LOST:
                break;
            case OnDetectionStateChangedListener.IDLE:
                break;
        }
    }
    public String getCurrentStatus() {
        return String.format("Angle: %d, Distance: %.2f, Out: %b, Time: %d", angle, distance, out, time);
    }

    // 기타 필요한 메서드 추가 가능
}

