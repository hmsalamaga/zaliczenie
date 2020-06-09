package edu.iis.mto.testreactor.dishwasher;

import static edu.iis.mto.testreactor.dishwasher.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

import edu.iis.mto.testreactor.dishwasher.engine.Engine;
import edu.iis.mto.testreactor.dishwasher.pump.WaterPump;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DishWasherTest {

    @Mock
    WaterPump waterPump;
    @Mock
    Engine engine;
    @Mock
    DirtFilter dirtFilter;
    @Mock
    Door door;

    ProgramConfiguration programConfiguration;
    DishWasher dishWasher;

    @BeforeEach
    void setUp() {
        dishWasher = new DishWasher(waterPump, engine, dirtFilter, door);
        WashingProgram unrelevantWashingProgram = WashingProgram.ECO;
        programConfiguration = programConfiguration(unrelevantWashingProgram, true);
    }

    @Test
    public void start_programIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            dishWasher.start(null);
        });
    }

    @Test
    public void start_doorIsNotClosed_ReturnsDoorOpenError() {
        lenient().when(door.closed()).thenReturn(false);
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(error(DOOR_OPEN), samePropertyValuesAs(result));
    }

    @Test
    public void start_dirtFilterisFilled_ReturnsFilterError() {
        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(30.0d);
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(error(ERROR_FILTER), samePropertyValuesAs(result));
    }

    @Test
    public void start_doorAreClosedAndDirtFilterIsNotFilled_ReturnsSuccess() {
        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(70.0d);
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(success(programConfiguration.getProgram()), samePropertyValuesAs(result));
    }

    private RunResult error(Status errorPump) {
        return RunResult.builder()
                        .withStatus(errorPump)
                        .build();
    }

    private RunResult success(WashingProgram program) {
        return RunResult.builder()
                        .withStatus(SUCCESS)
                        .withRunMinutes(program.getTimeInMinutes())
                        .build();
    }

    private ProgramConfiguration programConfiguration(WashingProgram program, boolean tabletsUsed) {
        return ProgramConfiguration.builder()
                                   .withProgram(program)
                                   .withTabletsUsed(tabletsUsed)
                                   .withFillLevel(FillLevel.HALF)
                                   .build();
    }
}
