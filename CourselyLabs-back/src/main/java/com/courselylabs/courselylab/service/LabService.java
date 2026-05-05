package com.courselylabs.courselylab.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.courselylabs.courselylab.dto.lab.EchoConnectionStatusDTO;
import com.courselylabs.courselylab.dto.lab.LabConsoleDTO;
import com.courselylabs.courselylab.dto.lab.LabStatusDTO;
import com.courselylabs.courselylab.entity.LabSessionEventEntity;
import com.courselylabs.courselylab.entity.LessonBlockEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.integration.echo.EchoLabClient;
import com.courselylabs.courselylab.integration.echo.EchoUserDTO;
import com.courselylabs.courselylab.integration.echo.EchoVmClonesDTO;
import com.courselylabs.courselylab.integration.echo.EchoVmDTO;
import com.courselylabs.courselylab.integration.echo.EchoVmStatusDTO;
import com.courselylabs.courselylab.repository.LabSessionEventRepository;
import com.courselylabs.courselylab.repository.LessonBlockRepository;
import com.courselylabs.courselylab.repository.UserRepository;
import com.courselylabs.courselylab.security.SecretEncryptor;

/**
 * Orquesta la integración con echo:
 *   - guarda/cifra el token personal del usuario,
 *   - resuelve el clone de VM asignado al usuario para una plantilla,
 *   - hace start/stop/console proxy,
 *   - audita cada operación en {@code lab_session_events}.
 *
 * Nada del token sale de aquí: los controllers reciben DTOs sin secrets.
 */
@Service
@Transactional
public class LabService {

    private final UserRepository userRepository;
    private final LessonBlockRepository blockRepository;
    private final LabSessionEventRepository eventRepository;
    private final SecretEncryptor encryptor;
    private final EchoLabClient echo;

    public LabService(UserRepository userRepository,
                      LessonBlockRepository blockRepository,
                      LabSessionEventRepository eventRepository,
                      SecretEncryptor encryptor,
                      EchoLabClient echo) {
        this.userRepository = userRepository;
        this.blockRepository = blockRepository;
        this.eventRepository = eventRepository;
        this.encryptor = encryptor;
        this.echo = echo;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Conexión del token de echo
    // ──────────────────────────────────────────────────────────────────────

    public EchoConnectionStatusDTO getConnectionStatus(String email) {
        UserEntity user = requireUser(email);
        if (user.getEchoTokenEncrypted() == null) {
            return new EchoConnectionStatusDTO(false, null, null, null);
        }
        try {
            EchoUserDTO me = echo.me(decryptToken(user));
            if (me == null || !me.isOk()) {
                return new EchoConnectionStatusDTO(false, null, null,
                        "El token guardado ya no es válido. Vuelve a conectarlo.");
            }
            return new EchoConnectionStatusDTO(true, me.getUsername(), me.getRoleName(), null);
        } catch (Exception e) {
            return new EchoConnectionStatusDTO(false, null, null,
                    "No se pudo contactar con echo: " + e.getMessage());
        }
    }

    public EchoConnectionStatusDTO connectToken(String email, String rawToken) {
        UserEntity user = requireUser(email);
        // Validar contra echo antes de persistir.
        EchoUserDTO me;
        try {
            me = echo.me(rawToken);
        } catch (Exception e) {
            throw new BadRequestException("No se pudo validar el token con echo: " + e.getMessage());
        }
        if (me == null || !me.isOk()) {
            throw new BadRequestException("El token de echo no es válido");
        }

        user.setEchoTokenEncrypted(encryptor.encrypt(rawToken));
        user.setEchoTokenUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new EchoConnectionStatusDTO(true, me.getUsername(), me.getRoleName(), null);
    }

    public void disconnectToken(String email) {
        UserEntity user = requireUser(email);
        user.setEchoTokenEncrypted(null);
        user.setEchoTokenUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Operaciones del laboratorio
    // ──────────────────────────────────────────────────────────────────────

    public LabStatusDTO getStatus(String email, UUID blockId) {
        UserEntity user = requireUser(email);
        LessonBlockEntity block = requireLabBlock(blockId);

        if (user.getEchoTokenEncrypted() == null) {
            return new LabStatusDTO("NO_TOKEN", null, null,
                    "Conecta tu cuenta de echo desde tu perfil para acceder al laboratorio.");
        }

        try {
            String token = decryptToken(user);
            Optional<EchoVmDTO> myClone = findMyClone(token, block.getLabTemplateId());
            if (myClone.isEmpty()) {
                return new LabStatusDTO("NO_VM", null, null,
                        "No tienes una VM asignada de esta plantilla. Pide a tu instructor que te asigne una.");
            }
            EchoVmDTO vm = myClone.get();
            EchoVmStatusDTO live = safe(() -> echo.getVmStatus(token, vm.getId()));
            String state = mapVmStatus(live != null ? live.getStatus() : vm.getStatus());
            return new LabStatusDTO(state, vm.getId(), vm.getName(), null);
        } catch (Exception e) {
            log(user, block, "error", "status: " + e.getMessage());
            return new LabStatusDTO("ERROR", null, null,
                    "Error al consultar echo: " + e.getMessage());
        }
    }

    public LabStatusDTO start(String email, UUID blockId) {
        return changeState(email, blockId, "start");
    }

    public LabStatusDTO stop(String email, UUID blockId) {
        return changeState(email, blockId, "stop");
    }

    public LabConsoleDTO openConsole(String email, UUID blockId) {
        UserEntity user = requireUser(email);
        LessonBlockEntity block = requireLabBlock(blockId);

        if (user.getEchoTokenEncrypted() == null) {
            return error("Conecta tu cuenta de echo en tu perfil antes de abrir la consola.");
        }

        String token = decryptToken(user);
        Optional<EchoVmDTO> myClone = findMyClone(token, block.getLabTemplateId());
        if (myClone.isEmpty()) {
            return error("No tienes una VM asignada para este laboratorio.");
        }
        try {
            Map<String, Object> ticket = echo.getConsoleTicket(token, myClone.get().getId().intValue());
            log(user, block, "console", "vm_id=" + myClone.get().getId());
            return new LabConsoleDTO(true, ticket, null);
        } catch (Exception e) {
            log(user, block, "error", "console: " + e.getMessage());
            return error("No se pudo obtener el ticket de consola: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Privados
    // ──────────────────────────────────────────────────────────────────────

    private LabStatusDTO changeState(String email, UUID blockId, String action) {
        UserEntity user = requireUser(email);
        LessonBlockEntity block = requireLabBlock(blockId);

        if (user.getEchoTokenEncrypted() == null) {
            return new LabStatusDTO("NO_TOKEN", null, null,
                    "Conecta tu cuenta de echo antes de operar el laboratorio.");
        }

        try {
            String token = decryptToken(user);
            Optional<EchoVmDTO> myClone = findMyClone(token, block.getLabTemplateId());
            if (myClone.isEmpty()) {
                return new LabStatusDTO("NO_VM", null, null,
                        "No tienes una VM asignada de esta plantilla.");
            }
            EchoVmDTO vm = myClone.get();
            EchoVmStatusDTO result = echo.setVmAction(token, vm.getId(), action);
            log(user, block, action, "vm_id=" + vm.getId() + " result=" + (result != null && result.isOk()));
            String state = mapVmStatus(result != null ? result.getStatus() : null);
            return new LabStatusDTO(state, vm.getId(), vm.getName(), null);
        } catch (Exception e) {
            log(user, block, "error", action + ": " + e.getMessage());
            return new LabStatusDTO("ERROR", null, null,
                    "Error al " + action + " la VM: " + e.getMessage());
        }
    }

    private Optional<EchoVmDTO> findMyClone(String token, Integer templateId) {
        if (templateId == null) return Optional.empty();
        EchoVmClonesDTO clones = echo.listClones(token, templateId);
        if (clones == null || !clones.isOk() || clones.getClones() == null) return Optional.empty();
        // El backend de echo marca con `is_me`, pero también podemos identificar por user id.
        EchoUserDTO me = echo.me(token);
        Integer myEchoId = me != null ? me.getId() : null;
        return clones.getClones().stream()
                .filter(c -> myEchoId != null && myEchoId.equals(c.getAssignedTo()))
                .findFirst();
    }

    private String mapVmStatus(String echoStatus) {
        if (echoStatus == null) return "ERROR";
        return switch (echoStatus) {
            case "running" -> "RUNNING";
            case "stopped" -> "STOPPED";
            default -> "ERROR";
        };
    }

    private String decryptToken(UserEntity user) {
        return encryptor.decrypt(user.getEchoTokenEncrypted());
    }

    private UserEntity requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private LessonBlockEntity requireLabBlock(UUID blockId) {
        LessonBlockEntity block = blockRepository.findById(blockId)
                .orElseThrow(() -> new ResourceNotFoundException("LessonBlock", "id", blockId));
        if (!"lab".equals(block.getType())) {
            throw new BadRequestException("El bloque indicado no es de tipo laboratorio");
        }
        if (block.getLabTemplateId() == null) {
            throw new BadRequestException("El bloque no tiene una plantilla de VM configurada");
        }
        return block;
    }

    private void log(UserEntity user, LessonBlockEntity block, String action, String detail) {
        LabSessionEventEntity ev = new LabSessionEventEntity();
        ev.setUser(user);
        ev.setBlock(block);
        ev.setAction(action);
        ev.setDetail(detail);
        eventRepository.save(ev);
    }

    private LabConsoleDTO error(String message) {
        return new LabConsoleDTO(false, null, message);
    }

    @FunctionalInterface
    private interface SafeCall<T> {
        T call() throws Exception;
    }

    private <T> T safe(SafeCall<T> call) {
        try {
            return call.call();
        } catch (Exception e) {
            return null;
        }
    }
}
