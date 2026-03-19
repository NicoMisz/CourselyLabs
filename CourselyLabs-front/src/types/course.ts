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

export interface CourseDetail {
  id: string;
  slug: string;
  title: string;
  shortDescription?: string;
  description?: string;
  level?: 'PRINCIPIANTE' | 'INTERMEDIO' | 'AVANZADO' | string;
  price?: number;
  free: boolean;
  thumbnailUrl?: string;
  categoryName?: string;
  durationText?: string;
  studentsCount?: number;
  averageRating?: number;
  updatedAt?: string;
  instructors?: InstructorSummary[];
}