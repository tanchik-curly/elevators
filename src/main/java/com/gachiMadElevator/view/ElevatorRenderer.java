package com.gachiMadElevator.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ElevatorRenderer extends DefaultTableCellRenderer {
    private final int floor;
    private final ElevatorStatus status;

    public ElevatorRenderer(int floor, ElevatorStatus status)
    {
        this.floor = floor;
        this.status = status;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setBackground(Color.WHITE);
        c.setForeground(Color.BLACK);

        if (row == floor) {
            switch(status) {
                case FREE:
                    c.setBackground(Color.GREEN);
                    break;
                case WORK:
                    c.setBackground(Color.ORANGE);
                    break;
                case FULL:
                    c.setBackground(Color.RED);
                    break;
                default:
                    c.setBackground(Color.WHITE);
            }
        }

        return c;
    }
}