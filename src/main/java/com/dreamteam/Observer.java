package com.dreamteam;

import com.dreamteam.model2.Main;
import com.dreamteam.view.*;
import com.dreamteam.view.viewModels.ElevatorViewModel;
import com.dreamteam.view.viewModels.UserQueueViewModel;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

public class Observer implements PropertyChangeListener {
    public JTable table;

    public Observer(JTable table) {
        this.table = table;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals(ObservableProperties.FLOOR_CHANGED.toString())) {
            var newElevator = (ElevatorViewModel)event.getNewValue();
            ElevatorStatus status = ElevatorStatus.FREE;

            if(newElevator.getCurrentActiveUserAmount() == newElevator.getMaxActiveUserAmount() ||
               newElevator.getMaxCapacity() - newElevator.getCurrentCapacity() <= 10) {
                status = ElevatorStatus.FULL;
            }
            else if(newElevator.getCurrentActiveUserAmount() > 0) {
                status = ElevatorStatus.WORK;
            }

            ChangeCellColor(table.getColumnModel(), newElevator.getNumber(), newElevator.getCurrentFloor(), status);

            for(int i = 0; i < Main.getFloorCount(); i++) {
                table.setValueAt("", i,newElevator.getNumber() * 2);

                if(i == Main.getFloorCount() - newElevator.getCurrentFloor()) {
                    table.setValueAt(newElevator.getCurrentActiveUserAmount(), i - 1, newElevator.getNumber() * 2);
                }
            }
        }
        else if(event.getPropertyName().equals(ObservableProperties.QUEUE_CHANGED.toString())) {
            var newUserQueue = (UserQueueViewModel)event.getNewValue();

            StringBuilder waitingPeople = new StringBuilder();
            for(int i = 0; i < newUserQueue.getUsersInQueue(); i++) {
                waitingPeople.append(Emoji.getRandom());
            }
            var queueRenderer = new QueueCellRenderer();
            queueRenderer.setHorizontalAlignment(JLabel.RIGHT);
            table.getColumnModel().getColumn(newUserQueue.getElevatorNumber() * 2 - 1).setCellRenderer(queueRenderer);

            table.setValueAt(waitingPeople.toString(),
                    Main.getFloorCount() - newUserQueue.getCurrentFloor() - 1,
                    newUserQueue.getElevatorNumber() * 2 - 1);
        }
        table.repaint();
    }

    public static void ChangeCellColor(TableColumnModel model, int elevatorIndex, int floorNum, ElevatorStatus status)
    {
        var elevatorRenderer = new ElevatorRenderer(Main.getFloorCount() - floorNum - 1, status);
        elevatorRenderer.setHorizontalAlignment(JLabel.CENTER);
        model.getColumn(elevatorIndex * 2).setCellRenderer(elevatorRenderer);
    }
}
