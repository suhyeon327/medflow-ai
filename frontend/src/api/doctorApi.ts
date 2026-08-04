import type { PublicDoctor } from '../types/doctor';
import { apiClient } from './apiClient';

export function getDoctor(doctorId: number): Promise<PublicDoctor> {
  return apiClient<PublicDoctor>({ url: `/api/v1/doctors/${doctorId}`, method: 'GET' }, false);
}