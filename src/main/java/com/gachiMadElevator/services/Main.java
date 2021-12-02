package com.gachiMadElevator.services;

import com.gachiMadElevator.helpers.Observer;
import com.gachiMadElevator.utils.CustomThread;
import com.gachiMadElevator.utils.PassengerFactory;
import com.gachiMadElevator.utils.SpeedControl;
import com.gachiMadElevator.view.FloorRenderer;
import com.gachiMadElevator.view.MainForm;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.Timer;

@Slf4j
public class Main {

    private static int floorCount;
    private static int elevatorCount;
    private static Observer observer;
    private static Timer timer;
    private static boolean working;
    private static boolean suspending;
    private static List<Floor> floorList;
    private static List<CustomThread> threadList;


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("Gachi Project");

        var form  = new MainForm();
        form.getStartButton().addActionListener(e -> executeAlgorithm(form));
        form.getResetButton().addActionListener(e -> stopAlgorithm(form));
        form.getPauseButton().addActionListener(e -> pauseAlgorithm());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                stopAlgorithm(form);
                System.exit(0);
            }
        });

        form.getSpinnerFloorAmount().setModel(new SpinnerNumberModel(10, 3, 30, 1));
        form.getSpinnerElevatorAmount().setModel(new SpinnerNumberModel(3, 1, 8, 1));
        JSlider slider = form.getElevatorSpeedSlider();
        slider.setInverted(true);
        slider.setModel(new DefaultBoundedRangeModel(150, 0, 100, 200));
        slider.addChangeListener(e -> SpeedControl.setElevatorSpeed(((JSlider)e.getSource()).getValue()));

        slider = form.getQueueSpeedSlider();
        slider.setInverted(true);
        slider.setModel(new DefaultBoundedRangeModel(50, 0, 20, 80));
        slider.addChangeListener(e -> {
            SpeedControl.setQueueSpeed(((JSlider)e.getSource()).getValue());
            if (working) {
                restartTimer();
            }
        });

        frame.setContentPane(form.getRootPanel());
        frame.pack();
        frame.setVisible(true);

        observer = new Observer(form.getTable1());
    }

    private static void getNumbers(MainForm form) {
        elevatorCount = (int)form.getSpinnerElevatorAmount().getValue();
        floorCount = (int)form.getSpinnerFloorAmount().getValue();
    }

    private static void clearTable(MainForm form){
        DefaultTableModel model = (DefaultTableModel)form.getTable1().getModel();
        model.setRowCount(0);
        model.setColumnCount(1);
    }

    private static void createTable(MainForm form) {
        DefaultTableModel model = (DefaultTableModel)form.getTable1().getModel();

        getNumbers(form);
        var table = form.getTable1();
        model.addColumn("Floor");

        for(int i = 0; i < elevatorCount * 2-1; i++) {
            model.addColumn("Elevator #" + i);
        }
        for(int i = 0; i < floorCount; i++) {
            model.addRow(new Object[elevatorCount]);
            model.setValueAt(floorCount - i, i, 0);
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.setRowHeight(form.getTable1().getHeight() / floorCount);

        FloorRenderer floorRenderer = new FloorRenderer();
        floorRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(floorRenderer);
    }

    private static void stopAlgorithm(MainForm form){
        if (working) {
            timer.cancel();
            threadList.forEach(Thread::interrupt);
            threadList.clear();
            clearTable(form);
            working = false;
            suspending = false;
        }
    }

    private static void pauseAlgorithm() {
        if (!working) {
            JOptionPane.showMessageDialog(null, "Can't suspend/resume elevators if they are not started ;)");
            return;
        }

        if (suspending) {
            restartTimer();
            for (var th: threadList) {
                th.resumeThread();
            }
            suspending = false;
        } else {
            timer.cancel();
            for (var th: threadList) {
                th.suspendThread();
            }
            suspending = true;
        }

    }

    private static void executeAlgorithm(MainForm form) {
        if(working) {
            JOptionPane.showMessageDialog(null, "Elevators are already working ;)");
            return;
        }

        clearTable(form);
        createTable(form);

        floorList = new ArrayList<>();
        List<Elevator> elevatorList = new ArrayList<>();

        for (int i = 0; i < floorCount; ++i) {
            floorList.add(new Floor(i, observer));
        }

        floorList.get(0).setPrevious(null);
        for (int i = 0, j = 1; j < floorCount; ++i, ++j) {
            floorList.get(j).setPrevious(floorList.get(i));
            floorList.get(i).setNext(floorList.get(j));
        }
        floorList.get(floorCount - 1).setNext(null);

        String strategy = Objects.requireNonNull(form.getComboBoxStrategy().getSelectedItem()).toString();

        Elevator.setCounter(0);
        if (strategy.equals("Direct")){
            for (int i = 0; i < elevatorCount; ++i) {
                elevatorList.add(new Elevator(floorList.get(0), observer, new DirectElevator()));
            }
        } else {
            for (int i = 0; i < elevatorCount; ++i) {
                elevatorList.add(new Elevator(floorList.get(0), observer, new AccumulativeElevator()));
            }
        }
        floorList.forEach(f -> f.initQueues(elevatorList));

        threadList = new LinkedList<>();
        for (var el: elevatorList) {
            CustomThread th = new CustomThread(() -> {
                try {
                    el.processElevator();
                } catch (InterruptedException e) {
                    log.info("Thread was successfully reset");
                    Thread.currentThread().interrupt();
                }
            });
            threadList.add(th);
            th.start();
        }
        restartTimer();
        working = true;
    }

    private static void restartTimer() {
        long delay = 0L;
        long period = SpeedControl.getQueueSpeed().get();
        TimerTask task = new TimerTask() {
            public void run() {
                new Thread(() -> {
                    Passenger passenger = PassengerFactory.createPassenger(floorList);
                    passenger.invokeElevator();
                }).start();
            }
        };
        if (working && !suspending) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(task, delay, period);
    }


    public static int getFloorCount() {
        return floorCount;
    }
}

