<script setup lang="ts">
import { computed } from 'vue'
import type { BlockedPrerequisite, CoursePrerequisite } from '@/types/prerequisite'

const props = defineProps<{
	prerequisites: CoursePrerequisite[]
	blockers: BlockedPrerequisite[]
}>()

const blockerBySlug = computed(() => {
	const map = new Map<string, BlockedPrerequisite>()
	for (const b of props.blockers ?? []) {
		map.set(b.slug, b)
	}
	return map
})
</script>

<template>
    <section v-if="prerequisites.length || blockers.length" class="q-mb-lg">
		<div class="text-h6 q-mb-md">Antes de empezar</div>

		<!-- Lista principal -->
		<div v-if="prerequisites.length" class="row q-col-gutter-md">
			<div v-for="prereq in prerequisites" :key="prereq.id" class="col-12 col-sm-6">
				<q-card flat bordered class="q-pa-md">
					<div class="text-subtitle1 text-weight-medium">{{ prereq.prerequisiteTitle }}</div>
					<div class="text-body2 text-grey-7">Slug: {{ prereq.prerequisiteSlug }}</div>

					<div class="text-caption text-grey-7 q-mt-xs">
						Requerido: {{ prereq.completionThreshold }}%
					</div>

					<div class="text-caption text-grey-7">
						Progreso actual:
						{{ blockerBySlug.get(prereq.prerequisiteSlug)?.progressPercent ?? 'Cumplido o no disponible' }}
						<template v-if="blockerBySlug.get(prereq.prerequisiteSlug)">%</template>
					</div>
				</q-card>
			</div>
		</div>

		<!-- Fallback por blockers -->
		<div v-else class="row q-col-gutter-md">
			<div v-for="b in blockers" :key="b.courseId" class="col-12 col-sm-6">
				<q-card flat bordered class="q-pa-md">
					<div class="text-subtitle1 text-weight-medium">{{ b.title }}</div>
					<div class="text-body2 text-grey-7">Slug: {{ b.slug }}</div>
					<div class="text-caption text-grey-7 q-mt-xs">
						Progreso: {{ b.progressPercent }}% · Requerido: {{ b.requiredThreshold }}%
					</div>
				</q-card>
			</div>
		</div>
	</section>
</template>