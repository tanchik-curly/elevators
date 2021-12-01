package com.gachiMadElevator.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class SpeedControl {
    private static final AtomicInteger queueSpeed = new AtomicInteger(180);
    private static final AtomicInteger elevatorSpeed = new AtomicInteger(150);

    private SpeedControl() {}

    public static void update(int sliderValue) {
        queueSpeed.set(sliderValue);
        elevatorSpeed.set(sliderValue);
    }

    public static AtomicInteger getQueueSpeed() {
        return queueSpeed;
    }

    public static AtomicInteger getElevatorSpeed() {
        return elevatorSpeed;
    }
}
