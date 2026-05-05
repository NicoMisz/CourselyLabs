package com.courselylabs.courselylab.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lesson_blocks")
public class LessonBlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private LessonEntity lesson;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "position", nullable = false)
    private Integer position = 0;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "lab_provider", length = 20)
    private String labProvider;

    @Column(name = "lab_template_id")
    private Integer labTemplateId;

    @Column(name = "lab_instructions", columnDefinition = "TEXT")
    private String labInstructions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
