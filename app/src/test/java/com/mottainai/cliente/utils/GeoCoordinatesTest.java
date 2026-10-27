package com.mottainai.cliente.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class GeoCoordinatesTest {
    @Test public void acceptsZeroAndBothGeographicBoundaries() {
        assertTrue(GeoCoordinates.areValid(0.0, 0.0));
        assertTrue(GeoCoordinates.areValid(-90.0, -180.0));
        assertTrue(GeoCoordinates.areValid(90.0, 180.0));
        assertTrue(GeoCoordinates.areValid(-23.55, -46.63));
    }

    @Test public void rejectsMissingPairs() {
        assertFalse(GeoCoordinates.areValid(null, null));
        assertFalse(GeoCoordinates.areValid(0.0, null));
        assertFalse(GeoCoordinates.areValid(null, 0.0));
    }

    @Test public void rejectsLatitudeOutsideBounds() {
        assertFalse(GeoCoordinates.areValid(-90.001, 0.0));
        assertFalse(GeoCoordinates.areValid(90.001, 0.0));
    }

    @Test public void rejectsLongitudeOutsideBounds() {
        assertFalse(GeoCoordinates.areValid(0.0, -180.001));
        assertFalse(GeoCoordinates.areValid(0.0, 180.001));
    }

    @Test public void rejectsNonFiniteValues() {
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertFalse(GeoCoordinates.areValid(value, 0.0));
            assertFalse(GeoCoordinates.areValid(0.0, value));
        }
    }
}
