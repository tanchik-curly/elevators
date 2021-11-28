package com.dreamteam.model2;

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
    private int _passengerId;
    private int _passengerWeight;
    private String _passengerName;

    // floor info
    private Floor _finalFloor;
    private Floor _initialFloor;

    // utils
    private Elevator _executiveElevator;
    private static int _numberOfPassengers = 0;

    public Passenger(String _passengerName, int _passengerWeight, Floor _initialFloor, Floor _finalFloor) {
        this._passengerId = _numberOfPassengers++;
        this._passengerName = _passengerName;
        this._passengerWeight = _passengerWeight;
        this._initialFloor = _initialFloor;
        this._finalFloor = _finalFloor;
    }

    public synchronized boolean grantPassengerAccess(Elevator currElevator) {
        return currElevator.getActiveUsersCount() + 1 <= Elevator.MAX_USER_COUNT && currElevator.getCurrentCapacity() + get_passengerWeight() <= Elevator.CAPACITY;
    }

    public synchronized void invokeElevator() {
        _executiveElevator.invoke(this);
    }

}
