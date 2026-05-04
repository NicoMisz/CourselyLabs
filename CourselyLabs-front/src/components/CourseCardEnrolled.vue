<template>
    <q-card flat bordered class="course-card cursor-pointer" @click="goToCourse">
        <div class="thumb-wrap">
        <CourseThumbnail
            :thumbnail-url="course.thumbnailUrl"
            :title="course.title"
            :rounded="false"
        />

        <div class="progress-bar-wrap">
            <q-linear-progress :value="normalizedProgress" color="primary" track-color="white" rounded />
        </div>
        </div>

        <q-card-section>
        <div class="row items-start justify-between q-col-gutter-sm">
            <div class="col">
            <div class="text-subtitle1 text-weight-medium">{{ course.title }}</div>
            <div class="text-body2 text-grey-7 q-mt-xs">{{ course.shortDescription || 'Continúa con tu aprendizaje.' }}</div>
            </div>
            <q-chip dense :color="statusColor" text-color="white">{{ statusLabel }}</q-chip>
        </div>

        <div class="row items-center justify-between q-mt-md">
            <div class="text-caption text-grey-7">{{ lastAccessLabel }}</div>
            <div class="text-caption text-weight-medium">{{ course.progressPercent }}%</div>
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
    return 'grey-7';
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
}

.course-card:hover {
    transform: translateY(-2px) scale(1.01);
    box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
}

.thumb-wrap {
    position: relative;
}

.progress-bar-wrap {
    position: absolute;
    left: 12px;
    right: 12px;
    bottom: 12px;
}
</style>