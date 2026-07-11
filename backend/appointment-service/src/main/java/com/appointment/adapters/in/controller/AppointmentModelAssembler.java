package com.appointment.adapters.in.controller;

import com.appointment.adapters.in.controller.dtos.AppointmentResponse;
import com.appointment.entities.Appointment;
import com.appointment.enums.Status;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AppointmentModelAssembler {

    public EntityModel<AppointmentResponse> toModel(Appointment appointment) {
        AppointmentResponse response = AppointmentResponse.fromModel(appointment);

        EntityModel<AppointmentResponse> model = EntityModel.of(response,
                linkTo(methodOn(AppointmentController.class).findById(appointment.id())).withSelfRel(),
                linkTo(methodOn(AppointmentController.class).list(null, null)).withRel("appointments"));

        if (appointment.status() == Status.PENDING) {
            model.add(linkTo(methodOn(AppointmentController.class)
                    .updateStatus(appointment.id(), null)).withRel("confirm"));
            model.add(linkTo(methodOn(AppointmentController.class)
                    .updateStatus(appointment.id(), null)).withRel("cancel"));
        } else if (appointment.status() == Status.CONFIRMED) {
            model.add(linkTo(methodOn(AppointmentController.class)
                    .updateStatus(appointment.id(), null)).withRel("cancel"));
        }

        return model;
    }
}
