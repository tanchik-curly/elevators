package com.gachiMadElevator.model2;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Passenger {
    // passenger info
    private int passengerId;
    private int passengerWeight;
    private String passengerName;

    // floor info
    private Floor finalFloor;
    private Floor initialFloor;

    // utils
    private Elevator executiveElevator;
    private static int numberOfPassengers = 0;

    public Passenger(String passengerName, int passengerWeight, Floor initialFloor, Floor finalFloor) {
        this.passengerId = numberOfPassengers++;
        this.passengerName = passengerName;
        this.passengerWeight = passengerWeight;
        this.initialFloor = initialFloor;
        this.finalFloor = finalFloor;
    }


    public synchronized boolean grantPassengerAccess(Elevator currElevator) {
        return currElevator.getActiveUsersCount() + 1 <= Elevator.MAX_USERS_COUNT && currElevator.getCurrentWeight() + getPassengerWeight() <= Elevator.MAX_ACCEPTABLE_WEIGHT;
    }

    public synchronized void invokeElevator() {
        executiveElevator.invokeElevator(this);
    }

}
