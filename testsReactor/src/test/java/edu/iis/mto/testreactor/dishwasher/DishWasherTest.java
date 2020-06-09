package edu.iis.mto.testreactor.dishwasher;

import static edu.iis.mto.testreactor.dishwasher.Status.DOOR_OPEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Mock
    ProgramConfiguration programConfiguration;

    DishWasher dishWasher;

    @BeforeEach
    void setUp() {
        dishWasher = new DishWasher(waterPump, engine, dirtFilter, door);
    }

    @Test
    public void start_programIsNull_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            dishWasher.start(null);
        });
    }

    @Test
    public void start_doorIsNotClosed_ReturnDoorOpenError() {
        lenient().when(door.closed()).thenReturn(false);
        RunResult result = dishWasher.start(programConfiguration);

        assertThat(error(DOOR_OPEN), samePropertyValuesAs(result));
    }

    private RunResult error(Status errorPump) {
        return RunResult.builder()
                        .withStatus(errorPump)
                        .build();
    }
}
