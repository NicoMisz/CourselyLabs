package com.courselylabs.courselylab.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.courselylabs.courselylab.dto.LessonResourceDTO;
import com.courselylabs.courselylab.entity.CourseEntity;
import com.courselylabs.courselylab.entity.LessonEntity;
import com.courselylabs.courselylab.entity.LessonResourceEntity;
import com.courselylabs.courselylab.entity.UserEntity;
import com.courselylabs.courselylab.exception.BadRequestException;
import com.courselylabs.courselylab.exception.ResourceNotFoundException;
import com.courselylabs.courselylab.repository.CourseRepository;
import com.courselylabs.courselylab.repository.LessonRepository;
import com.courselylabs.courselylab.repository.LessonResourceRepository;
import com.courselylabs.courselylab.repository.UserRepository;

@Service
@Transactional
public class LessonResourceService {

    private static final long MAX_BYTES_USER = 300L * 1024 * 1024;   // 300 MB
    private static final long MAX_BYTES_PREMIUM = 1024L * 1024 * 1024; // 1 GB

    private static final Set<String> ALLOWED_TYPES = Set.of(
            // Documents
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // Images
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml",
            // Text / code
            "text/plain", "text/markdown", "text/csv", "application/json",
            // Archives
            "application/zip", "application/x-zip-compressed"
    );

    private static final long MAX_THUMBNAIL_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> THUMBNAIL_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final LessonResourceRepository resourceRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final FileStorageService storage;

    public LessonResourceService(LessonResourceRepository resourceRepository,
                                  LessonRepository lessonRepository,
                                  CourseRepository courseRepository,
                                  UserRepository userRepository,
                                  FileStorageService storage) {
        this.resourceRepository = resourceRepository;
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.storage = storage;
    }

    // --- Public methods ---

    @Transactional(readOnly = true)
    public List<LessonResourceDTO> findByLessonId(UUID lessonId) {
        return resourceRepository.findByLessonIdOrderByPositionAsc(lessonId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public LessonResourceDTO uploadResource(UUID lessonId, MultipartFile file, String email) {
        validateResource(file);

        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        CourseEntity course = lesson.getSection().getCourse();
        checkCourseSizeLimit(course, file.getSize(), email);

        String storageKey = "resources/" + course.getId() + "/" + lessonId + "/"
                + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());

        try {
            storage.upload(storageKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (java.io.IOException e) {
            throw new BadRequestException("Error leyendo el archivo");
        }

        LessonResourceEntity entity = new LessonResourceEntity();
        entity.setLesson(lesson);
        entity.setFileName(file.getOriginalFilename());
        entity.setStorageKey(storageKey);
        entity.setFileSize(file.getSize());
        entity.setMimeType(file.getContentType());
        entity.setPosition(resourceRepository.findByLessonIdOrderByPositionAsc(lessonId).size());
        entity = resourceRepository.save(entity);

        // Increment course storage usage
        course.setStorageBytes((course.getStorageBytes() == null ? 0 : course.getStorageBytes()) + file.getSize());
        courseRepository.save(course);

        return toDTO(entity);
    }

    public String getDownloadUrl(UUID resourceId) {
        LessonResourceEntity resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        resource.setDownloadCount(resource.getDownloadCount() + 1);
        resourceRepository.save(resource);

        return storage.getPresignedUrl(resource.getStorageKey());
    }

    public void deleteResource(UUID resourceId) {
        LessonResourceEntity resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        storage.delete(resource.getStorageKey());

        // Decrement course storage
        CourseEntity course = resource.getLesson().getSection().getCourse();
        long current = course.getStorageBytes() == null ? 0 : course.getStorageBytes();
        course.setStorageBytes(Math.max(0, current - resource.getFileSize()));
        courseRepository.save(course);

        resourceRepository.delete(resource);
    }

    // --- Generic uploads (thumbnails, lesson content) ---

    public String uploadThumbnail(UUID courseId, MultipartFile file, String email) {
        if (file.getSize() > MAX_THUMBNAIL_BYTES) {
            throw new BadRequestException("El thumbnail no puede superar 5 MB");
        }
        if (!THUMBNAIL_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Formato no permitido. Usa JPG, PNG, WebP o GIF.");
        }

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        checkCourseSizeLimit(course, file.getSize(), email);

        String storageKey = "thumbnails/" + courseId + "/" + UUID.randomUUID()
                + "-" + sanitize(file.getOriginalFilename());

        try {
            storage.upload(storageKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (java.io.IOException e) {
            throw new BadRequestException("Error leyendo el archivo");
        }

        // Update course storage
        course.setStorageBytes((course.getStorageBytes() == null ? 0 : course.getStorageBytes()) + file.getSize());
        courseRepository.save(course);

        return storage.getPresignedUrl(storageKey);
    }

    public String uploadLessonContent(UUID lessonId, MultipartFile file, String email) {
        validateResource(file);

        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        CourseEntity course = lesson.getSection().getCourse();
        checkCourseSizeLimit(course, file.getSize(), email);

        String storageKey = "lesson-content/" + course.getId() + "/" + lessonId + "/"
                + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());

        try {
            storage.upload(storageKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (java.io.IOException e) {
            throw new BadRequestException("Error leyendo el archivo");
        }

        course.setStorageBytes((course.getStorageBytes() == null ? 0 : course.getStorageBytes()) + file.getSize());
        courseRepository.save(course);

        return storage.getPresignedUrl(storageKey);
    }

    // --- Helpers ---

    private void validateResource(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Archivo vacio");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Tipo de archivo no permitido: " + file.getContentType());
        }
    }

    private void checkCourseSizeLimit(CourseEntity course, long newBytes, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String role = user.getRole() == null ? "user" : user.getRole();
        if ("admin".equals(role)) return;

        long limit = "premium".equals(role) ? MAX_BYTES_PREMIUM : MAX_BYTES_USER;
        long current = course.getStorageBytes() == null ? 0 : course.getStorageBytes();

        if (current + newBytes > limit) {
            long limitMb = limit / (1024 * 1024);
            throw new BadRequestException(
                "Superas el limite de almacenamiento del curso (" + limitMb + " MB). "
                + "Hazte Premium para ampliar a 1 GB por curso.");
        }
    }

    private String sanitize(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private LessonResourceDTO toDTO(LessonResourceEntity e) {
        return new LessonResourceDTO(
                e.getId(),
                e.getLesson().getId(),
                e.getFileName(),
                e.getFileSize(),
                e.getMimeType(),
                e.getDownloadCount(),
                e.getPosition(),
                e.getCreatedAt()
        );
    }
}
