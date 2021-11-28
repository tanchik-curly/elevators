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
    protected synchronized void moveToTheNextFloor() throws InterruptedException {
        if (activeUsers.isEmpty()) {
            if (waitingUsers.isEmpty()) {
                status = ElevatorStatus.FREE;
                log.info(ConsoleColors.YELLOW+"There are no waiting users, elevator is free!"+ ConsoleColors.RESET);
                return;
            } else {
                this.currentDestination = waitingUsers.poll().getStartFloor(); //get first passenger in queue
                if (currentDestination.getNumber() >= this.currentFloor.getNumber()) {
                    direction = ElevatorDirection.UP;
                } else {
                    direction = ElevatorDirection.DOWN;
                }
                log.info(ConsoleColors.YELLOW+"Direct elevator #" + this.id + " goes to floor "
                        + currentDestination.getNumber() + ", direction: " + direction+ConsoleColors.RESET);
            }
        } else {
            int newDestination;
            if (direction == ElevatorDirection.UP) {
                newDestination = activeUsers.stream()
                        .map(User::getDestinationFloor)
                        .map(Floor::getNumber)
                        .filter(x -> x >= this.currentFloor.getNumber())
                        .min(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);
            } else {
                newDestination = activeUsers.stream()
                        .map(User::getDestinationFloor)
                        .map(Floor::getNumber)
                        .filter(x -> x < this.currentFloor.getNumber())
                        .max(Integer::compareTo)
                        .orElse(Integer.MAX_VALUE);
            }
            if (newDestination == Integer.MAX_VALUE) {
                newDestination = activeUsers.stream()
                        .map(User::getDestinationFloor)
                        .map(Floor::getNumber)
                        .min(Comparator.comparingInt(x -> Math.abs(x - this.currentFloor.getNumber())))
                        .get();
                if (newDestination < this.currentFloor.getNumber()) {
                    direction = ElevatorDirection.DOWN;
                } else {
                    direction = ElevatorDirection.UP;
                }
            }
            User currentUser = findFirstUser(newDestination);
            this.currentDestination = currentUser.getDestinationFloor();
            log.info(ConsoleColors.YELLOW+"Direct elevator #" + this.id + " goes to floor "
                    + currentDestination.getNumber() + ", direction: " + direction+ConsoleColors.RESET);
        }
        moveToFloor(this.currentDestination);
    }

    private User findFirstUser(int resultFloor) {
        return activeUsers.stream()
                .filter(x -> x.getDestinationFloor().getNumber() == resultFloor)
                .findFirst().get();
    }
}

