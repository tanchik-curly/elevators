package com.gachiMadElevator.utils;

import com.gachiMadElevator.console_colors.ConsoleColors;
import com.gachiMadElevator.services.Floor;
import com.gachiMadElevator.services.Passenger;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

@Slf4j
public class PassengerFactory {
    private static final Random random = new Random();
    private static final List<String> passengerNames =
            List.of("Vitalik", "Katerina", "Karina", "Olexandr",
                    "Ivan", "Viktoria", "Christian",
                    "Vasyl", "Yana", "Tanchik");

    public static synchronized Passenger createPassenger(List<Floor> floors){
        int initialIdx = random.nextInt(floors.size());
        int finalIdx = random.nextInt(floors.size());
        if (finalIdx == initialIdx)
            if (finalIdx + 1 == floors.size()) {
                finalIdx--;
            } else {
                finalIdx++;
            }

        var passenger = new Passenger(
                passengerNames.get(
                        random.nextInt(passengerNames.size())),
                        random.ints(20, 100).findFirst().getAsInt(),
                        floors.get(initialIdx),
                        floors.get(finalIdx));


        floors.get(initialIdx).addPassengerToQueue(passenger);
        log.info(
                    ConsoleColors.CYAN +
                    "New pasenger: " +
                    passenger.getPassengerName() +
                    ", ID: " +
                    passenger.getPassengerId() +
                    ", Start floor: " +
                    passenger.getInitialFloor().getCurrent() +
                    ", Destination floor: " +
                    passenger.getFinalFloor().getCurrent() +
                    ", " +
                    passenger.getExecutiveElevator().getClass().getSimpleName() +
                    "" +
                    passenger.getExecutiveElevator().getId() +
                    ConsoleColors.RESET
        );
        return passenger;
    }
}
