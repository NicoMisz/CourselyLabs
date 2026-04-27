import api from './axios';
import type { CourseDetail, InstructorSummary } from '@/types/course';

export async function getCourseBySlug(slug: string): Promise<CourseDetail> {
  const { data } = await api.get(`/api/courses/slug/${slug}`);
  return data;
}

export async function getCourseInstructors(courseId: string): Promise<InstructorSummary[]> {
  const { data } = await api.get(`/api/courses/${courseId}/instructors`);
  return data;
}

export async function getCategories() {
  const { data } = await api.get('/api/categories');
  return data;
}

// Para el selector de cursos propios en prerequisitos, se necesitan solo id y title, 
// y opcionalmente slug para futuras mejoras (mostrar link al curso)
export interface MyCourseItem {
  id: string
  title: string
  slug: string
  isFree?: boolean
  isPublished?: boolean
}

// Trae los cursos del instructor logueado, para usarlos como 
// opciones de prerequisitos en el CourseWizard
export async function getMyCourses(): Promise<MyCourseItem[]> {
  const { data } = await api.get<MyCourseItem[]>('/api/courses/mine')
  return Array.isArray(data) ? data : []
}