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