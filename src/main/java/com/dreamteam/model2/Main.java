package com.dreamteam.model2;

import com.dreamteam.Observer;
import com.dreamteam.utils.PassengerFactory;
import com.dreamteam.utils.SpeedControl;
import com.dreamteam.view.FloorRenderer;
import com.dreamteam.view.MainForm;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class Main {

    private static int floorCount;
    private static int elevatorCount;
    private static Observer observer;
    private static Timer timer;
    private static boolean working;


    public static void main(String[] args) {
        createAndShowGUI();
    }

    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("Gachi Project");

        var form  = new MainForm();
        form.getStartButton().addActionListener(e -> ExecuteAlgorithm(form));
        form.getResetButton().addActionListener(e -> StopAlgorithm(form));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                StopAlgorithm(form);
                System.exit(0);
            }
        });

        form.getSpinnerFloorAmount().setModel(new SpinnerNumberModel(10, 3, 30, 1));
        form.getSpinnerElevatorAmount().setModel(new SpinnerNumberModel(3, 1, 8, 1));
        JSlider slider = form.getSpeedSlider();
        slider.setInverted(true);
        slider.setModel(new DefaultBoundedRangeModel(150, 0, 100, 200));
        slider.addChangeListener(e -> SpeedControl.update(((JSlider)e.getSource()).getValue()));

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

    private static void StopAlgorithm(MainForm form){
        if (working) {
            timer.cancel();
            Set<Thread> threads = Thread.getAllStackTraces().keySet();
            threads.forEach(Thread::interrupt);
            clearTable(form);
            working = false;
        }
    }

    private static void ExecuteAlgorithm(MainForm form) {
        if(working) {
            JOptionPane.showMessageDialog(null, "Elevators are already working ;)");
            return;
        }

        clearTable(form);
        createTable(form);

        List<Floor> floorList = new ArrayList<>();
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
        if(strategy.equals("Direct")){
            for (int i = 0; i < elevatorCount; ++i) {
                elevatorList.add(new DirectElevator(floorList.get(0), observer));
            }

        } else {
            for (int i = 0; i < elevatorCount; ++i) {
                elevatorList.add(new AccumulativeElevator(floorList.get(0), observer));
            }
        }

        floorList.forEach(f -> f.initQueues(elevatorList));

        elevatorList.forEach(el -> new Thread(() -> {
            try {
                el.processElevator();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start());

        working = true;

        TimerTask task = new TimerTask() {
                    @SneakyThrows
                    public void run() {
                        new Thread(() -> {
                            Passenger passenger = PassengerFactory.createPassenger(floorList);
                            passenger.invokeElevator();
                        }).start();
                        }
            };
        timer = new Timer();
        long delay = 0L;
        long period = 60L;
        timer.scheduleAtFixedRate(task, delay,period);
    }


    public static int getFloorCount() {
        return floorCount;
    }
}

