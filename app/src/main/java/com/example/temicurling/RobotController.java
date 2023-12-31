package com.example.temicurling;

import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.robotemi.sdk.listeners.OnMovementStatusChangedListener;

import java.util.HashMap;
import java.util.Map;


@RequiresApi(api = Build.VERSION_CODES.N)
public class RobotController implements OnDetectionStateChangedListener, OnMovementStatusChangedListener {
    // 로봇의 현재 상태를 나타내는 변수
    private int angle = 90;
    private float distance = 0.0F;
    private boolean out = false;
    private long time = 0L;
    private ForwardAction forwardAction;
    private Robot robot;
    private Handler handler;
    private String robotId;
    private DatabaseReference robotRef;
    private Map<Integer, String> robotIdToNameMap = new HashMap<>();

    private String firstPosition;
    private void initializeRobotNames(){
        robotIdToNameMap.put(1, "Stone1");
        robotIdToNameMap.put(2, "Stone2");
        robotIdToNameMap.put(3, "Stone3");
        robotIdToNameMap.put(4, "Stone4");
        robotIdToNameMap.put(5, "Stone5");
        robotIdToNameMap.put(6, "Stone6");
    }

    private Map<Integer, Robot> robots = new HashMap<>();

    private void initializeAndAddStone1(){
        Robot stone1Robot = Robot.getInstance();
        if(stone1Robot != null){
            robots.put(1, stone1Robot);
            Log.d("RobotController", "Stone1 로봇 Initialize 성공");
        } else{
            Log.e("RobotController", "Stone1 로봇 Initialize 실패");
        }
    }

    // 로봇과의 통신을 위한 메서드 및 변수

    public RobotController(Handler handler, Robot robot) {
        this.robot = robot;
        this.handler = handler;
        this.forwardAction = new ForwardAction(handler, robot);
        initializeRobotNames();
        initializeAndAddStone1();
        robot.addOnDetectionStateChangedListener(this);
        setupFirebaseListeners();
    }

    private void setupFirebaseListeners(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        robotRef = FirebaseDatabase.getInstance().getReference("temi/");
        for (int i = 1; i <= 6; i++){
            final int robotNumber = i; // 로봇의 각 번호
            robotRef.child("Stone" + robotNumber).addValueEventListener(new ValueEventListener(){
                @Override
                public void onDataChange(DataSnapshot dataSnapshot){
                    try {
                        String angleString = dataSnapshot.child("angle").getValue(String.class);
                        String distanceString = dataSnapshot.child("distance").getValue(String.class);
                        String outString = dataSnapshot.child("out").getValue(String.class);
                        String timeString = dataSnapshot.child("time").getValue(String.class);
                        Log.d("RobotController", "angle:" + angleString);
                        Log.d("RobotController", "distance:" + distanceString);
                        Log.d("RobotController", "out:" + outString);
                        Log.d("RobotController", "time:" + timeString);

                        if(angleString != null && distanceString != null && outString != null && timeString != null){
                            Log.d("RobotController","Firebase 값 불러오기 성공");
                            float angle = Float.parseFloat(angleString.trim());
                            float distance = Float.parseFloat(distanceString.trim());
                            boolean out = Boolean.parseBoolean(outString.trim());
                            float time = Float.parseFloat(timeString.trim());

                            if(time > 0 && distance > 0){
                                addActionToQueue("Stone" + robotNumber, angle, distance, time);
                                if(out){
                                    resetPosition(robotNumber);
                                }
                            }
                        } else{
                            Log.e("RobotController", "Firebase 값을 불러왔으나 null");
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
        setFirstPosition();
    }

    public void resetPosition(int robotNumber) {
        robot.goTo("home base");
    }

    // distance 를 durationMillis로 바꾸는 메소드
    private float convertDistanceToDurationMillis(float distance){
        //공식은 바뀔 수 있음. 일단 distance 10은 10초간 이동하는 것으로 대입
        return distance * 1000;
    }

    private void rotateRobot(String stoneName, float angle){
       Robot specificRobot = getRobotInstanceByName(stoneName);

       if(specificRobot != null){
           int intAngle = Math.round(angle) - 90;
           specificRobot.turnBy(intAngle, 1.0f);
       }
    }

    private synchronized void addActionToQueue(String robotName, float angle, float distance, float time){
        Map<String, Object> actionData = new HashMap<>();
        actionData.put("robotName", robotName);
        actionData.put("angle", angle);
        actionData.put("distance", distance);
        actionData.put("time", (float) time);
        actionQueue.offer(actionData);
        processNextAction();
    }
    private void processNextAction() {
        if(!actionQueue.isEmpty()){
            Map<String, Object> actionData = actionQueue.poll();
            try{
                executeAction(actionData);
            } catch(Exception e){
                handleActionError(actionData, e);
            }
        }
    }
    private PriorityBlockingQueue<Map<String,Object>> actionQueue = new PriorityBlockingQueue<>(
            10, new Comparator<Map<String, Object>>() {
        @Override
        public int compare(Map<String, Object> a, Map<String, Object> b) {
            return Double.compare((Double) a.get("time"), (Double) b.get("time"));
        }
    }
    );
    private void executeAction(Map<String, Object> actionData) throws Exception {
        String robotName = (String) actionData.get("robotName");
        Float distance = (Float) actionData.get("distance");
        Float angle = (Float) actionData.get("angle");
        Float time = (Float) actionData.get("time");

        if(robotName == null){
            Log.e("RobotController", "robotName이 null이거나 없음" +robotName);
            return;
        }
        if(angle != null && !angle.isNaN()){
            rotateRobot(robotName, angle);
        } else{
            Log.e("RobotController", "Invalid or null angle: " + angle);
        }
        if(distance != null && !distance.isNaN() && time != null && !time.isNaN() && distance > 0 && time > 0){
            Log.d("RObotController", "forwardAction 수행시작 " + distance);
            forwardAction.moveForwardForDuration(convertDistanceToDurationMillis(distance));
        } else{
            Log.e("RobotController", "distance, time 값 오류" + distance + "/" + time);
        }
    }

    @Nullable
    private Robot getRobotInstanceByName(String stoneName) {
        Integer robotId = null;
        for (Map.Entry<Integer, String> entry : robotIdToNameMap.entrySet()) {
            if (entry.getValue().equals(stoneName)) {
                robotId = entry.getKey();
                break;
            }
        }

        // If an ID was found, try to retrieve the corresponding Robot instance.
        if (robotId != null) {
            Robot robot = robots.get(robotId);
            if (robot != null) {
                return robot;
            } else {
                Log.e("RobotController", "Robot instance not found for ID: " + robotId);
            }
        } else {
            Log.e("RobotController", "No ID found for robot name: " + stoneName);
        }
        return null;
    }
    private void handleActionError(Map<String, Object> actionData, Exception e){
        //오류 처리 구현(선택)
        Log.e("RobotController", "에러가 동작중 발생" + actionData);
    }
    @Override
    public void onDetectionStateChanged(int state){
        switch(state){
            case OnDetectionStateChangedListener.DETECTED:
                robot.stopMovement();
                robot.turnBy(angle, 1.0F);

                float durationMillis = convertDistanceToDurationMillis(distance);
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
    public void setFirstPosition(){
        Robot stone1Robot = getRobotInstanceByName("Stone1");
        if (stone1Robot != null) {
            List<String> locations = stone1Robot.getLocations();
            if (locations != null && !locations.isEmpty()) {
                firstPosition = locations.get(0);
                Log.d("RobotController", "첫 위치 Stone1으로 설정" + firstPosition);
            } else {
                Log.e("RobotController", "첫 위치 설정 실패");
            }
        } else {
            Log.e("RobotController", "Stone1 인스턴스 없음.");
        }
    }

    private int currentMovingStone = 0;
    private void moveToFirstPositionSequentially(){
        if (currentMovingStone < 6) {
            currentMovingStone++;
            String stoneName = "Stone" + currentMovingStone;
            Robot stoneRobot = getRobotInstanceByName(stoneName);
            if (stoneRobot != null) {
                stoneRobot.goTo(firstPosition);
            } else {
                Log.e("RobotController", "로봇ID 오류" + stoneName);
            }
        }
    }



    @Override
    public void onMovementStatusChanged(@NonNull String type, @NonNull String status) {
        if (status == STATUS_START){
            moveToFirstPositionSequentially();
        }
    }
}

