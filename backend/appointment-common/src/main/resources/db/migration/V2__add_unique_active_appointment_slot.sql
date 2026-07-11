ALTER TABLE appointment
    ADD COLUMN active_scheduled_at TIMESTAMP
    GENERATED ALWAYS AS (CASE WHEN status <> 'CANCELED' THEN scheduled_at END);

ALTER TABLE appointment
    ADD CONSTRAINT ux_appointment_active_slot UNIQUE (active_scheduled_at);
