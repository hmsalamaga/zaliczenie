package edu.iis.mto.testreactor.dishwasher;

import static edu.iis.mto.testreactor.dishwasher.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import edu.iis.mto.testreactor.dishwasher.engine.Engine;
import edu.iis.mto.testreactor.dishwasher.engine.EngineException;
import edu.iis.mto.testreactor.dishwasher.pump.PumpException;
import edu.iis.mto.testreactor.dishwasher.pump.WaterPump;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    FillLevel unrelevantFillLevel;
    ProgramConfiguration programConfiguration;
    DishWasher dishWasher;
    WashingProgram notRinseProgram;

    @BeforeEach
    void setUp() {
        dishWasher = new DishWasher(waterPump, engine, dirtFilter, door);
        notRinseProgram = WashingProgram.ECO;
        unrelevantFillLevel = FillLevel.HALF;
        programConfiguration = programConfiguration(notRinseProgram, true);
    }

    @Test
    public void start_programIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            dishWasher.start(null);
        });
    }

    @Test
    public void start_doorIsNotClosed_returnsDoorOpenError() {
        lenient().when(door.closed()).thenReturn(false);
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(error(DOOR_OPEN), samePropertyValuesAs(result));
    }

    @Test
    public void start_dirtFilterIsFilled_returnsFilterError() {
        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(30.0d);
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(error(ERROR_FILTER), samePropertyValuesAs(result));
    }

    @Test
    public void start_withProperAttributes_returnsSuccess() {
        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(70.0d);
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(success(programConfiguration.getProgram()), samePropertyValuesAs(result));
    }

    @Test
    public void start_withProperAttributes_callInstancesInProperOrder() throws PumpException, EngineException {
        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(70.0d);
        dishWasher.start(programConfiguration);

        InOrder callOrder = Mockito.inOrder(door, dirtFilter, waterPump, engine);
        callOrder.verify(door).closed();
        callOrder.verify(dirtFilter).capacity();
        callOrder.verify(waterPump).pour(unrelevantFillLevel);
        callOrder.verify(engine).runProgram(programConfiguration.getProgram());
        callOrder.verify(waterPump).drain();
        callOrder.verify(door).unlock();
    }

    @Test
    public void start_doorAreClosedAndDirtFilterIsNotFilled_callDoor() {
        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(70.0d);
        dishWasher.start(programConfiguration);

        verify(door).closed();
        verify(door).unlock();
    }

    @Test
    public void start_withProperAttributes_callDirtFilter() {
        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(70.0d);
        dishWasher.start(programConfiguration);

        verify(dirtFilter).capacity();
    }

    @Test
    public void start_withProperAttributes_callWaterPumpTwice() throws PumpException {
        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(70.0d);
        dishWasher.start(programConfiguration);

        verify(waterPump, Mockito.times(2)).pour(unrelevantFillLevel);
        verify(waterPump, Mockito.times(2)).drain();
    }

    @Test
    public void start_programIsRinse_callWaterPumpOnce() throws PumpException {
        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(70.0d);
        programConfiguration = programConfiguration(WashingProgram.RINSE, true);
        dishWasher.start(programConfiguration);

        verify(waterPump).pour(unrelevantFillLevel);
        verify(waterPump).drain();
    }

    @Test
    public void start_withProperAttributes_callEngine() throws EngineException {
        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(70.0d);
        dishWasher.start(programConfiguration);

        verify(engine).runProgram(programConfiguration.getProgram());
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
                                   .withFillLevel(unrelevantFillLevel)
                                   .build();
    }
}
