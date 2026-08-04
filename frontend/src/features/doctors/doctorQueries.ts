import { useQuery } from '@tanstack/react-query';
import { getDoctor } from '../../api/doctorApi';

export const doctorKeys = {
  detail: (doctorId: number) => ['doctors', doctorId] as const,
};

export function useDoctorQuery(doctorId: number) {
  return useQuery({
    queryKey: doctorKeys.detail(doctorId),
    queryFn: () => getDoctor(doctorId),
    enabled: Number.isInteger(doctorId) && doctorId > 0,
  });
}