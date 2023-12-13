package com.example.temicurling;

import java.util.HashMap;
import java.util.Map;

public class GameController {
    private boolean isGameRunning;
    private int currentRound;
    private int totalRounds;

    // 각 스톤의 데이터를 저장하기 위한 Map
    private Map<String, StoneData> stonesData;

    public GameController(int totalRounds) {
        this.isGameRunning = false;
        this.currentRound = 0;
        this.totalRounds = totalRounds;
        this.stonesData = new HashMap<>();
        // 각 스톤에 대한 초기 데이터 설정
        initializeStonesData();
    }

    private void initializeStonesData() {
        for (int i = 1; i <= 6; i++) {
            stonesData.put("Stone" + i, new StoneData());
        }
    }

    public void startGame() {
        isGameRunning = true;
        currentRound = 1;
        // 게임 시작 로직
    }

    public void endGame() {
        isGameRunning = false;
        // 게임 종료 로직
    }

    public void nextRound() {
        if (currentRound < totalRounds) {
            currentRound++;
            // 라운드 변경 로직
        } else {
            endGame();
        }
    }

    // Firebase에서 받은 데이터를 기반으로 각 스톤의 상태 업데이트
    public void updateStoneData(String stoneName, String angle, String distance, int out, String time) {
        StoneData stone = stonesData.get(stoneName);
        if (stone != null) {
            stone.setAngle(angle);
            stone.setDistance(distance);
            stone.setOut(out);
            stone.setTime(time);
            // 여기에 스톤의 상태 변경에 따른 추가적인 로직을 구현할 수 있습니다.
        }
    }

    // 여기에 추가적인 게임 로직 및 메서드 구현

    // 각 스톤의 데이터를 저장하는 내부 클래스
    private class StoneData {
        private String angle;
        private String distance;
        private int out;
        private String time;

        // angle에 대한 getter 및 setter
        public String getAngle() {
            return angle;
        }

        public void setAngle(String angle) {
            this.angle = angle;
        }

        // distance에 대한 getter 및 setter
        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        // out에 대한 getter 및 setter
        public int getOut() {
            return out;
        }

        public void setOut(int out) {
            this.out = out;
        }

        // time에 대한 getter 및 setter
        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

}

