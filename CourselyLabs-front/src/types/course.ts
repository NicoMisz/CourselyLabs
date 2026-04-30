import type { CoursePrerequisite } from './prerequisite';

export interface Course {
  id: string
  slug: string
  title: string
  shortDescription: string
  level: string
  isFree: boolean
  price: number
  thumbnailUrl?: string
  categoryName?: string
  averageRating?: number
  studentsCount?: number
}

export interface InstructorSummary {
  id: string;
  name: string;
  bio?: string;
  avatarUrl?: string;
  /** True si es el instructor principal del curso. */
  isMain?: boolean;
}

import type { Section } from './lesson'

export interface CourseDetail {
  id: string;
  slug: string;
  title: string;
  shortDescription?: string;
  description?: string;
  level?: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO' | string;
  price?: number;
  isFree: boolean;
  thumbnailUrl?: string;
  categoryId?: number;
  categoryName?: string;
  durationText?: string;
  studentsCount?: number;
  averageRating?: number;
  storageBytes?: number;
  updatedAt?: string;
  instructors?: InstructorSummary[];
  sections?: Section[];
  prerequisites?: CoursePrerequisite[];
}