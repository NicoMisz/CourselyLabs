package com.courselylabs.courselylab.integration.echo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Respuesta de {@code GET /api/vms.php} en echo. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EchoVmListDTO {
    private boolean ok;
    private List<EchoVmDTO> vms;
    private Integer count;
    private String error;
}
