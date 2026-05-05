package com.courselylabs.courselylab.integration.echo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Respuesta de {@code /api/vm_status.php} (GET y POST start/stop). */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EchoVmStatusDTO {
    private boolean ok;
    /** {@code running}, {@code stopped}, ... */
    private String status;
    @JsonProperty("last_active")
    private String lastActive;
    /** Solo presente en POST: indica si Proxmox aplicó la acción. */
    private Boolean proxmox;
    private String error;
}
