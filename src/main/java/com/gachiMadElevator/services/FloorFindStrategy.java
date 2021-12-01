package com.gachiMadElevator.services;

import com.gachiMadElevator.helpers.StupidTuple;

import java.util.List;
import java.util.Queue;

public interface FloorFindStrategy {
    StupidTuple findNextFloor(List<Passenger> activePassengers, Queue<Passenger> waitingPassengers,
                              ElevatorStatus status, int id, Floor currentDestination,
                              Floor currentFloor, ElevatorDirection direction);
}
