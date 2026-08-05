import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { approveDoctor, createDoctorSchedules, getAdminDoctor, getAdminDoctors, getDoctor, getDoctorProfile, getOwnDoctorSchedules, rejectDoctor, updateDoctorProfile } from '../../api/doctorApi';

export const doctorKeys = {
  detail: (doctorId: number) => ['doctors', doctorId] as const,
  profile: ['doctor', 'profile'] as const,
  schedules: (date?: string) => ['doctor', 'schedules', date ?? 'all'] as const,
  adminList: (status?: string) => ['admin', 'doctors', 'list', status ?? 'all'] as const,
  adminDetail: (doctorId: number) => ['admin', 'doctors', doctorId] as const,
};

export function useDoctorQuery(doctorId: number) {
  return useQuery({ queryKey: doctorKeys.detail(doctorId), queryFn: () => getDoctor(doctorId), enabled: Number.isInteger(doctorId) && doctorId > 0 });
}

export function useDoctorProfileQuery() {
  return useQuery({ queryKey: doctorKeys.profile, queryFn: getDoctorProfile });
}

export function useUpdateDoctorProfileMutation() {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: updateDoctorProfile, onSuccess: (profile) => queryClient.setQueryData(doctorKeys.profile, profile) });
}

export function useOwnDoctorSchedulesQuery(date?: string) {
  return useQuery({ queryKey: doctorKeys.schedules(date), queryFn: () => getOwnDoctorSchedules(date) });
}

export function useCreateDoctorSchedulesMutation(date?: string) {
  const queryClient = useQueryClient();
  return useMutation({ mutationFn: createDoctorSchedules, onSuccess: () => queryClient.invalidateQueries({ queryKey: doctorKeys.schedules(date) }) });
}

export function useAdminDoctorsQuery(status?: string) {
  return useQuery({ queryKey: doctorKeys.adminList(status), queryFn: () => getAdminDoctors(status) });
}
export function useAdminDoctorQuery(doctorId: number) {
  return useQuery({ queryKey: doctorKeys.adminDetail(doctorId), queryFn: () => getAdminDoctor(doctorId), enabled: Number.isInteger(doctorId) && doctorId > 0 });
}

export function useDoctorDecisionMutation(doctorId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (decision: 'approve' | 'reject') => decision === 'approve' ? approveDoctor(doctorId) : rejectDoctor(doctorId),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: doctorKeys.adminDetail(doctorId) }),
        queryClient.invalidateQueries({ queryKey: ['admin', 'doctors', 'list'] }),
      ]);
    },
  });
}