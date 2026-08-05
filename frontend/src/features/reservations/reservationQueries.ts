import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { cancelReservation, createReservation, getAdminReservations, getAvailableSchedules, getDoctorReservationPatient, getDoctorReservations, getPatientReservations, updateDoctorReservationStatus } from '../../api/reservationApi';
import type { AdminReservationFilters, DoctorReservationFilters, DoctorReservationStatusUpdate, ReservationFilters } from '../../types/reservation';

export const reservationKeys = {
  all: ['reservations'] as const,
  patient: (filters: ReservationFilters) => ['reservations', 'patient', filters] as const,
  schedules: (doctorId: number) => ['doctors', doctorId, 'schedules'] as const,
  doctor: (filters: DoctorReservationFilters) => ['reservations', 'doctor', filters] as const,
  patientDetail: (reservationId: number) => ['reservations', reservationId, 'patient'] as const,
  admin: (filters: AdminReservationFilters) => ['reservations', 'admin', filters] as const,
};

export function useAvailableSchedulesQuery(doctorId: number) {
  return useQuery({
    queryKey: reservationKeys.schedules(doctorId),
    queryFn: () => getAvailableSchedules(doctorId),
    enabled: Number.isInteger(doctorId) && doctorId > 0,
  });
}

export function useDoctorReservationsQuery(filters: DoctorReservationFilters) {
  return useQuery({ queryKey: reservationKeys.doctor(filters), queryFn: () => getDoctorReservations(filters) });
}

export function useDoctorReservationPatientQuery(reservationId: number | null) {
  return useQuery({ queryKey: reservationKeys.patientDetail(reservationId ?? 0), queryFn: () => getDoctorReservationPatient(reservationId as number), enabled: reservationId !== null });
}

export function useUpdateDoctorReservationStatusMutation() {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: ({ reservationId, status }: { reservationId: number; status: DoctorReservationStatusUpdate }) => updateDoctorReservationStatus(reservationId, status), onSuccess: () => queryClient.invalidateQueries({ queryKey: reservationKeys.all }) });
}

export function useAdminReservationsQuery(filters: AdminReservationFilters) {
  return useQuery({ queryKey: reservationKeys.admin(filters), queryFn: () => getAdminReservations(filters) });
}

export function useCreateReservationMutation(doctorId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createReservation,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: reservationKeys.schedules(doctorId) }),
        queryClient.invalidateQueries({ queryKey: reservationKeys.all }),
      ]);
    },
  });
}

export function usePatientReservationsQuery(filters: ReservationFilters) {
  return useQuery({
    queryKey: reservationKeys.patient(filters),
    queryFn: () => getPatientReservations(filters),
  });
}

export function useCancelReservationMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ reservationId }: { reservationId: number; doctorId: number }) => cancelReservation(reservationId),
    onSuccess: async (_, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: reservationKeys.all }),
        queryClient.invalidateQueries({ queryKey: reservationKeys.schedules(variables.doctorId) }),
      ]);
    },
  });
}
