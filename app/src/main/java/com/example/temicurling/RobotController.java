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


    // 로봇과의 통신을 위한 메서드 및 변수

    public RobotController(Handler handler, Robot robot) {
        this.robot = robot;
        this.handler = handler;
        this.forwardAction = new ForwardAction(handler, robot);
        initializeRobotNames();
        robot.addOnDetectionStateChangedListener(this);
        setupFirebaseListeners();
    }

    private void setupFirebaseListeners(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        robotRef = FirebaseDatabase.getInstance().getReference("temi/" + robotId);
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

                        float angle = Float.parseFloat(angleString);
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
        setFirstPosition();
    }


    // 로봇의 움직임을 시작하는 메서드
    public void startMovement(int durationMillis) {
        forwardAction.moveForwardForDuration(durationMillis);
    }

    public void resetPosition(int robotNumber) {
        robot.goTo("home base");
    }

    // distance 를 durationMillis로 바꾸는 메소드
    private int convertDistanceToDurationMillis(float distance){
        //공식은 바뀔 수 있음. 일단 distance 10은 10초간 이동하는 것으로 대입
        return (int)(distance * 1000);
    }

    private void rotateRobot(String stoneName, int angle){
       Robot specificRobot = getRobotInstanceByName(stoneName);
       if(specificRobot != null){
           specificRobot.turnBy(angle, 1.0f);
       }
    }

    private synchronized void addActionToQueue(int stoneNumber, float angle, float distance, float time){
        Map<String, Object> actionData = new HashMap<>();
        actionData.put("robotNumber", stoneNumber);
        actionData.put("angle", angle);
        actionData.put("distance", distance);
        actionData.put("time", (double) time);
        actionQueue.offer(actionData);
        processNextAction();
    }
    private void processNextAction() {
        // Continue processing as long as there are actions in the queue
        while (!actionQueue.isEmpty()) {
            Map<String, Object> actionData = actionQueue.poll(); // Retrieve and remove the head of the queue
            try {
                executeAction(actionData); // Execute the action
            } catch (Exception e) {
                handleActionError(actionData, e); // Handle any errors
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
        String stoneName = (String) actionData.get("stoneNumber");
        int angle = (Integer) actionData.get("angle");
        float distance = (Float) actionData.get("distance");

        rotateRobot(stoneName, angle);
        forwardAction.moveForwardForDuration(convertDistanceToDurationMillis(distance));
    }


    @Nullable
    private Robot getRobotInstanceByName(String stoneName){
        for (Map.Entry<Integer, String> entry : robotIdToNameMap.entrySet()){
            if (entry.getValue().equals(stoneName)){
                return robots.get(entry.getKey());
            }
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

