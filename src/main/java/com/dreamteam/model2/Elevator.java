package com.dreamteam.model2;

import com.dreamteam.console_colors.ConsoleColors;
import com.dreamteam.view.viewModels.ElevatorViewModel;
import com.dreamteam.view.ObservableProperties;
import com.dreamteam.view.viewModels.UserQueueViewModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static java.lang.Thread.sleep;

@Getter
@Setter
@Slf4j
public abstract class Elevator {
    public static final int MAX_USERS_COUNT = 30;
    public static final int MAX_ACCEPTABLE_WEIGHT = 1000;
    @Setter
    private static int counter = 0;
    protected int id;
    protected ElevatorStatus status;
    protected ElevatorDirection direction;
    protected List<User> activeUsers;
    protected Queue<User> waitingUsers;
    protected Floor currentFloor;
    protected Floor currentDestination;

    private PropertyChangeSupport support;

    public Elevator(Floor floor, PropertyChangeListener listener) {
        this.currentFloor = floor;
        id = counter++;
        status = ElevatorStatus.FREE;
        activeUsers = new ArrayList<>();
        waitingUsers = new LinkedList<>();

        support = new PropertyChangeSupport(this);
        support.addPropertyChangeListener(listener);
    }

    public synchronized void invokeElevator(User user) {
        if (status == ElevatorStatus.BUSY) {
            waitingUsers.add(user);

            log.info(ConsoleColors.BLUE + "User #" + user.getId() + " " + user.getName() + " added to elevator #"
                    + this.getId() + " waiting list, size: " + waitingUsers.size()+ConsoleColors.RESET);
        } else if (status == ElevatorStatus.FREE) {
            status = ElevatorStatus.BUSY;

            if (user.getStartFloor() != currentFloor) {
                waitingUsers.add(user);

                log.info(ConsoleColors.YELLOW + "Elevator #" + this.getId() + " goes to " + "user #"
                        + user.getId() + " " + user.getName() + ConsoleColors.RESET);
            }
        }
    }

    public void processElevator() throws InterruptedException {
        while (true) {
            sleep(80);
            removeLeavingUsers();
            takeUsers();
            moveToNextFloor();
        }
    }

    protected synchronized void removeLeavingUsers() {
        if (activeUsers.removeIf(x -> x.getDestinationFloor() == currentFloor)) {
            log.info(ConsoleColors.BLUE + "User(s) left elevator #" + this.getId() + ConsoleColors.RESET);
        } else {
            log.info(ConsoleColors.BLUE + "No users left elevator #" + this.getId() + ConsoleColors.RESET);
        }
    }

    protected synchronized void takeUsers() {
        if (currentFloor == null)
            return;

        while (true) {
            if (!currentFloor.getUsersQueueToElevator().get(this).isEmpty()) {
                var user = currentFloor.getUsersQueueToElevator().get(this).element();
                if (user.canUserEnter(this)) {
                    waitingUsers.remove(user);
                    currentFloor.getUsersQueueToElevator().get(this).poll();
                    activeUsers.add(user);

                    log.info(ConsoleColors.BLUE + "User #" + user.getId() + " " + user.getName() + " entered elevator #"
                            + this.getId() + ", active users: " + activeUsers.size() +ConsoleColors.RESET);

                    var userQueueViewModel = new UserQueueViewModel(currentFloor.getNumber(),
                            id + 1,
                            currentFloor.getUsersQueueToElevator().get(this).size());

                    support.firePropertyChange(ObservableProperties.QUEUE_CHANGED.toString(), null, userQueueViewModel);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    protected abstract void moveToNextFloor() throws InterruptedException;

    public synchronized int getActiveUsersCount() {
        return activeUsers.size();
    }

    public synchronized int getCurrentWeight() {
        return activeUsers.stream().map(User::getWeight).reduce(0, Integer::sum);
    }

    public synchronized void moveToFloor(Floor floor) throws InterruptedException {
        int tempFloorNumber = currentFloor.getNumber();

        while (tempFloorNumber != currentDestination.getNumber()) {
            var elevatorViewModel = new ElevatorViewModel(
                    id + 1,
                    activeUsers.size(),
                    getCurrentWeight(),
                    Elevator.MAX_USERS_COUNT,
                    Elevator.MAX_ACCEPTABLE_WEIGHT,
                    tempFloorNumber);

            support.firePropertyChange(ObservableProperties.FLOOR_CHANGED.toString(), null, elevatorViewModel);

            if (direction == ElevatorDirection.UP) {
                tempFloorNumber++;
            } else {
                tempFloorNumber--;
            }

             sleep(50);
        }

        this.currentFloor = floor;

        log.info(ConsoleColors.YELLOW + "Elevator #" + this.getId() + ", current floor: "
                + (this.currentFloor == null ? "NULL" : this.currentFloor.getNumber()) + ConsoleColors.RESET);
    }
}
