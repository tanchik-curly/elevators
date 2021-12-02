package com.gachiMadElevator.services;

import com.gachiMadElevator.console_colors.ConsoleColors;
import com.gachiMadElevator.helpers.StupidTuple;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Queue;

@Slf4j
public class DirectElevator implements FloorFindStrategy {

    public synchronized StupidTuple findNextFloor(List<Passenger> activePassengers, Queue<Passenger> waitingPassengers,
                                                  ElevatorStatus status, int id, Floor currentDestination,
                                                  Floor currentFloor, ElevatorDirection direction) {
        if (activePassengers.isEmpty()) {
            if (waitingPassengers.isEmpty()) {
                status = ElevatorStatus.FREE;
                log.info(ConsoleColors.PURPLE +"There are no waiting passengers, elevator is free!"+ ConsoleColors.RESET);
                return new StupidTuple(status, currentDestination, direction);
            } else {
                currentDestination = waitingPassengers.poll().getInitialFloor(); //get first passenger in queue
                if (currentDestination.getCurrent() >= currentFloor.getCurrent()) {
                    direction = ElevatorDirection.UP;
                } else {
                    direction = ElevatorDirection.DOWN;
                }
                log.info(ConsoleColors.PURPLE +"Direct elevator #" + id + " goes to floor "
                        + currentDestination.getCurrent() + ", direction: " + direction+ConsoleColors.RESET);
            }
        } else {
            int newDestination;
            if (direction == ElevatorDirection.UP) {
                newDestination = activePassengers.stream()
                        .map(Passenger::getFinalFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x >= currentFloor.getCurrent())
                        .min(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);
            } else {
                newDestination = activePassengers.stream()
                        .map(Passenger::getFinalFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x < currentFloor.getCurrent())
                        .max(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);
            }
            if (newDestination == Integer.MAX_VALUE) {
                newDestination = activePassengers.stream()
                        .map(Passenger::getFinalFloor)
                        .map(Floor::getCurrent)
                        .min(Comparator.comparingInt(x -> Math.abs(x - currentFloor.getCurrent())))
                        .get();
                if (newDestination < currentFloor.getCurrent()) {
                    direction = ElevatorDirection.DOWN;
                } else {
                    direction = ElevatorDirection.UP;
                }
            }
            Passenger currentPassenger = findFirstPassenger(newDestination, activePassengers);
            currentDestination = currentPassenger.getFinalFloor();
            log.info(ConsoleColors.YELLOW+"Direct elevator #" + id + " goes to floor "
                    + currentDestination.getCurrent() + " with direction: " + direction+ConsoleColors.RESET);
        }

        return new StupidTuple(status, currentDestination, direction);
    }

    private Passenger findFirstPassenger(int resultFloor, List<Passenger> activePassengers) {
        return activePassengers.stream()
                .filter(x -> x.getFinalFloor().getCurrent() == resultFloor)
                .findFirst().get();
    }
}

