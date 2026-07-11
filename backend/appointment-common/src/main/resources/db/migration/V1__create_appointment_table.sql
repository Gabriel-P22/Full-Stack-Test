CREATE TABLE appointment (
    id UUID NOT NULL,
    patient_cpf VARCHAR(255),
    patient_name VARCHAR(255),
    scheduled_at TIMESTAMP,
    status VARCHAR(255),
    observation VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_appointment PRIMARY KEY (id)
);
