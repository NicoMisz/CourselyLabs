import { ref } from 'vue'
import api from '@/api/axios'
import type { Course } from '@/types/course'

export function useCourses() {
  const courses = ref<Course[]>([])
  const loading = ref(false)
  const error = ref('')

  async function fetchCourses() {
    loading.value = true
    error.value = ''
    try {
      const { data } = await api.get<Course[]>('/api/courses/all')
      courses.value = data
    } catch {
      error.value = 'Error al cargar los cursos.'
    } finally {
      loading.value = false
    }
  }

  return { courses, loading, error, fetchCourses }
}
