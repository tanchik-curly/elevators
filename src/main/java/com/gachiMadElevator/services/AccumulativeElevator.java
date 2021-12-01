package com.gachiMadElevator.services;

import com.gachiMadElevator.console_colors.ConsoleColors;
import com.gachiMadElevator.helpers.StupidTuple;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Queue;

@Slf4j
public class AccumulativeElevator implements FloorFindStrategy {

    public synchronized StupidTuple findNextFloor(List<Passenger> activePassengers, Queue<Passenger> waitingPassengers,
                                                  ElevatorStatus status, int id, Floor currentDestination,
                                                  Floor currentFloor, ElevatorDirection direction) {
        if (activePassengers.isEmpty()) {
            if (waitingPassengers.isEmpty()) {
                status = ElevatorStatus.FREE;

                log.info(ConsoleColors.YELLOW + "No active or waiting passengers, elevator #" +
                        id + " is chilling..." + ConsoleColors.RESET);

                return new StupidTuple(status, currentDestination, direction);
            }

            currentDestination = waitingPassengers.poll().getInitialFloor();

            if (currentDestination.getCurrent() >= currentFloor.getCurrent()) {
                direction = ElevatorDirection.UP;
            } else {
                direction = ElevatorDirection.DOWN;
            }
        } else {
            int destinationFloorNumber = Integer.MAX_VALUE;
            int intermediateFloorNumber = Integer.MAX_VALUE;

            if (direction == ElevatorDirection.UP) {
                destinationFloorNumber = activePassengers.stream()
                        .map(Passenger::getFinalFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x > currentFloor.getCurrent())
                        .min(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);

                intermediateFloorNumber = waitingPassengers.stream()
                        .map(Passenger::getInitialFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x > currentFloor.getCurrent())
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
                        .filter(x -> x < currentFloor.getCurrent())
                        .max(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);

                intermediateFloorNumber = waitingPassengers.stream()
                        .map(Passenger::getInitialFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x < currentFloor.getCurrent())
                        .max(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);

                destinationFloorNumber = Math.max(destinationFloorNumber, intermediateFloorNumber);
            }

            if (destinationFloorNumber == Integer.MAX_VALUE) {
                destinationFloorNumber = activePassengers.stream()
                        .map(Passenger::getFinalFloor)
                        .map(Floor::getCurrent)
                        .min(Comparator.comparingInt(x -> Math.abs(x - currentFloor.getCurrent())))
                        .get();

                if (destinationFloorNumber >= currentFloor.getCurrent()) {
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
                currentDestination = currentPassenger.getInitialFloor();
            } else {
                currentDestination = currentPassenger.getFinalFloor();
            }
        }

        log.info(ConsoleColors.YELLOW + "Elevator #" + id + " goes to floor " +
                currentDestination.getCurrent() + ", direction: " + direction + ConsoleColors.RESET);

        return new StupidTuple(status, currentDestination, direction);
    }
}

