package com.gachiMadElevator.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SpeedControl {
    private static final AtomicInteger queueSpeed = new AtomicInteger(50);
    private static final AtomicInteger elevatorSpeed = new AtomicInteger(150);

    private SpeedControl() {}

    public static void setElevatorSpeed(int sliderValue) {
        elevatorSpeed.set(sliderValue);
    }

    public static void setQueueSpeed(int sliderValue) {
        queueSpeed.set(sliderValue);
    }

    public static AtomicInteger getQueueSpeed() {
        return queueSpeed;
    }

    public static AtomicInteger getElevatorSpeed() {
        return elevatorSpeed;
    }
}
