package com.desafio.agendamento.adapters.out.persistence.entity;

import com.desafio.agendamento.entities.Appointment;
import com.desafio.agendamento.entities.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment")
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String patientName;

    @Column
    private String patientCpf;

    @Column
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status;

    @Column
    private String observation;

    @Column
    private LocalDateTime createdAt;

    protected AppointmentEntity() {}

    public AppointmentEntity(UUID id, String patientName, String patientCpf, LocalDateTime scheduledAt,
                              Status status, String observation, LocalDateTime createdAt) {
        this.id = id;
        this.patientName = patientName;
        this.patientCpf = patientCpf;
        this.scheduledAt = scheduledAt;
        this.status = status;
        this.observation = observation;
        this.createdAt = createdAt;
    }

    public static AppointmentEntity fromDomain(final Appointment domainEntity) {
        return new AppointmentEntity(
                domainEntity.id(),
                domainEntity.patientName(),
                domainEntity.patientCpf(),
                domainEntity.scheduledAt(),
                domainEntity.status(),
                domainEntity.observation(),
                domainEntity.createdAt()
        );
    }

    public Appointment toDomain() {
        return new Appointment(
                this.id,
                this.patientName,
                this.patientCpf,
                this.scheduledAt,
                this.status,
                this.observation,
                this.createdAt
        );
    }

}
