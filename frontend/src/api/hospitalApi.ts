import type { PublicDoctor } from '../types/doctor';
import type { HospitalDetail, HospitalListItem } from '../types/hospital';
import { apiClient } from './apiClient';

export function getHospitals(keyword?: string): Promise<HospitalListItem[]> {
  return apiClient<HospitalListItem[]>({
    url: '/api/v1/hospitals',
    method: 'GET',
    params: keyword ? { keyword } : undefined,
  }, false);
}

export function getHospital(hospitalId: number): Promise<HospitalDetail> {
  return apiClient<HospitalDetail>({ url: `/api/v1/hospitals/${hospitalId}`, method: 'GET' }, false);
}

export function getHospitalDoctors(hospitalId: number): Promise<PublicDoctor[]> {
  return apiClient<PublicDoctor[]>({ url: `/api/v1/hospitals/${hospitalId}/doctors`, method: 'GET' }, false);
}