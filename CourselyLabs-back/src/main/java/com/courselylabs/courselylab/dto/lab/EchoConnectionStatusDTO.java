package com.courselylabs.courselylab.dto.lab;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Estado de la conexión del usuario con echo. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EchoConnectionStatusDTO {
    private boolean connected;
    private String username;     // nombre del usuario en echo
    private String roleName;     // rol en echo (Admin, Teacher, Student, ...)
    private String error;        // si connected=false y hubo error de validación
}
