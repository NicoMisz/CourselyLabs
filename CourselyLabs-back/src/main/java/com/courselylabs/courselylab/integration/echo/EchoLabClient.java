package com.courselylabs.courselylab.integration.echo;

import java.net.Socket;
import java.net.http.HttpClient;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
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
            @Value("${app.echo.timeout-seconds}") int timeoutSeconds,
            @Value("${app.echo.trust-all-certs:false}") boolean trustAllCerts) {
        ClientHttpRequestFactory rf = trustAllCerts
                ? trustAllRequestFactory(timeoutSeconds)
                : simpleRequestFactory(timeoutSeconds);

        // echo a veces devuelve JSON con Content-Type text/html (algunos endpoints PHP no
        // setean cabecera correcta). Aceptamos ambos para no romper la deserialización.
        JacksonJsonHttpMessageConverter jsonConverter = new JacksonJsonHttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                MediaType.valueOf("text/html"),
                MediaType.valueOf("text/plain")
        ));

        this.http = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(rf)
                .configureMessageConverters(b -> b.registerDefaults().withJsonConverter(jsonConverter))
                .build();
    }

    private static ClientHttpRequestFactory simpleRequestFactory(int timeoutSeconds) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(timeoutSeconds * 1000);
        rf.setReadTimeout(timeoutSeconds * 1000);
        return rf;
    }

    /**
     * Crea un HttpClient que confía en cualquier certificado TLS.
     * Solo se usa si {@code app.echo.trust-all-certs=true}, normalmente en dev
     * cuando echo.lab tiene un cert self-signed.
     */
    private static ClientHttpRequestFactory trustAllRequestFactory(int timeoutSeconds) {
        try {
            TrustManager[] trustAll = new TrustManager[]{ new X509ExtendedTrustManager() {
                @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                @Override public void checkClientTrusted(X509Certificate[] c, String a) {}
                @Override public void checkServerTrusted(X509Certificate[] c, String a) {}
                @Override public void checkClientTrusted(X509Certificate[] c, String a, Socket s) {}
                @Override public void checkServerTrusted(X509Certificate[] c, String a, Socket s) {}
                @Override public void checkClientTrusted(X509Certificate[] c, String a, SSLEngine e) {}
                @Override public void checkServerTrusted(X509Certificate[] c, String a, SSLEngine e) {}
            }};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAll, new java.security.SecureRandom());

            // Disable hostname verification too (echo.lab cert puede no coincidir con el host).
            System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");

            HttpClient client = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .build();
            return new JdkClientHttpRequestFactory(client);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo configurar SSL trust-all para echo", e);
        }
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

    /**
     * Pide a echo un ticket VNC corto para conectar noVNC al Proxmox.
     * El payload exacto depende de Proxmox; lo devolvemos como Map para que
     * el frontend lo pase tal cual a noVNC.
     */
    @SuppressWarnings("unchecked")
    public java.util.Map<String, Object> getConsoleTicket(String echoToken, int vmId) {
        return http.post()
                .uri("/api/vm_console.php")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + echoToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("vm_id", vmId))
                .retrieve()
                .body(java.util.Map.class);
    }
}
