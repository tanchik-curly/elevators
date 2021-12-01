package com.gachiMadElevator.services;

import com.gachiMadElevator.console_colors.ConsoleColors;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeListener;
import java.util.Comparator;

@Slf4j
public class AccumulativeElevator extends Elevator {
    public AccumulativeElevator(Floor floor, PropertyChangeListener listener) {
        super(floor, listener);
    }

    @Override
    protected synchronized void findNextFloor() throws InterruptedException {
        if (activePassengers.isEmpty()) {
            if (waitingPassengers.isEmpty()) {
                this.status = ElevatorStatus.FREE;

                log.info(ConsoleColors.YELLOW + "No active or waiting passengers, elevator #" +
                        this.id + " is chilling..." + ConsoleColors.RESET);

                return;
            }

            this.currentDestination = waitingPassengers.poll().getInitialFloor();

            if (currentDestination.getCurrent() >= this.currentFloor.getCurrent()) {
                this.direction = ElevatorDirection.UP;
            } else {
                this.direction = ElevatorDirection.DOWN;
            }
        } else {
            int destinationFloorNumber = Integer.MAX_VALUE;
            int intermediateFloorNumber = Integer.MAX_VALUE;

            if (direction == ElevatorDirection.UP) {
                destinationFloorNumber = activePassengers.stream()
                        .map(Passenger::getFinalFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x > this.currentFloor.getCurrent())
                        .min(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);

                intermediateFloorNumber = waitingPassengers.stream()
                        .map(Passenger::getInitialFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x > this.currentFloor.getCurrent())
                        .min(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);

                if (destinationFloorNumber != Integer.MAX_VALUE && intermediateFloorNumber != Integer.MAX_VALUE) {
                    destinationFloorNumber = Math.min(destinationFloorNumber, intermediateFloorNumber);
                } else if (destinationFloorNumber == Integer.MAX_VALUE && intermediateFloorNumber != Integer.MAX_VALUE) {
                    destinationFloorNumber = intermediateFloorNumber;
                }
            } else {
                destinationFloorNumber = activePassengers.stream()
                        .map(Passenger::getFinalFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x < this.currentFloor.getCurrent())
                        .max(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);

                intermediateFloorNumber = waitingPassengers.stream()
                        .map(Passenger::getInitialFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x < this.currentFloor.getCurrent())
                        .max(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);

                destinationFloorNumber = Math.max(destinationFloorNumber, intermediateFloorNumber);
            }

            if (destinationFloorNumber == Integer.MAX_VALUE) {
                destinationFloorNumber = activePassengers.stream()
                        .map(Passenger::getFinalFloor)
                        .map(Floor::getCurrent)
                        .min(Comparator.comparingInt(x -> Math.abs(x - this.currentFloor.getCurrent())))
                        .get();

                if (destinationFloorNumber >= this.currentFloor.getCurrent()) {
                    direction = ElevatorDirection.UP;
                } else {
                    direction = ElevatorDirection.DOWN;
                }
            }

            int finalDestinationFloor = destinationFloorNumber;
            Passenger currentPassenger = activePassengers.stream()
                    .filter(x -> x.getFinalFloor().getCurrent() == finalDestinationFloor)
                    .findFirst().orElse(null);
            if (currentPassenger == null) {
                currentPassenger = waitingPassengers.stream()
                        .filter(x -> x.getInitialFloor().getCurrent() == finalDestinationFloor)
                        .findFirst().get();
                this.currentDestination = currentPassenger.getInitialFloor();
            } else {
                this.currentDestination = currentPassenger.getFinalFloor();
            }
        }

        log.info(ConsoleColors.YELLOW + "Elevator #" + this.id + " goes to floor " +
                this.currentDestination.getCurrent() + ", direction: " + direction + ConsoleColors.RESET);

        moveToFloor(this.currentDestination);
    }
}

