package edu.iis.mto.testreactor.dishwasher;

import static edu.iis.mto.testreactor.dishwasher.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

        lenient().when(door.closed()).thenReturn(true);
        lenient().when(dirtFilter.capacity()).thenReturn(70.0d);
    }

    @Test
    public void start_withProperAttributes_shouldReturnSuccess() {
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(success(programConfiguration.getProgram()), samePropertyValuesAs(result));
    }

    @Test
    public void start_programIsNull_shouldThrowNullPointerExceptionWithProperMessage() {
        assertThrows(NullPointerException.class, () -> {
            dishWasher.start(null);
        }, "program == null");
    }

    @Test
    public void start_doorIsNotClosed_shouldReturnDoorOpenError() {
        lenient().when(door.closed()).thenReturn(false);
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(error(DOOR_OPEN), samePropertyValuesAs(result));
    }

    @Test
    public void start_dirtFilterIsFilled_shouldReturnFilterError() {
        lenient().when(dirtFilter.capacity()).thenReturn(30.0d);
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(error(ERROR_FILTER), samePropertyValuesAs(result));
    }

    @Test
    public void start_engineThrowsAnException_shouldReturnProgramError() throws EngineException {
        doThrow(EngineException.class).when(engine).runProgram(programConfiguration.getProgram());
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(error(ERROR_PROGRAM), samePropertyValuesAs(result));
    }

    @Test
    public void start_waterPumpThrowsAnException_shouldReturnPumpError() throws PumpException {
        doThrow(PumpException.class).when(waterPump).pour(unrelevantFillLevel);
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(error(ERROR_PUMP), samePropertyValuesAs(result));
    }

    @Test
    public void start_withProperAttributes_shouldCallInstancesInProperOrder() throws PumpException, EngineException {
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
    public void start_withProperAttributes_shouldCallDoor() {
        dishWasher.start(programConfiguration);

        verify(door).closed();
        verify(door).unlock();
    }

    @Test
    public void start_withProperAttributes_shouldCallDirtFilter() {
        dishWasher.start(programConfiguration);

        verify(dirtFilter).capacity();
    }

    @Test
    public void start_withProperAttributes_shouldCallWaterPumpTwice() throws PumpException {
        dishWasher.start(programConfiguration);

        verify(waterPump, Mockito.times(2)).pour(unrelevantFillLevel);
        verify(waterPump, Mockito.times(2)).drain();
    }

    @Test
    public void start_programIsRinse_shouldCallWaterPumpOnce() throws PumpException {
        programConfiguration = programConfiguration(WashingProgram.RINSE, true);
        dishWasher.start(programConfiguration);

        verify(waterPump).pour(unrelevantFillLevel);
        verify(waterPump).drain();
    }

    @Test
    public void start_programIsWithoutWashingTablets_shouldNotCallDirtFilter() {
        programConfiguration = programConfiguration(WashingProgram.ECO, false);
        dishWasher.start(programConfiguration);

        verify(dirtFilter, Mockito.times(0)).capacity();
    }

    @Test
    public void start_withProperAttributes_shouldCallEngine() throws EngineException {
        dishWasher.start(programConfiguration);

        verify(engine).runProgram(programConfiguration.getProgram());
    }

    private RunResult error(Status errorPump) {
        return RunResult.builder().withStatus(errorPump).build();
    }

    private RunResult success(WashingProgram program) {
        return RunResult.builder().withStatus(SUCCESS).withRunMinutes(program.getTimeInMinutes()).build();
    }

    private ProgramConfiguration programConfiguration(WashingProgram program, boolean tabletsUsed) {
        return ProgramConfiguration.builder().withProgram(program).withTabletsUsed(tabletsUsed)
                                   .withFillLevel(unrelevantFillLevel).build();
    }
}
