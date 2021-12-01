package com.gachiMadElevator.helpers;

import com.gachiMadElevator.services.ElevatorDirection;
import com.gachiMadElevator.services.ElevatorStatus;
import com.gachiMadElevator.services.Floor;

public class StupidTuple {
    public ElevatorStatus status;
    public Floor currentDestination;
    public ElevatorDirection direction;

    public StupidTuple(ElevatorStatus status, Floor currentDestination, ElevatorDirection direction) {
        this.status = status;
        this.currentDestination = currentDestination;
        this.direction = direction;
    }
}
