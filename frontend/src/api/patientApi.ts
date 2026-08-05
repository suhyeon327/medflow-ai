import type { PatientProfile, PatientProfileUpdateRequest } from '../types/patient';
import { apiClient } from './apiClient';

export function getPatientProfile(): Promise<PatientProfile> {
  return apiClient<PatientProfile>({ url: '/api/v1/patients/profile', method: 'GET' });
}

export function updatePatientProfile(request: PatientProfileUpdateRequest): Promise<PatientProfile> {
  return apiClient<PatientProfile>({ url: '/api/v1/patients/profile', method: 'PUT', data: request });
}