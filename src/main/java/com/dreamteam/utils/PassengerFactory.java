package com.dreamteam.utils;

import com.dreamteam.console_colors.ConsoleColors;
import com.dreamteam.model2.Floor;
import com.dreamteam.model2.Passenger;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

@Slf4j
public class PassengerFactory {
    private static final Random _random = new Random();
    private static final List<String> _passengerNames =
            List.of("Vitalik", "Katerina", "Karina", "Olexandr",
                    "Ivan", "Viktoria", "Christian",
                    "Vasyl", "Yana", "Tanchik");

    public static synchronized Passenger createPassenger(List<Floor> floors){
        int initialIdx = _random.nextInt(floors.size());
        int finalIdx = _random.nextInt(floors.size());
        if (finalIdx == initialIdx)
            if (finalIdx + 1 == floors.size()) finalIdx--;
            else finalIdx++;

        var passenger = new Passenger(
                _passengerNames.get(
                        _random.nextInt(_passengerNames.size())),
                        _random.nextInt(200),
                        floors.get(initialIdx),
                        floors.get(finalIdx));


        floors.get(initialIdx)
              .addUserToQueue(passenger);

        log.info(
                    ConsoleColors.CYAN +
                    "New user: " +
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
