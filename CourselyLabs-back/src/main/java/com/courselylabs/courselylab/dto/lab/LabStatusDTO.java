package com.courselylabs.courselylab.dto.lab;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Estado del laboratorio del usuario en un bloque concreto. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabStatusDTO {
    /** {@code NO_TOKEN}, {@code NO_VM}, {@code STOPPED}, {@code RUNNING}, {@code ERROR}. */
    private String state;
    private Integer vmId;        // id en echo
    private String vmName;
    private String message;      // detalle adicional (errores, instrucciones)
}
