CREATE TABLE recurring_expense_occurrences
(
    id                   UUID PRIMARY KEY,
    recurring_expense_id UUID           NOT NULL REFERENCES recurring_expenses (id) ON DELETE CASCADE,
    -- Транзакция может быть удалена независимо; история планирования при этом сохраняется.
    transaction_id       UUID REFERENCES transactions (id) ON DELETE SET NULL,
    planned_date         DATE           NOT NULL,
    actual_date          DATE,
    planned_amount       NUMERIC(19, 2) NOT NULL CHECK (planned_amount > 0),
    actual_amount        NUMERIC(19, 2) CHECK (actual_amount IS NULL OR actual_amount > 0),
    status               VARCHAR(32)    NOT NULL,
    created_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Ключ идемпотентности: генерацию будущих платежей можно запускать повторно
    -- (по расписанию, при изменении правила, при открытии календаря) без дублей.
    CONSTRAINT uq_occurrences_rule_planned_date UNIQUE (recurring_expense_id, planned_date)
);

CREATE INDEX idx_occurrences_planned_date ON recurring_expense_occurrences (planned_date);
CREATE INDEX idx_occurrences_status ON recurring_expense_occurrences (status);
CREATE INDEX idx_occurrences_recurring_expense_id ON recurring_expense_occurrences (recurring_expense_id);
