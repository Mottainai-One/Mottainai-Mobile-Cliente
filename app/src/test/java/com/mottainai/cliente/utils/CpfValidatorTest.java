package com.mottainai.cliente.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class CpfValidatorTest {
    @Test public void acceptsValidCheckDigits() {
        assertTrue(CpfValidator.isValid("52998224725"));
        assertTrue(CpfValidator.isValid("11144477735"));
    }

    @Test public void preservesLeadingZeros() {
        assertTrue(CpfValidator.isValid("01234567890"));
    }

    @Test public void rejectsEitherIncorrectCheckDigit() {
        assertFalse(CpfValidator.isValid("52998224715"));
        assertFalse(CpfValidator.isValid("52998224724"));
    }

    @Test public void rejectsAllRepeatedDigitSequences() {
        for (int digit = 0; digit <= 9; digit++) {
            assertFalse(CpfValidator.isValid(String.valueOf(digit).repeat(11)));
        }
    }

    @Test public void rejectsMissingOrIncorrectLength() {
        assertFalse(CpfValidator.isValid(null));
        assertFalse(CpfValidator.isValid(""));
        assertFalse(CpfValidator.isValid("5299822472"));
        assertFalse(CpfValidator.isValid("529982247250"));
    }

    @Test public void expectsOnlyAsciiDigitsAfterFormNormalization() {
        assertFalse(CpfValidator.isValid("5299822472a"));
        assertFalse(CpfValidator.isValid("529.982.247-25"));
        assertFalse(CpfValidator.isValid("５２９９８２２４７２５"));
    }
}
