package com.dreamteam.model2;

import com.dreamteam.console_colors.ConsoleColors;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeListener;
import java.util.Comparator;

@Slf4j
public class ElevatorA extends Elevator {
    public ElevatorA(Floor currentFloor, PropertyChangeListener listener, ElevatorDirection elevatorDirection) {
        super(currentFloor, listener);
    }

    @Override
    protected synchronized void moveToNextFloor() throws InterruptedException {
        if (activePassengers.isEmpty()) {
            if (waitingPassengers.isEmpty()) {
                status = ElevatorStatus.FREE;
                log.info(ConsoleColors.YELLOW+"No active and waiting users, elevator is free now"+ConsoleColors.RESET);
                return;
            } else {
                // Elevator goes to start floor of the first user in waiting users list
                this.currentDestination = waitingPassengers.poll().get_initialFloor();
                if (currentDestination.getNumber() >= this.currentFloor.getNumber()) {
                    direction = ElevatorDirection.UP;
                } else {
                    direction = ElevatorDirection.DOWN;
                }
                log.info(ConsoleColors.YELLOW+"ElevatorA" + this.id + " goes to floor " + currentDestination.getNumber() + ", direction: " + direction+ConsoleColors.RESET);
            }
        } else {
            int destFloor;
            if (direction == ElevatorDirection.UP) {
                destFloor = activePassengers.stream()
                        .map(Passenger::get_finalFloor)
                        .map(Floor::getNumber)
                        .filter(x -> x >= this.currentFloor.getNumber())
                        .min(Integer::compareTo)
                        .orElse(-1);
            } else {
                destFloor = activePassengers.stream()
                        .map(Passenger::get_finalFloor)
                        .map(Floor::getNumber)
                        .filter(x -> x < this.currentFloor.getNumber())
                        .max(Integer::compareTo)
                        .orElse(-1);
            }
            if (destFloor == -1) {
                destFloor = activePassengers.stream()
                        .map(Passenger::get_finalFloor)
                        .map(Floor::getNumber)
                        .min(Comparator.comparingInt(x -> Math.abs(x - this.currentFloor.getNumber())))
                        .get();
                if (destFloor >= this.currentFloor.getNumber()) {
                    direction = ElevatorDirection.UP;
                } else {
                    direction = ElevatorDirection.DOWN;
                }
            }
            int finalDestFloor = destFloor;
            Passenger currentPassenger = activePassengers.stream()
                    .filter(x -> x.get_finalFloor().getNumber() == finalDestFloor)
                    .findFirst().get();
            this.currentDestination = currentPassenger.get_finalFloor();
            log.info(ConsoleColors.YELLOW+"ElevatorA" + this.id + " goes to floor " + currentDestination.getNumber() + ", direction: " + direction+ConsoleColors.RESET);
        }
        moveToFloor(this.currentDestination);
    }
}

