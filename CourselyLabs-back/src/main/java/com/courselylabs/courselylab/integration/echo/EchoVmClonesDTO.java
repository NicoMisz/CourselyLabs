package com.courselylabs.courselylab.integration.echo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Respuesta de {@code GET /api/vm_clones.php?template_id=N} en echo. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EchoVmClonesDTO {
    private boolean ok;
    private List<EchoVmDTO> clones;
    private String error;
}
