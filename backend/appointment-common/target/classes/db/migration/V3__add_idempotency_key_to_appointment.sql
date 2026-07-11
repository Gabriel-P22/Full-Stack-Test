ALTER TABLE appointment
    ADD COLUMN idempotency_key VARCHAR(255);

ALTER TABLE appointment
    ADD CONSTRAINT ux_appointment_idempotency_key UNIQUE (idempotency_key);
