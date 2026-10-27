package com.mottainai.cliente.models;

import org.junit.Test;

import static org.junit.Assert.*;

public class OfferRadiusTest {
    @Test public void startsAtFiveKilometers() {
        assertEquals(5, OfferRadius.DEFAULT.kilometers());
    }

    @Test public void exposesOnlyProductApprovedOptionsInOrder() {
        assertEquals(4, OfferRadius.values().length);
        int[] expected = {5, 10, 15, 20};
        for (int index = 0; index < expected.length; index++) {
            assertEquals(expected[index], OfferRadius.values()[index].kilometers());
            assertSame(OfferRadius.values()[index], OfferRadius.fromKilometers(expected[index]));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsArbitraryRadius() { OfferRadius.fromKilometers(7); }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsZero() { OfferRadius.fromKilometers(0); }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeRadius() { OfferRadius.fromKilometers(-5); }
}
