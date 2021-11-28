package com.dreamteam.model2;

import com.dreamteam.console_colors.ConsoleColors;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeListener;
import java.util.Comparator;

@Slf4j
public class ElevatorB extends Elevator {
    public ElevatorB(Floor currentFloor, PropertyChangeListener listener, ElevatorDirection elevatorDirection) {
        super(currentFloor, elevatorDirection, listener);
    }

    @Override
    protected synchronized void moveToTheNextFloor() throws InterruptedException {
        if (activeUsers.isEmpty()) {
            if (waitingUsers.isEmpty()) {
                status = ElevatorStatus.FREE;
                log.info(ConsoleColors.YELLOW+"No active and waiting users, ElevatorB" + this.id + " is free now"+ConsoleColors.RESET);
                return;
            } else {
                // Elevator goes to start floor of the first user in waiting users list
                this.currentDestination = waitingUsers.poll().getStartFloor();
                if (currentDestination.getCurrent() >= this.currentFloor.getCurrent()) {
                    direction = ElevatorDirection.UP;
                } else {
                    direction = ElevatorDirection.DOWN;
                }
                log.info(ConsoleColors.YELLOW+"ElevatorB" + this.id + " goes to floor " + currentDestination.getCurrent()
                        + ", direction: " + direction+ConsoleColors.RESET);
            }
        }
        else {
            int destFloor;
            int tempFloor;
            if (direction == ElevatorDirection.UP) {
                destFloor = activeUsers.stream()
                        .map(User::getDestinationFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x > this.currentFloor.getCurrent())
                        .min(Integer::compareTo)
                        .orElse(-1);
                tempFloor = waitingUsers.stream()
                        .map(User::getStartFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x > this.currentFloor.getCurrent())
                        .min(Integer::compareTo)
                        .orElse(-1);
                if (destFloor != -1 && tempFloor != -1) {
                    destFloor = Math.min(destFloor, tempFloor);
                } else if (destFloor == -1 && tempFloor != -1){
                    destFloor = tempFloor;
                }
            } else {
                destFloor = activeUsers.stream()
                        .map(User::getDestinationFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x < this.currentFloor.getCurrent())
                        .max(Integer::compareTo)
                        .orElse(-1);
                tempFloor = waitingUsers.stream()
                        .map(User::getStartFloor)
                        .map(Floor::getCurrent)
                        .filter(x -> x < this.currentFloor.getCurrent())
                        .max(Integer::compareTo)
                        .orElse(-1);
                destFloor = Math.max(destFloor, tempFloor);
            }
            if (destFloor == -1) {
                destFloor = activeUsers.stream()
                        .map(User::getDestinationFloor)
                        .map(Floor::getCurrent)
                        .min(Comparator.comparingInt(x -> Math.abs(x - this.currentFloor.getCurrent())))
                        .get();
                if (destFloor >= this.currentFloor.getCurrent()) {
                    direction = ElevatorDirection.UP;
                } else {
                    direction = ElevatorDirection.DOWN;
                }
            }
            int finalDestFloor = destFloor;
            User currentUser = activeUsers.stream()
                    .filter(x -> x.getDestinationFloor().getCurrent() == finalDestFloor)
                    .findFirst().orElse(null);
            if (currentUser == null) {
                currentUser = waitingUsers.stream()
                        .filter(x -> x.getStartFloor().getCurrent() == finalDestFloor)
                        .findFirst().get();
                this.currentDestination = currentUser.getStartFloor();
            } else {
                this.currentDestination = currentUser.getDestinationFloor();
            }
            log.info(ConsoleColors.YELLOW+"ElevatorB" + this.id + " goes to floor " + currentDestination.getCurrent()
                    + ", direction: " + direction + ConsoleColors.RESET);
        }
        moveToFloor(this.currentDestination);
    }
}

