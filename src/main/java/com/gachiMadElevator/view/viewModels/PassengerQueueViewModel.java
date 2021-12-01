package com.dreamteam.view.viewModels;
import lombok.*;

@AllArgsConstructor
@Getter
public class PassengerQueueViewModel {
    private int currentFloor;
    private int elevatorNumber;
    private int usersInQueue;
}
