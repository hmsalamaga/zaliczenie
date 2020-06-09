package edu.iis.mto.testreactor.dishwasher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.iis.mto.testreactor.dishwasher.engine.Engine;
import edu.iis.mto.testreactor.dishwasher.pump.WaterPump;
import org.hamcrest.Matchers;
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

}
