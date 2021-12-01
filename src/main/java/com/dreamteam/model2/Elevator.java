package com.dreamteam.model2;

import com.dreamteam.console_colors.ConsoleColors;
import com.dreamteam.utils.SpeedControl;
import com.dreamteam.view.viewModels.ElevatorViewModel;
import com.dreamteam.view.ObservableProperties;
import com.dreamteam.view.viewModels.PassengerQueueViewModel;
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
    protected List<Passenger> activePassengers;
    protected Queue<Passenger> waitingPassengers;
    protected Floor currentFloor;
    protected Floor currentDestination;

    private PropertyChangeSupport support;

    public Elevator(Floor floor, PropertyChangeListener listener) {
        this.currentFloor = floor;
        id = counter++;
        status = ElevatorStatus.FREE;
        activePassengers = new ArrayList<>();
        waitingPassengers = new LinkedList<>();

        support = new PropertyChangeSupport(this);
        support.addPropertyChangeListener(listener);
    }

    public synchronized void invokeElevator(Passenger passenger) {
        if (status == ElevatorStatus.BUSY) {
            waitingPassengers.add(passenger);

            log.info(ConsoleColors.BLUE + "User #" + passenger.getPassengerId() + " " + passenger.getPassengerName() + " added to elevator #"
                    + this.getId() + " waiting list, size: " + waitingPassengers.size()+ConsoleColors.RESET);
        } else if (status == ElevatorStatus.FREE) {
            status = ElevatorStatus.BUSY;

            if (passenger.getInitialFloor() != currentFloor) {
                waitingPassengers.add(passenger);

                log.info(ConsoleColors.YELLOW + "Elevator #" + this.getId() + " goes to " + "user #"
                        + passenger.getPassengerId() + " " + passenger.getPassengerName() + ConsoleColors.RESET);
            }
        }
    }

    public void processElevator() throws InterruptedException {
        while (true) {
            sleep(SpeedControl.getQueueSpeed().get());
            removeLeavingUsers();
            takeUsers();
            moveToNextFloor();
        }
    }

    protected synchronized void removeLeavingUsers() {
        if (activePassengers.removeIf(x -> x.getFinalFloor() == currentFloor)) {
            log.info(ConsoleColors.BLUE + "User(s) left elevator #" + this.getId() + ConsoleColors.RESET);
        } else {
            log.info(ConsoleColors.BLUE + "No users left elevator #" + this.getId() + ConsoleColors.RESET);
        }
    }

    protected synchronized void takeUsers() {
        if (currentFloor == null)
            return;

        while (true) {
            if (!currentFloor.getPassengerElevatorQueue().get(this).isEmpty()) {
                Passenger passenger = currentFloor.getPassengerElevatorQueue().get(this).element();
                if (passenger.grantPassengerAccess(this)) {
                    waitingPassengers.remove(passenger);
                    currentFloor.getPassengerElevatorQueue().get(this).poll();
                    activePassengers.add(passenger);

                    log.info(ConsoleColors.BLUE + "User #" + passenger.getPassengerId() + " " + passenger.getPassengerName() + " entered elevator #"
                            + this.getId() + ", active users: " + activePassengers.size() +ConsoleColors.RESET);

                    var userQueueViewModel = new PassengerQueueViewModel(currentFloor.getCurrent(),
                            id + 1,
                            currentFloor.getPassengerElevatorQueue().get(this).size());
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
        return activePassengers.size();
    }

    public synchronized int getCurrentWeight() {
        return activePassengers.stream().map(Passenger::getPassengerWeight).reduce(0, Integer::sum);
    }

    public synchronized void moveToFloor(Floor floor) throws InterruptedException {
        int tempFloorNumber = currentFloor.getCurrent();

        while (tempFloorNumber != currentDestination.getCurrent()) {
            var elevatorViewModel = new ElevatorViewModel(
                    id + 1,
                    activePassengers.size(),
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

             sleep(SpeedControl.getElevatorSpeed().get());
        }

        this.currentFloor = floor;
        log.info(ConsoleColors.YELLOW + "Elevator #" + this.getId() + ", current floor: "
                + (this.currentFloor == null ? "NULL" : this.currentFloor.getCurrent()) + ConsoleColors.RESET);
    }
}

