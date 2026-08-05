import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { cancelReservation, createReservation, getAvailableSchedules, getPatientReservations } from '../../api/reservationApi';
import type { ReservationFilters } from '../../types/reservation';

export const reservationKeys = {
  all: ['reservations'] as const,
  patient: (filters: ReservationFilters) => ['reservations', 'patient', filters] as const,
  schedules: (doctorId: number) => ['doctors', doctorId, 'schedules'] as const,
};

export function useAvailableSchedulesQuery(doctorId: number) {
  return useQuery({
    queryKey: reservationKeys.schedules(doctorId),
    queryFn: () => getAvailableSchedules(doctorId),
    enabled: Number.isInteger(doctorId) && doctorId > 0,
  });
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