import api from './axios';
import type {
    CreateEnrollmentPayload,
    EnrolledCourse,
    EnrollmentResponse,
} from '../types/enrollment';

export async function checkEnrollment(courseId: string): Promise<boolean> {
    const { data } = await api.get<boolean>('/api/enrollments/check', {
        params: { courseId },
    });
    return data;
}

export async function createEnrollment(
    payload: CreateEnrollmentPayload,
): Promise<EnrollmentResponse> {
    const { data } = await api.post<EnrollmentResponse>('/api/enrollments/me', payload);
    return data;
}

export async function getMyCourses(): Promise<EnrolledCourse[]> {
    const { data } = await api.get<EnrolledCourse[]>('/api/enrollments/me/courses');
    return data;
}

export async function updateEnrollmentLastAccess(enrollmentId: string): Promise<void> {
    await api.patch(`/api/enrollments/${enrollmentId}/access`);
}