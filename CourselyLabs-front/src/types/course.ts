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
  categoryName?: string;
  durationText?: string;
  studentsCount?: number;
  averageRating?: number;
  updatedAt?: string;
  instructors?: InstructorSummary[];
  sections?: Section[];
}