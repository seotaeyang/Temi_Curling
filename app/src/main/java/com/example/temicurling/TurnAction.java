package com.example.temicurling;

import com.robotemi.sdk.Robot;
public class TurnAction {
    private Robot robot;

    public TurnAction(Robot robot) {
        this.robot = robot;
    }

    public void rotate90Degrees() {
        robot.turnBy(90, 1.0f);
    }
}
