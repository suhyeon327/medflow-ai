import type { DoctorSchedule, PatientReservationPage, ReservationCancelResponse, ReservationCreateRequest, ReservationCreateResponse, ReservationFilters } from '../types/reservation';
import { apiClient } from './apiClient';

export function getAvailableSchedules(doctorId: number): Promise<DoctorSchedule[]> {
  return apiClient<DoctorSchedule[]>({ url: `/api/v1/doctors/${doctorId}/schedules`, method: 'GET' });
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