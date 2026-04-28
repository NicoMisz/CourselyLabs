// Este archivo define las interfaces relacionadas con los 
// prerequisitos de cursos, usadas en CoursePrerequisitesView.vue 
// y en la gestión de prerequisitos en el backend.

// Representa un prerequisito de curso, con la información necesaria 
// para mostrarlo en la vista de gestión de prerequisitos y para 
// validar el acceso al curso.
export interface CoursePrerequisite {
    id: string
    courseId: string
    prerequisiteCourseId: string
    prerequisiteTitle: string
    prerequisiteSlug: string
    completionThreshold: number
}

// Representa el estado de cumplimiento de un prerequisito para un usuario,
// usado para mostrar en la vista de detalle del curso si el usuario ha cumplido 
// o no los prerequisitos.
export interface CoursePrerequisiteStatus {
    prerequisiteCourseId: string
    title: string
    slug: string
    enrolled: boolean
    completed: boolean
    completedLessons: number
    totalLessons: number
    progressPercent: number
    requiredThreshold: number
}

// Representa un prerequisito bloqueado, es decir, un curso que el usuario no ha cumplido
export interface BlockedPrerequisite {
    courseId: string
    title: string
    slug: string
    enrolled: boolean
    completed: boolean
    completedLessons: number
    totalLessons: number
    progressPercent: number
    requiredThreshold: number
}

// Payload para crear o actualizar un prerequisito, usado en el CourseWizard.vue
export interface CreateCoursePrerequisitePayload {
    prerequisiteCourseId: string
    completionThreshold?: number
}

// Payload para sincronizar la lista completa de prerequisitos de 
// un curso, usado en el CourseWizard.vue
export interface SyncCoursePrerequisitesPayload {
    prerequisites: CreateCoursePrerequisitePayload[]
}

// Representa un curso relacionado, que puede ser un prerequisito o un 
// curso que requiere el curso actual como prerequisito
export interface RelatedCourseItem {
    courseId: string
    title: string
    slug: string
    completionThreshold?: number
}

// Respuesta del backend para obtener los cursos relacionados de un curso,
// usada en CoursePrerequisitesView.vue para mostrar los prerequisitos y 
// cursos que requieren el curso actual
export interface CourseRelatedResponse {
    prerequisites: RelatedCourseItem[]
    requiredBy: RelatedCourseItem[]
}