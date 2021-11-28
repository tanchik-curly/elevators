package com.dreamteam.model2;

import com.dreamteam.console_colors.ConsoleColors;
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
    public static final int MAX_USER_COUNT = 30;
    public static final int CAPACITY = 1000;
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

    public Elevator(Floor currentFloor, PropertyChangeListener listener) {
        this.currentFloor = currentFloor;
        id = counter++;
        status = ElevatorStatus.FREE;
        activePassengers = new ArrayList<>();
        waitingPassengers = new LinkedList<>();

        support = new PropertyChangeSupport(this);
        support.addPropertyChangeListener(listener);
    }

    public synchronized void invoke(Passenger passenger) {
        if (status == ElevatorStatus.BUSY) {
            waitingPassengers.add(passenger);
            log.info(ConsoleColors.BLUE+"User " + passenger.get_passengerName() + passenger.get_passengerId() + " added to " + "Elevator" + this.getId() + " waiting list, size: " + waitingPassengers.size()+ConsoleColors.RESET);
        } else if (status == ElevatorStatus.FREE){
            status = ElevatorStatus.BUSY;
            if (passenger.get_initialFloor() != currentFloor) {
                waitingPassengers.add(passenger);
                log.info(ConsoleColors.YELLOW+"Elevator" + this.getId() + " goes to " + "user " + passenger.get_passengerName() + passenger.get_passengerId()+ConsoleColors.RESET);
            }
        }
    }

    public void process() throws InterruptedException {
        while (true) {
            sleep(80);
            deleteUsersWhoExitOnCurrentFloor();
            pickupUsers();
            moveToTheNextFloor();
        }
    }

    protected synchronized void deleteUsersWhoExitOnCurrentFloor() {
        if (activePassengers.removeIf(x -> x.get_finalFloor() == currentFloor)) {
            log.info(ConsoleColors.BLUE+"User(s) left Elevator" + this.getId()+ConsoleColors.RESET);
        } else {
            log.info(ConsoleColors.BLUE+"No users left Elevator" + this.getId()+ConsoleColors.RESET);
        }
    }

    protected synchronized void pickupUsers() {
        if (currentFloor == null) return;
        while (true) {
            if (!currentFloor.getPassengerElevatorQueue().get(this).isEmpty()) {
                Passenger passenger = currentFloor.getPassengerElevatorQueue().get(this).element();
                if (passenger.grantPassengerAccess(this)) {
                    waitingPassengers.remove(passenger);
                    currentFloor.getPassengerElevatorQueue().get(this).poll();
                    activePassengers.add(passenger);
                    log.info(ConsoleColors.BLUE+"User " + passenger.get_passengerName() + "" + passenger.get_passengerId() +
                            " entered Elevator" + this.getId() +  ", active users: " + activePassengers.size()+ConsoleColors.RESET);

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

    protected abstract void moveToTheNextFloor() throws InterruptedException;

    public synchronized int getActiveUsersCount() {
        return activePassengers.size();
    }

    public synchronized int getCurrentCapacity() {
        return activePassengers.stream().map(Passenger::get_passengerWeight).reduce(0, Integer::sum);
    }

    public synchronized void moveToFloor(Floor floor) throws InterruptedException {

        var tempFloorNumber = currentFloor.getCurrent();

        while(tempFloorNumber != currentDestination.getCurrent()) {
            var elevatorViewModel = new ElevatorViewModel(id + 1,
                    activePassengers.size(),
                    getCurrentCapacity(),
                    Elevator.MAX_USER_COUNT,
                    Elevator.CAPACITY,
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

        log.info(ConsoleColors.YELLOW+"Elevator" + this.getId() + ", current floor: " +
                (this.currentFloor == null ? "NULL" : this.currentFloor.getCurrent())+ConsoleColors.RESET);
    }
}

