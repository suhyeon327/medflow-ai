import type { PublicDoctor } from "../types/doctor";
import type {
  AdminHospital,
  AdminHospitalCreateRequest,
  AdminHospitalDeleteResponse,
  AdminHospitalUpdateRequest,
  HospitalDetail,
  HospitalPage,
} from "../types/hospital";
import { apiClient } from "./apiClient";

export function getHospitals(
  keyword?: string,
  page = 0,
  size = 20,
): Promise<HospitalPage> {
  return apiClient<HospitalPage>(
    {
      url: "/api/v1/hospitals",
      method: "GET",
      params: { keyword: keyword || undefined, page, size },
    },
    false,
  );
}

export function getHospital(hospitalId: number): Promise<HospitalDetail> {
  return apiClient<HospitalDetail>(
    { url: `/api/v1/hospitals/${hospitalId}`, method: "GET" },
    false,
  );
}

export function getHospitalDoctors(
  hospitalId: number,
): Promise<PublicDoctor[]> {
  return apiClient<PublicDoctor[]>(
    { url: `/api/v1/hospitals/${hospitalId}/doctors`, method: "GET" },
    false,
  );
}

export function getAdminHospitals(): Promise<AdminHospital[]> {
  return apiClient<AdminHospital[]>({
    url: "/api/v1/admin/hospitals/",
    method: "GET",
  });
}

export function createAdminHospital(
  request: AdminHospitalCreateRequest,
): Promise<HospitalDetail> {
  return apiClient<HospitalDetail>({
    url: "/api/v1/admin/hospitals",
    method: "POST",
    data: request,
  });
}

export function updateAdminHospital(
  hospitalId: number,
  request: AdminHospitalUpdateRequest,
): Promise<AdminHospital> {
  return apiClient<AdminHospital>({
    url: `/api/v1/admin/hospitals/${hospitalId}`,
    method: "PUT",
    data: request,
  });
}

export function deleteAdminHospital(
  hospitalId: number,
): Promise<AdminHospitalDeleteResponse> {
  return apiClient<AdminHospitalDeleteResponse>({
    url: `/api/v1/admin/hospitals/${hospitalId}`,
    method: "DELETE",
  });
}
