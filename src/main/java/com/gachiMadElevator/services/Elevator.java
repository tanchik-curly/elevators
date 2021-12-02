package com.gachiMadElevator.services;

import com.gachiMadElevator.console_colors.ConsoleColors;
import com.gachiMadElevator.helpers.StupidTuple;
import com.gachiMadElevator.utils.LockObj;
import com.gachiMadElevator.utils.SpeedControl;
import com.gachiMadElevator.view.viewModels.ElevatorViewModel;
import com.gachiMadElevator.view.ObservableProperties;
import com.gachiMadElevator.view.viewModels.PassengerQueueViewModel;
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
public class Elevator {
    public static final int MAX_PASSENGERS_COUNT = 30;
    public static final int MAX_ACCEPTABLE_WEIGHT = 1000;
    @Setter
    private static int counter = 0;
    public int id;
    private ElevatorStatus status;
    private ElevatorDirection direction;
    private Floor currentFloor;
    private Floor currentDestination;
    private List<Passenger> activePassengers;
    private Queue<Passenger> waitingPassengers;

    private PropertyChangeSupport support;

    private FloorFindStrategy floorFindStrategy;

    public Elevator(Floor floor, PropertyChangeListener listener, FloorFindStrategy strategy) {
        this.currentFloor = floor;
        id = counter++;
        status = ElevatorStatus.FREE;
        activePassengers = new ArrayList<>();
        waitingPassengers = new LinkedList<>();

        support = new PropertyChangeSupport(this);
        support.addPropertyChangeListener(listener);

        this.floorFindStrategy = strategy;
    }

    public synchronized int getActivePassengersCount() {
        return activePassengers.size();
    }

    public synchronized int getCurrentWeight() {
        return activePassengers.stream().map(Passenger::getPassengerWeight).reduce(0, Integer::sum);
    }

    public void invokeElevator(Passenger passenger) {
        synchronized (LockObj.class) {
            if (status == ElevatorStatus.BUSY) {
                waitingPassengers.add(passenger);

                log.info(ConsoleColors.RED + "Passenger #" + passenger.getPassengerId() + " " + passenger.getPassengerName() + " wait on elevator #"
                        + this.getId() + ", number of waiting people: " + waitingPassengers.size() + ConsoleColors.RESET);
            } else if (status == ElevatorStatus.FREE) {
                if (passenger.getInitialFloor() != currentFloor) {
                    waitingPassengers.add(passenger);
                    log.info(ConsoleColors.YELLOW + "Elevator #" + this.getId() + " goes to " + "passenger "
                            + passenger.getPassengerName() + ConsoleColors.RESET);
                }
                status = ElevatorStatus.BUSY;
            }
        }
    }

    public void processElevator() throws InterruptedException {
        //elevators must make a decision about actions
        sleep(SpeedControl.getElevatorSpeed().get());
        removeLeavingPassengers();
        takePassengers();
        findAndMoveToNextFloor();
    }

    private synchronized void removeLeavingPassengers() {
        if (activePassengers.removeIf(x -> x.getFinalFloor() == currentFloor)) {
            log.info(ConsoleColors.GREEN + "Passengers left elevator #" + this.getId() + ConsoleColors.RESET);
        } else {
            log.info(ConsoleColors.GREEN + "Passengers didn't leave elevator #" + this.getId() + ConsoleColors.RESET);
        }
    }

    private synchronized void takePassengers() {
        if (currentFloor == null)
            return;

        while (true) {
            if (!currentFloor.getPassengerElevatorQueue().get(this).isEmpty()) {
                Passenger passenger = currentFloor.getPassengerElevatorQueue().get(this).element();
                if (passenger.grantPassengerAccess(this)) {
                    waitingPassengers.remove(passenger);
                    currentFloor.getPassengerElevatorQueue().get(this).poll();
                    activePassengers.add(passenger);

                    log.info(ConsoleColors.BLUE + "Passenger #" + passenger.getPassengerId() + " " + passenger.getPassengerName() + " entered elevator #"
                            + this.getId() + ConsoleColors.RESET);

                    var passengerQueueViewModel = new PassengerQueueViewModel(currentFloor.getCurrent(),
                            id + 1,
                            currentFloor.getPassengerElevatorQueue().get(this).size());
                    support.firePropertyChange(ObservableProperties.QUEUE_CHANGED.toString(), null, passengerQueueViewModel);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    public synchronized void moveToFloor(Floor floor) throws InterruptedException {
        if (currentDestination == null)
            return;

        int currentElevatorFloor = currentFloor.getCurrent();

        while (currentElevatorFloor != currentDestination.getCurrent()) {
            var elevatorViewModel = new ElevatorViewModel(
                    id + 1,
                    activePassengers.size(),
                    getCurrentWeight(),
                    Elevator.MAX_PASSENGERS_COUNT,
                    Elevator.MAX_ACCEPTABLE_WEIGHT,
                    currentElevatorFloor);

            support.firePropertyChange(ObservableProperties.FLOOR_CHANGED.toString(), null, elevatorViewModel);

            if (direction == ElevatorDirection.UP) {
                currentElevatorFloor++;
            } else {
                currentElevatorFloor--;
            }

             sleep(SpeedControl.getElevatorSpeed().get());
        }

        this.currentFloor = floor;
        if(this.currentFloor != null) {
            log.info(ConsoleColors.YELLOW + "Elevator #" + this.getId() + " arrived on "
                    + (this.currentFloor == null ? "NULL" : this.currentFloor.getCurrent()) + ConsoleColors.RESET);
        }
    }

    private void findAndMoveToNextFloor() throws InterruptedException {
        StupidTuple result;
        synchronized (LockObj.class) {
            result = floorFindStrategy.findNextFloor(
                    activePassengers, waitingPassengers, status, id, currentDestination, currentFloor, direction);
        }

        this.status = result.status;
        this.currentDestination = result.currentDestination;
        this.direction = result.direction;

        moveToFloor(currentDestination);
    }
}

