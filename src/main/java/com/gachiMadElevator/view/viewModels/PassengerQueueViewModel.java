package com.gachiMadElevator.view.viewModels;
import lombok.*;

@AllArgsConstructor
@Getter
public class PassengerQueueViewModel {
    private int currentFloor;
    private int elevatorNumber;
    private int passengersInQueue;
}
