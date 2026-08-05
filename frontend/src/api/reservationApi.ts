import type { AdminReservationFilters, AdminReservationPage, DoctorReservationFilters, DoctorReservationPage, DoctorReservationPatient, DoctorReservationStatusUpdate, DoctorSchedule, PatientReservationPage, ReservationCancelResponse, ReservationCreateRequest, ReservationCreateResponse, ReservationFilters, ReservationStatusResponse } from '../types/reservation';
import { apiClient } from './apiClient';

export function getAvailableSchedules(doctorId: number): Promise<DoctorSchedule[]> {
  return apiClient<DoctorSchedule[]>({ url: `/api/v1/doctors/${doctorId}/available-schedules`, method: 'GET' }, false);
}

export function createReservation(request: ReservationCreateRequest): Promise<ReservationCreateResponse> {
  return apiClient<ReservationCreateResponse>({ url: '/api/v1/reservations/', method: 'POST', data: request });
}

export function getPatientReservations(filters: ReservationFilters): Promise<PatientReservationPage> {
  return apiClient<PatientReservationPage>({
    url: '/api/v1/reservations/patient',
    method: 'GET',
    params: filters,
  });
}

export function cancelReservation(reservationId: number): Promise<ReservationCancelResponse> {
  return apiClient<ReservationCancelResponse>({ url: `/api/v1/reservations/${reservationId}/cancel`, method: 'PATCH' });
}

export function getDoctorReservations(filters: DoctorReservationFilters): Promise<DoctorReservationPage> {
  return apiClient<DoctorReservationPage>({ url: '/api/v1/doctors/reservations', method: 'GET', params: filters });
}

export function getDoctorReservationPatient(reservationId: number): Promise<DoctorReservationPatient> {
  return apiClient<DoctorReservationPatient>({ url: `/api/v1/doctors/reservations/${reservationId}/patient`, method: 'GET' });
}

export function updateDoctorReservationStatus(reservationId: number, status: DoctorReservationStatusUpdate): Promise<ReservationStatusResponse> {
  return apiClient<ReservationStatusResponse>({ url: `/api/v1/doctors/reservations/${reservationId}/status`, method: 'PATCH', data: { status } });
}

export function getAdminReservations(filters: AdminReservationFilters): Promise<AdminReservationPage> {
  return apiClient<AdminReservationPage>({ url: '/api/v1/admin/reservations', method: 'GET', params: filters });
}
