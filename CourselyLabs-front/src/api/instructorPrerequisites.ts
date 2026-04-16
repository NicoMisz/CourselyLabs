// src/api/instructorPrerequisites.ts
import api from './axios';

export interface PrerequisiteCourse {
    id: number;
    title: string;
}

export interface Prerequisite {
    id: number;
    prerequisiteCourse: PrerequisiteCourse;
    order: number;
}

// Obtener prerequisitos de un curso
export const getPrerequisites = (courseId: number): Promise<Prerequisite[]> =>
    api.get(`/instructor/courses/${courseId}/prerequisites`).then(r => r.data);

// Añadir prerequisito
export const addPrerequisite = (
    courseId: number,
    prerequisiteCourseId: number,
    order: number
): Promise<Prerequisite> =>
    api
    .post(`/instructor/courses/${courseId}/prerequisites`, {
        prerequisiteCourseId,
        order,
    })
    .then(r => r.data);

// Eliminar prerequisito
export const deletePrerequisite = (
    courseId: number,
    prerequisiteId: number
): Promise<void> =>
    api
    .delete(`/instructor/courses/${courseId}/prerequisites/${prerequisiteId}`)
    .then(r => r.data);

// Buscar cursos publicados para el selector
export const searchPublishedCourses = (
    query: string
): Promise<PrerequisiteCourse[]> =>
    api
    .get('/courses/published', { params: { search: query } })
    .then(r => r.data);