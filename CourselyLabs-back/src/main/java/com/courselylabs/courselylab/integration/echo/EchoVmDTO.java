package com.courselylabs.courselylab.integration.echo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Una VM tal como la describe echo en {@code /api/vms.php} y {@code /api/vm_clones.php}. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EchoVmDTO {
    private Integer id;
    private String name;
    /** {@code running}, {@code stopped}, ... */
    private String status;
    @JsonProperty("os_type")
    private String osType;
    @JsonProperty("ram_mb")
    private Integer ramMb;
    @JsonProperty("cpu_cores")
    private Integer cpuCores;
    @JsonProperty("disk_gb")
    private Integer diskGb;
    @JsonProperty("is_template")
    private Boolean isTemplate;
    @JsonProperty("template_id")
    private Integer templateId;
    @JsonProperty("assigned_to")
    private Integer assignedTo;
    @JsonProperty("last_active")
    private String lastActive;
}
