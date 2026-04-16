// src/composables/usePrerequisites.ts
import { ref } from 'vue';
import {
  getPrerequisites,
  addPrerequisite,
  deletePrerequisite,
  searchPublishedCourses,
  type Prerequisite,
  type PrerequisiteCourse,
} from '../api/instructorPrerequisites';

export function usePrerequisites(courseId: number) {
    const prerequisites = ref<Prerequisite[]>([]);
    const options = ref<PrerequisiteCourse[]>([]);
    const loading = ref(false);

    const load = async () => {
        prerequisites.value = await getPrerequisites(courseId);
    };

    const search = async (query: string) => {
        if (!query) return;
        options.value = await searchPublishedCourses(query);
    };

    const add = async (prerequisiteCourseId: number) => {
        loading.value = true;
        try {
        const next = prerequisites.value.length + 1;
        const created = await addPrerequisite(courseId, prerequisiteCourseId, next);
        prerequisites.value.push(created);
        } finally {
        loading.value = false;
        }
    };

    const remove = async (prerequisiteId: number) => {
        await deletePrerequisite(courseId, prerequisiteId);
        prerequisites.value = prerequisites.value.filter(p => p.id !== prerequisiteId);
    };

    return { prerequisites, options, loading, load, search, add, remove };
}