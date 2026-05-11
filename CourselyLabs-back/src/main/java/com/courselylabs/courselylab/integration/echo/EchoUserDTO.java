package com.courselylabs.courselylab.integration.echo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Respuesta de {@code GET /api/me.php} en echo. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EchoUserDTO {
    private boolean ok;
    private Integer id;
    private String username;
    private String email;
    private Integer role;
    @JsonProperty("role_name")
    private String roleName;
    @JsonProperty("center_id")
    private Integer centerId;
    private String lang;
    private String error;
}
