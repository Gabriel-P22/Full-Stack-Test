package com.appointment.adapters.out.persistence.entity;

import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "appointment")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String patientCpf;

    @Column
    private String patientName;

    @Column
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status;

    @Column
    private String observation;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    protected AppointmentEntity() {}

    public AppointmentEntity(UUID id, String patientCpf, String patientName, LocalDateTime scheduledAt, Status status, String observation, LocalDateTime createdAt, LocalDateTime updatedAt, String idempotencyKey) {
        this.id = id;
        this.patientCpf = patientCpf;
        this.patientName = patientName;
        this.scheduledAt = scheduledAt;
        this.status = status;
        this.observation = observation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public static AppointmentEntity fromDomain(final Appointment domainEntity) {
        return new AppointmentEntity(
                domainEntity.id(),
                domainEntity.patientCpf(),
                domainEntity.patientName(),
                domainEntity.scheduledAt(),
                domainEntity.status(),
                domainEntity.observation().orElse(null),
                domainEntity.createdAt(),
                null,
                domainEntity.idempotencyKey()
        );
    }

    public Appointment toDomain() {
        return new Appointment(
                this.id,
                this.patientCpf,
                this.patientName,
                this.scheduledAt,
                this.status,
                Optional.ofNullable(this.observation),
                this.createdAt,
                this.updatedAt,
                this.idempotencyKey
        );
    }

}