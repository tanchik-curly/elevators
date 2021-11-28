package com.dreamteam.model2;

import com.dreamteam.console_colors.ConsoleColors;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeListener;
import java.util.Comparator;

@Slf4j
public class DirectElevator extends Elevator {
    public DirectElevator(Floor currentFloor, PropertyChangeListener listener) {
        super(currentFloor, listener);
    }

    @Override
    protected synchronized void moveToNextFloor() throws InterruptedException {
        if (activePassengers.isEmpty()) {
            if (waitingPassengers.isEmpty()) {
                status = ElevatorStatus.FREE;
                log.info(ConsoleColors.YELLOW+"There are no waiting users, elevator is free!"+ ConsoleColors.RESET);
                return;
            } else {
                this.currentDestination = waitingPassengers.poll().get_initialFloor(); //get first passenger in queue
                if (currentDestination.getCurrent() >= this.currentFloor.getCurrent()) {
                    direction = ElevatorDirection.UP;
                } else {
                    direction = ElevatorDirection.DOWN;
                }
                log.info(ConsoleColors.YELLOW+"Direct elevator #" + this.id + " goes to floor "
                        + currentDestination.getCurrent() + ", direction: " + direction+ConsoleColors.RESET);
            }
        } else {
            int newDestination;
            if (direction == ElevatorDirection.UP) {
                newDestination = activePassengers.stream()
                        .map(Passenger::get_finalFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x >= this.currentFloor.getCurrent())
                        .min(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);
            } else {
                newDestination = activePassengers.stream()
                        .map(Passenger::get_finalFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x < this.currentFloor.getCurrent())
                        .max(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);
            }
            if (newDestination == Integer.MAX_VALUE) {
                newDestination = activePassengers.stream()
                        .map(Passenger::get_finalFloor)
                        .map(Floor::getCurrent)
                        .min(Comparator.comparingInt(x -> Math.abs(x - this.currentFloor.getCurrent())))
                        .get();
                if (newDestination < this.currentFloor.getCurrent()) {
                    direction = ElevatorDirection.DOWN;
                } else {
                    direction = ElevatorDirection.UP;
                }
            }
            Passenger currentPassenger = findFirstUser(newDestination);
            this.currentDestination = currentPassenger.get_finalFloor();
            log.info(ConsoleColors.YELLOW+"Direct elevator #" + this.id + " goes to floor "
                    + currentDestination.getCurrent() + ", direction: " + direction+ConsoleColors.RESET);
        }
        moveToFloor(this.currentDestination);
    }

    private Passenger findFirstUser(int resultFloor) {
        return activePassengers.stream()
                .filter(x -> x.get_finalFloor().getCurrent() == resultFloor)
                .findFirst().get();
    }
}

