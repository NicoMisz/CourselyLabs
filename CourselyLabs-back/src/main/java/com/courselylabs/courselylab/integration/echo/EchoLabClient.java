package com.courselylabs.courselylab.integration.echo;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP para la API pública de echo (laboratorios virtuales sobre Proxmox).
 * <p>
 * Todos los métodos hacen proxy con un Bearer token del usuario. El backend nunca
 * expone el token al frontend; el flujo es:
 *   1. El usuario pega su token de echo en el perfil de CourselyLabs.
 *   2. El backend lo cifra y persiste.
 *   3. Cuando el usuario interactúa con un bloque lab, el backend lo desencripta,
 *      llama a echo en su nombre y devuelve solo el resultado relevante.
 * <p>
 * No usamos un service "técnico" compartido: cada llamada se hace con el token del
 * usuario real para que echo aplique sus propias reglas de permiso.
 */
@Component
public class EchoLabClient {

    private final RestClient http;

    public EchoLabClient(
            @Value("${app.echo.base-url}") String baseUrl,
            @Value("${app.echo.timeout-seconds}") int timeoutSeconds) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(timeoutSeconds * 1000);
        rf.setReadTimeout(timeoutSeconds * 1000);
        this.http = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(rf)
                .build();
    }

    /** Verifica que el token de echo es válido y devuelve los datos del usuario. */
    public EchoUserDTO me(String echoToken) {
        return http.get()
                .uri("/api/me.php")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + echoToken)
                .retrieve()
                .body(EchoUserDTO.class);
    }

    /** Lista las VMs visibles para el usuario en echo. */
    public EchoVmListDTO listVms(String echoToken) {
        return http.get()
                .uri("/api/vms.php")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + echoToken)
                .retrieve()
                .body(EchoVmListDTO.class);
    }

    /** Estado actual de una VM concreta. */
    public EchoVmStatusDTO getVmStatus(String echoToken, int vmId) {
        return http.get()
                .uri(b -> b.path("/api/vm_status.php").queryParam("vm_id", vmId).build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + echoToken)
                .retrieve()
                .body(EchoVmStatusDTO.class);
    }

    /** Inicia o detiene una VM. {@code action} ∈ {"start", "stop"}. */
    public EchoVmStatusDTO setVmAction(String echoToken, int vmId, String action) {
        return http.post()
                .uri("/api/vm_status.php")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + echoToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("vm_id", vmId, "action", action))
                .retrieve()
                .body(EchoVmStatusDTO.class);
    }

    /** Devuelve los clones (instancias) de una plantilla; cada usuario tiene un clone propio. */
    public EchoVmClonesDTO listClones(String echoToken, int templateId) {
        return http.get()
                .uri(b -> b.path("/api/vm_clones.php").queryParam("template_id", templateId).build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + echoToken)
                .retrieve()
                .body(EchoVmClonesDTO.class);
    }
}
