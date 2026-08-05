import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createAdminHospital, deleteAdminHospital, getAdminHospitals, updateAdminHospital } from '../../../api/hospitalApi';
import type { AdminHospitalCreateRequest, AdminHospitalUpdateRequest } from '../../../types/hospital';

export const adminHospitalKeys = { all: ['admin', 'hospitals'] as const };

export function useAdminHospitalsQuery() {
  return useQuery({ queryKey: adminHospitalKeys.all, queryFn: getAdminHospitals });
}

export function useCreateAdminHospitalMutation() {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: (request: AdminHospitalCreateRequest) => createAdminHospital(request), onSuccess: () => queryClient.invalidateQueries({ queryKey: adminHospitalKeys.all }) });
}

export function useUpdateAdminHospitalMutation() {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: ({ hospitalId, request }: { hospitalId: number; request: AdminHospitalUpdateRequest }) => updateAdminHospital(hospitalId, request), onSuccess: () => queryClient.invalidateQueries({ queryKey: adminHospitalKeys.all }) });
}

export function useDeleteAdminHospitalMutation() {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: deleteAdminHospital, onSuccess: () => queryClient.invalidateQueries({ queryKey: adminHospitalKeys.all }) });
}
