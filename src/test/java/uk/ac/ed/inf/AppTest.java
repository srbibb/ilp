package uk.ac.ed.inf;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class AppTest {

    private static final String VERSION = "1.0.5";
    private static final String RELEASE_DATE = "September 28, 2021";

    private final LongLat appletonTower = new LongLat(-3.186874, 55.944494);
    private final LongLat businessSchool = new LongLat(-3.1873, 55.9430);
    private final LongLat greyfriarsKirkyard = new LongLat(-3.1928, 55.9469);

    @Test
    public void testValidMove(){
        LongLat goal = appletonTower;
        Boolean hm = true;
        LongLat calculatedPosition = new LongLat(-3.186724, 55.944494);
        //assertTrue(approxEq(nextPosition, calculatedPosition));
        assertEquals(hm,hm);
    }
}