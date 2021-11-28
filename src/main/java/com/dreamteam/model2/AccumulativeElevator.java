package com.dreamteam.model2;

import com.dreamteam.console_colors.ConsoleColors;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeListener;
import java.util.Comparator;

@Slf4j
public class AccumulativeElevator extends Elevator {
    public AccumulativeElevator(Floor floor, PropertyChangeListener listener) {
        super(floor, listener);
    }

    @Override
    protected synchronized void moveToNextFloor() throws InterruptedException {
        if (activePassengers.isEmpty()) {
            if (waitingPassengers.isEmpty()) {
                this.status = ElevatorStatus.FREE;

                log.info(ConsoleColors.YELLOW + "No active or waiting passengers, elevator #" +
                        this.id + " is chilling..." + ConsoleColors.RESET);

                return;
            }

            this.currentDestination = waitingPassengers.poll().get_initialFloor();

            if (currentDestination.getNumber() >= this.currentFloor.getNumber()) {
                this.direction = ElevatorDirection.UP;
            } else {
                this.direction = ElevatorDirection.DOWN;
            }
        } else {
            int destinationFloorNumber = -1;
            int tempFloorNumber = -1;

            if (direction == ElevatorDirection.UP) {
                destinationFloorNumber = activePassengers.stream()
                        .map(Passenger::get_finalFloor)
                        .map(Floor::getNumber)
                        .filter(x -> x > this.currentFloor.getNumber())
                        .min(Integer::compareTo)
                        .orElse(-1);
                tempFloorNumber = waitingPassengers.stream()
                        .map(Passenger::get_initialFloor)
                        .map(Floor::getNumber)
                        .filter(x -> x > this.currentFloor.getNumber())
                        .min(Integer::compareTo)
                        .orElse(-1);

                if (destinationFloorNumber != -1 && tempFloorNumber != -1) {
                    destinationFloorNumber = Math.min(destinationFloorNumber, tempFloorNumber);
                } else if (destinationFloorNumber == -1 && tempFloorNumber != -1) {
                    destinationFloorNumber = tempFloorNumber;
                }
            } else {
                destinationFloorNumber = activePassengers.stream()
                        .map(Passenger::get_finalFloor)
                        .map(Floor::getNumber)
                        .filter(x -> x < this.currentFloor.getNumber())
                        .max(Integer::compareTo)
                        .orElse(-1);

                tempFloorNumber = waitingPassengers.stream()
                        .map(Passenger::get_initialFloor)
                        .map(Floor::getNumber)
                        .filter(x -> x < this.currentFloor.getNumber())
                        .max(Integer::compareTo)
                        .orElse(-1);

                destinationFloorNumber = Math.max(destinationFloorNumber, tempFloorNumber);
            }

            if (destinationFloorNumber == -1) {
                destinationFloorNumber = activePassengers.stream()
                        .map(Passenger::get_finalFloor)
                        .map(Floor::getNumber)
                        .min(Comparator.comparingInt(x -> Math.abs(x - this.currentFloor.getNumber())))
                        .get();

                if (destinationFloorNumber >= this.currentFloor.getNumber()) {
                    direction = ElevatorDirection.UP;
                } else {
                    direction = ElevatorDirection.DOWN;
                }
            }

            int finalDestinationFloor = destFloor;
            Passenger currentPassenger = activePassengers.stream()
                    .filter(x -> x.get_finalFloor().getNumber() == finalDestinationFloor)
                    .findFirst().orElse(null);
            if (currentPassenger == null) {
                currentPassenger = waitingPassengers.stream()
                        .filter(x -> x.get_initialFloor().getNumber() == finalDestinationFloor)
                        .findFirst().get();
                this.currentDestination = currentPassenger.get_initialFloor();
            } else {
                this.currentDestination = currentPassenger.get_finalFloor();
            }
        }

        log.info(ConsoleColors.YELLOW + "Elevator #" + this.id + " goes to floor " +
                this.currentDestination.getNumber() + ", direction: " + direction + ConsoleColors.RESET);

        moveToFloor(this.currentDestination);
    }
}

