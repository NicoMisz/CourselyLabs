<script setup lang="ts">
    import { computed } from 'vue'
    import type { CoursePrerequisite, CoursePrerequisiteStatus, RelatedCourseItem } from '@/types/prerequisite'

    const props = defineProps<{
        prerequisites?: CoursePrerequisite[]
        statuses: CoursePrerequisiteStatus[]
        requiredBy?: RelatedCourseItem[]
        loading?: boolean
        isLoggedIn?: boolean
    }>()

    // Cuando hay statuses (usuario logueado e inscrito), los preferimos por traer progreso.
    // Cuando no, mostramos la lista plana de prerequisitos como info pública.
    const showPlainList = computed(() =>
        !props.statuses.length && !!props.prerequisites?.length
    )

    const hasContent = computed(() =>
        props.statuses.length > 0 ||
        showPlainList.value ||
        (props.requiredBy?.length ?? 0) > 0
    )
</script>

<template>
    <section>
        <q-skeleton v-if="loading" type="rect" height="90px" class="q-mb-sm" />

        <template v-else>
            <q-banner v-if="!hasContent" class="app-banner-soft" rounded>
                Este curso no tiene cursos relacionados.
            </q-banner>

            <!-- Logueado e inscrito: prerequisitos con progreso del usuario -->
            <template v-if="statuses.length">
                <div class="text-subtitle1 q-mb-sm">Necesitas completar antes</div>
                <div class="row q-col-gutter-md q-mb-md">
                    <div v-for="s in statuses" :key="s.prerequisiteCourseId" class="col-12 col-sm-6">
                        <q-card flat bordered class="q-pa-md">
                            <router-link :to="`/cursos/${s.slug}`" class="text-primary text-weight-medium">
                                {{ s.title }}
                            </router-link>
                            <div class="text-caption text-grey-7 q-mt-xs">
                                Progreso: {{ s.progressPercent }}% · Requerido: {{ s.requiredThreshold }}%
                            </div>
                            <div class="text-caption" :class="s.completed ? 'text-positive' : 'text-orange-9'">
                                {{ s.completed ? 'Completado' : 'Pendiente' }}
                            </div>
                        </q-card>
                    </div>
                </div>
            </template>

            <!-- No logueado o no inscrito: lista plana sin progreso -->
            <template v-else-if="showPlainList">
                <div class="text-subtitle1 q-mb-sm">Necesitas completar antes</div>
                <div class="row q-col-gutter-md q-mb-md">
                    <div v-for="p in prerequisites" :key="p.id" class="col-12 col-sm-6">
                        <q-card flat bordered class="q-pa-md">
                            <router-link :to="`/cursos/${p.prerequisiteSlug}`" class="text-primary text-weight-medium">
                                {{ p.prerequisiteTitle }}
                            </router-link>
                            <div class="text-caption text-grey-7 q-mt-xs">
                                Requerido: {{ p.completionThreshold }}%
                            </div>
                        </q-card>
                    </div>
                </div>
                <q-banner v-if="!isLoggedIn" class="app-banner-soft q-mt-sm" rounded dense>
                    <template v-slot:avatar>
                        <q-icon name="info" color="primary" />
                    </template>
                    <router-link to="/login" class="text-primary text-weight-medium">Inicia sesión</router-link>
                    para ver tu progreso en estos cursos.
                </q-banner>
            </template>

            <template v-if="requiredBy?.length">
                <div class="text-subtitle1 q-mb-sm">Este curso desbloquea</div>
                <div class="row q-col-gutter-md">
                    <div v-for="r in requiredBy" :key="r.courseId" class="col-12 col-sm-6">
                    <q-card flat bordered class="q-pa-md">
                        <router-link :to="`/cursos/${r.slug}`" class="text-primary text-weight-medium">
                        {{ r.title }}
                        </router-link>
                        <div class="text-caption text-grey-7 q-mt-xs">
                        Umbral: {{ r.completionThreshold ?? 80 }}%
                        </div>
                    </q-card>
                    </div>
                </div>
            </template>
        </template>
    </section>
</template>
