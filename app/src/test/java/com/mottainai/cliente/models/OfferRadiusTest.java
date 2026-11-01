package com.mottainai.cliente.models;

import org.junit.Test;

import static org.junit.Assert.*;

/** Reúne os cenários de teste de OfferRadius. */
public class OfferRadiusTest {
    /** Verifica se a distância inicial é de cinco quilômetros. */
    @Test public void startsAtFiveKilometers() {
        assertEquals(5, OfferRadius.DEFAULT.kilometers());
    }

    /** Verifica a ordem e os valores das opções de raio permitidas. */
    @Test public void exposesOnlyProductApprovedOptionsInOrder() {
        assertEquals(4, OfferRadius.values().length);
        int[] expected = {5, 10, 15, 20};
        for (int index = 0; index < expected.length; index++) {
            assertEquals(expected[index], OfferRadius.values()[index].kilometers());
            assertSame(OfferRadius.values()[index], OfferRadius.fromKilometers(expected[index]));
        }
    }

    /** Verifica a rejeição de distâncias fora das opções permitidas. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsArbitraryRadius() { OfferRadius.fromKilometers(7); }

    /** Verifica a rejeição de um raio igual a zero. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsZero() { OfferRadius.fromKilometers(0); }

    /** Verifica a rejeição de um raio negativo. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeRadius() { OfferRadius.fromKilometers(-5); }
}
