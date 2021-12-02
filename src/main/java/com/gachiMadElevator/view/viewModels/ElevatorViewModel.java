package com.gachiMadElevator.view.viewModels;
import lombok.*;

@AllArgsConstructor
@Getter
public class ElevatorViewModel {
    private int number;
    private int currentActivePassengerAmount;
    private int currentCapacity;
    private int maxActivePassengerAmount;
    private int maxCapacity;
    private int currentFloor;
}
