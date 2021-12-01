package com.gachiMadElevator.services;

import com.gachiMadElevator.console_colors.ConsoleColors;
import com.gachiMadElevator.view.ObservableProperties;
import com.gachiMadElevator.view.viewModels.PassengerQueueViewModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

@Getter
@Setter
@Slf4j
public class Floor {

    private int current;

    private Floor next;

    private Floor previous;

    private Map<Elevator, Queue<Passenger>> passengerElevatorQueue;

    private PropertyChangeSupport propertyChangeSupport;

    public static int MAX_FLOOR_AMOUNT = 30;

    public Floor(int currentFloorNumber, PropertyChangeListener listener) {
        this.current = currentFloorNumber;
        propertyChangeSupport = new PropertyChangeSupport(this);
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void initQueues(List<Elevator> elevators) {
        passengerElevatorQueue = new HashMap<>();
        for (var elevator: elevators) {
            if (!passengerElevatorQueue.containsKey(elevator)) {
                passengerElevatorQueue.put(elevator, new ArrayDeque<>());
            }
        }
    }

    public synchronized void addUserToQueue(Passenger passenger) {
        var shortestPassengerQueue = passengerElevatorQueue
                .values()
                .stream()
                .min(Comparator.comparing(Queue::size))
                .orElseThrow(NoSuchElementException::new);

        var elevator = passengerElevatorQueue
                .entrySet()
                .stream()
                .filter(entry -> shortestPassengerQueue.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElseThrow(NoSuchElementException::new);

        shortestPassengerQueue.add(passenger);
        passenger.setExecutiveElevator(elevator);

        log.info(ConsoleColors.PURPLE + "Queue at floor №" + this.getCurrent() + " to elevator: " + elevator.getId() +
                ", number of passengers in queue: " + this.getPassengerElevatorQueue().get(elevator).size() +
                ConsoleColors.RESET);

        var passengerQueueViewModel = new PassengerQueueViewModel(this.current,
                elevator.id + 1, passengerElevatorQueue.get(elevator).size());

        propertyChangeSupport.firePropertyChange(ObservableProperties.QUEUE_CHANGED.toString(), null, passengerQueueViewModel);
    }
}
