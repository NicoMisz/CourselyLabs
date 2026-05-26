<template>
    <q-card flat bordered class="course-card cursor-pointer" @click="goToCourse">
        <div class="thumb-wrap">
            <CourseThumbnail
                :thumbnail-url="course.thumbnailUrl"
                :title="course.title"
                :rounded="false"
            />

            <!-- Status badge: overlay arriba a la derecha -->
            <q-chip dense :color="statusColor" text-color="white" class="status-chip">
                <q-icon :name="statusIcon" size="14px" class="q-mr-xs" />
                {{ statusLabel }}
            </q-chip>

            <!-- Progress bar: overlay abajo -->
            <div class="progress-bar-wrap">
                <q-linear-progress
                    :value="normalizedProgress"
                    color="primary"
                    track-color="rgba(255,255,255,0.4)"
                    rounded
                    size="6px"
                />
            </div>
        </div>

        <q-card-section>
            <div class="text-subtitle1 text-weight-medium card-title">{{ course.title }}</div>
            <div class="text-body2 q-mt-xs card-description">
                {{ course.shortDescription || 'Continúa con tu aprendizaje.' }}
            </div>

            <div class="row items-center justify-between q-mt-md card-meta">
                <div class="text-caption">{{ lastAccessLabel }}</div>
                <div class="text-caption text-weight-medium percent-label">
                    {{ course.progressPercent }}%
                </div>
            </div>
        </q-card-section>
    </q-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import CourseThumbnail from './CourseThumbnail.vue';
import type { EnrolledCourse } from '../types/enrollment';

const props = defineProps<{ course: EnrolledCourse }>();
const router = useRouter();

const normalizedProgress = computed(() => Math.min(Math.max(props.course.progressPercent / 100, 0), 1));

const statusLabel = computed(() => {
    if (props.course.progressPercent >= 100) return 'Completado';
    if (props.course.progressPercent > 0) return 'En curso';
    return 'Nuevo';
});

const statusColor = computed(() => {
    if (props.course.progressPercent >= 100) return 'positive';
    if (props.course.progressPercent > 0) return 'warning';
    return 'primary';
});

const statusIcon = computed(() => {
    if (props.course.progressPercent >= 100) return 'check_circle';
    if (props.course.progressPercent > 0) return 'play_circle';
    return 'auto_awesome';
});

const lastAccessLabel = computed(() => {
    if (!props.course.lastAccessedAt) return 'Sin acceso reciente';
    return `Último acceso: ${new Date(props.course.lastAccessedAt).toLocaleDateString('es-ES')}`;
});

function goToCourse() {
    router.push(`/cursos/${props.course.slug}`);
}
</script>

<style scoped>
.course-card {
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    overflow: hidden;
}

.course-card:hover {
    transform: translateY(-2px) scale(1.01);
    box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
}

.thumb-wrap {
    position: relative;
}

/* Chip de estado: overlay arriba a la derecha del thumbnail */
.status-chip {
    position: absolute;
    top: 10px;
    right: 10px;
    margin: 0;
    font-weight: 500;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    backdrop-filter: blur(2px);
}

/* Barra de progreso: overlay abajo */
.progress-bar-wrap {
    position: absolute;
    left: 10px;
    right: 10px;
    bottom: 10px;
}

.card-title {
    color: var(--app-text-strong);
}
.card-description {
    color: var(--app-text-soft);
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
.card-meta {
    color: var(--app-text-soft);
}
.percent-label {
    color: var(--app-text-strong);
}
</style>