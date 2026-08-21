import type { PublicDoctor } from "../types/doctor";
import type {
  AdminHospital,
  AdminHospitalCreateRequest,
  AdminHospitalDeleteResponse,
  AdminHospitalUpdateRequest,
  HospitalDetail,
  HospitalPage,
  HospitalSummary,
} from "../types/hospital";
import { apiClient } from "./apiClient";

// 병원 목록 조회
export function getHospitals(
  keyword?: string,
  page = 0
): Promise<HospitalPage> {
  return apiClient<HospitalPage>(
    {
      url: "/api/v1/hospitals",
      method: "GET",
      params: { keyword: keyword || undefined, page }
    },
    false
  );
}

// 병원 상세 정보 조회
export function getHospital(
  hospitalId: number
): Promise<HospitalDetail> {
  return apiClient<HospitalDetail>(
    { 
      url: `/api/v1/hospitals/${hospitalId}`, 
      method: "GET"
    },
    false
  );
}

// 병원별 의사 목록 조회
export function getHospitalDoctors(
  hospitalId: number
): Promise<PublicDoctor[]> {
  return apiClient<PublicDoctor[]>(
    { 
      url: `/api/v1/hospitals/${hospitalId}/doctors`, 
      method: "GET"
    },
    false
  );
}

// 전체 병원 및 의료진 통계 조회
export async function getHospitalSummary() : Promise<HospitalSummary> {
  return apiClient<HospitalSummary>(
    {
      url: "/api/v1/hospitals/summary",
      method: "GET"
    },
    false
  );
}

// 병원 등록
export function createAdminHospital(
  request: AdminHospitalCreateRequest
): Promise<HospitalDetail> {
  return apiClient<HospitalDetail>(
    {
      url: "/api/v1/admin/hospitals",
      method: "POST",
      data: request
    }
  );
}

// 병원 관리 목록 조회
export function getAdminHospitals(): Promise<AdminHospital[]> {
  return apiClient<AdminHospital[]>(
    {
      url: "/api/v1/admin/hospitals",
      method: "GET"
    }
  );
}

// 병원 정보 수정
export function updateAdminHospital(
  hospitalId: number,
  request: AdminHospitalUpdateRequest
): Promise<AdminHospital> {
  return apiClient<AdminHospital>(
    {
      url: `/api/v1/admin/hospitals${hospitalId}`,
      method: "PUT",
      data: request
    }
  );
}

// 병원 삭제
export function deleteAdminHospital(
  hospitalId: number,
): Promise<AdminHospitalDeleteResponse> {
  return apiClient<AdminHospitalDeleteResponse>(
    {
      url: `/api/v1/admin/hospitals${hospitalId}`,
      method: "DELETE"
    }
  );
}