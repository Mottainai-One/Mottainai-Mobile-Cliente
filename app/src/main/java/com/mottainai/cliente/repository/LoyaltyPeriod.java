package com.mottainai.cliente.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Inclusive monthly bounds at the PostgreSQL timestamp microsecond precision. */
public final class LoyaltyPeriod {
    public final LocalDateTime from;
    public final LocalDateTime to;

    public LoyaltyPeriod(LocalDate month) {
        from = month.withDayOfMonth(1).atStartOfDay();
        to = from.plusMonths(1).minusNanos(1000);
    }
}
