package com.dreamteam;

import com.dreamteam.model2.Main;
import com.dreamteam.view.*;
import com.dreamteam.view.viewModels.ElevatorViewModel;
import com.dreamteam.view.viewModels.PassengerQueueViewModel;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

public class Observer implements PropertyChangeListener {
    public JTable table;
    private static Random random = new Random();

    public Observer(JTable table) {
        this.table = table;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(ObservableProperties.FLOOR_CHANGED.toString())) {
            var elevator = (ElevatorViewModel)evt.getNewValue();
            ElevatorStatus status = ElevatorStatus.FREE;

            if(elevator.getCurrentActiveUserAmount() > 0) {
                status = ElevatorStatus.WORK;
            }

            if(elevator.getCurrentActiveUserAmount() == elevator.getMaxActiveUserAmount() ||
               elevator.getMaxCapacity() - elevator.getCurrentCapacity() <= 40) {
                status = ElevatorStatus.FULL;
            }

            ChangeCellColor(table.getColumnModel(), elevator.getNumber(), elevator.getCurrentFloor(), status);

            for(int i = 0; i < Main.getFloorCount(); i++) {
                table.setValueAt("", i,elevator.getNumber() * 2);

                if(i == Main.getFloorCount() - elevator.getCurrentFloor()) {
                    table.setValueAt(elevator.getCurrentActiveUserAmount(), i - 1, elevator.getNumber() * 2);
                }
            }
        }

        if(evt.getPropertyName().equals(ObservableProperties.QUEUE_CHANGED.toString())) {
            var userQueue = (PassengerQueueViewModel)evt.getNewValue();
            var unicodeEmojis = new String[] {
                    "\uD83D\uDC69", // man
                    "\uD83D\uDC68", // woman
                    "\uD83D\uDC66", // boy
                    "\uD83D\uDC67", // girl
            };

            StringBuilder cellText = new StringBuilder();

            for(int i = 0; i < userQueue.getUsersInQueue(); i++) {
                var emoji = unicodeEmojis[random.nextInt(unicodeEmojis.length)];
                cellText.append(emoji);
            }

            var QueueRenderer = new QueueCellRenderer();
            QueueRenderer.setHorizontalAlignment(JLabel.RIGHT);
            table.getColumnModel().getColumn(userQueue.getElevatorNumber() * 2 - 1).setCellRenderer(QueueRenderer);

            table.setValueAt(cellText.toString(),
                    Main.getFloorCount() - userQueue.getCurrentFloor() - 1,
                    userQueue.getElevatorNumber() * 2 - 1);
        }

        table.repaint();
    }

    public static void ChangeCellColor(TableColumnModel model, int elevatorIndex, int floorIndex, ElevatorStatus status)
    {
        var ElevatorRenderer = new ElevatorRenderer(Main.getFloorCount() - floorIndex - 1, status);
        ElevatorRenderer.setHorizontalAlignment(JLabel.CENTER);
        model.getColumn(elevatorIndex * 2).setCellRenderer(ElevatorRenderer);
    }
}
