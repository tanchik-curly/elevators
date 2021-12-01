package com.gachiMadElevator.helpers;

import com.gachiMadElevator.helpers.Emoji;
import com.gachiMadElevator.services.Main;
import com.gachiMadElevator.view.*;
import com.gachiMadElevator.view.viewModels.ElevatorViewModel;
import com.gachiMadElevator.view.viewModels.PassengerQueueViewModel;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Observer implements PropertyChangeListener {
    public JTable table;

    public Observer(JTable table) {
        this.table = table;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals(ObservableProperties.FLOOR_CHANGED.toString())) {
            updateElevator((ElevatorViewModel) event.getNewValue());
        }
        else if(event.getPropertyName().equals(ObservableProperties.QUEUE_CHANGED.toString())) {
            updateQueue((PassengerQueueViewModel) event.getNewValue());
        }
        table.repaint();
    }
    private void updateElevator(ElevatorViewModel elevator) {
        ElevatorStatus status = ElevatorStatus.FREE;
        if(elevator.getCurrentActiveUserAmount() == elevator.getMaxActiveUserAmount() ||
                elevator.getMaxCapacity() - elevator.getCurrentCapacity() <= 10) {
            status = ElevatorStatus.FULL;
        }
        else if(elevator.getCurrentActiveUserAmount() > 0) {
            status = ElevatorStatus.WORK;
        }
        ChangeCellColor(table.getColumnModel(), elevator.getNumber(), elevator.getCurrentFloor(), status);

        for(int i = 0; i < Main.getFloorCount(); i++) {
            table.setValueAt("", i,elevator.getNumber() * 2);

            if(i == Main.getFloorCount() - elevator.getCurrentFloor()) {
                table.setValueAt(elevator.getCurrentActiveUserAmount(), i - 1, elevator.getNumber() * 2);
            }
        }
    }

    private void updateQueue(PassengerQueueViewModel newQueue) {
        String waitingPeople = "\ud83d\udc64".repeat(Math.max(0, newQueue.getUsersInQueue())) +
                " " + newQueue.getUsersInQueue();
        var queueRenderer = new QueueCellRenderer();
        queueRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(newQueue.getElevatorNumber() * 2 - 1).setCellRenderer(queueRenderer);

        table.setValueAt(waitingPeople,
                Main.getFloorCount() - newQueue.getCurrentFloor() - 1,
                newQueue.getElevatorNumber() * 2 - 1);
    }

    public static void ChangeCellColor(TableColumnModel model, int elevatorIndex, int floorNum, ElevatorStatus status)
    {
        var elevatorRenderer = new ElevatorRenderer(Main.getFloorCount() - floorNum - 1, status);
        elevatorRenderer.setHorizontalAlignment(JLabel.CENTER);
        model.getColumn(elevatorIndex * 2).setCellRenderer(elevatorRenderer);
    }
}
