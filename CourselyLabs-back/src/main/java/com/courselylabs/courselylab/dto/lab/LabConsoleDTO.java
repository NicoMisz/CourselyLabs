package com.courselylabs.courselylab.dto.lab;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ticket VNC corto para abrir noVNC desde el frontend.
 * Devolvemos el payload entero de echo (que incluye ticket, port, host…)
 * porque el formato exacto puede variar y el frontend lo pasa tal cual a noVNC.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabConsoleDTO {
    private boolean ok;
    private Map<String, Object> ticket;   // payload bruto de echo
    private String error;
}
