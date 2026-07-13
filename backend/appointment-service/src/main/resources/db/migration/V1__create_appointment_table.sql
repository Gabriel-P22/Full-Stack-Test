CREATE TABLE appointment (
    id UUID NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    patient_cpf VARCHAR(11) NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    observation VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_appointment PRIMARY KEY (id)
);
