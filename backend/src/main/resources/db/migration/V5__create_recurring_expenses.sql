CREATE TABLE recurring_expenses
(
    id              UUID PRIMARY KEY,
    user_id         UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    account_id      UUID           NOT NULL REFERENCES accounts (id) ON DELETE RESTRICT,
    category_id     UUID           NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    name            VARCHAR(255)   NOT NULL,
    description     VARCHAR(500),
    planned_amount  NUMERIC(19, 2) NOT NULL CHECK (planned_amount > 0),
    currency        VARCHAR(8)     NOT NULL,
    recurrence_type VARCHAR(32)    NOT NULL,
    day_of_month    INTEGER        NOT NULL CHECK (day_of_month BETWEEN 1 AND 31),
    start_date      DATE           NOT NULL,
    end_date        DATE,
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_recurring_expenses_period CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_recurring_expenses_user_id ON recurring_expenses (user_id);
CREATE INDEX idx_recurring_expenses_active ON recurring_expenses (active);
CREATE INDEX idx_recurring_expenses_user_active ON recurring_expenses (user_id, active);
