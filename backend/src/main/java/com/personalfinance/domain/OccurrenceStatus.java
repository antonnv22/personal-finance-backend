package com.personalfinance.domain;

/**
 * Состояние запланированного платежа.
 *
 * <p>Просроченность (OVERDUE) намеренно не хранится: это производная величина
 * {@code PLANNED && plannedDate < today}, которая менялась бы со временем сама
 * по себе. Она вычисляется при отдаче наружу.
 */
public enum OccurrenceStatus {
    PLANNED,
    COMPLETED,
    SKIPPED
}
