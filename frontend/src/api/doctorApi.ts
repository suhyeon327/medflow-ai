import type {
  AdminDoctorDecisionResponse,
  AdminDoctorDetail,
  AdminDoctorListItem,
  DoctorProfile,
  DoctorProfileUpdateRequest,
  DoctorSchedule,
  DoctorScheduleCreateRequest,
  PublicDoctor,
} from "../types/doctor";
import { apiClient } from "./apiClient";

export function getDoctor(doctorId: number): Promise<PublicDoctor> {
  return apiClient<PublicDoctor>(
    { url: `/api/v1/doctors/${doctorId}`, method: "GET" },
    false,
  );
}

export function getDoctorProfile(): Promise<DoctorProfile> {
  return apiClient<DoctorProfile>({
    url: "/api/v1/doctors/profile",
    method: "GET",
  });
}

export function updateDoctorProfile(
  request: DoctorProfileUpdateRequest,
): Promise<DoctorProfile> {
  return apiClient<DoctorProfile>({
    url: "/api/v1/doctors/profile",
    method: "PUT",
    data: request,
  });
}

export function getOwnDoctorSchedules(
  date?: string,
): Promise<DoctorSchedule[]> {
  return apiClient<DoctorSchedule[]>({
    url: "/api/v1/doctors/schedules",
    method: "GET",
    params: date ? { date } : undefined,
  });
}

export function createDoctorSchedules(
  request: DoctorScheduleCreateRequest,
): Promise<DoctorSchedule[]> {
  return apiClient<DoctorSchedule[]>({
    url: "/api/v1/doctors/schedules",
    method: "POST",
    data: request,
  });
}

export function getAdminDoctors(
  status?: string,
): Promise<AdminDoctorListItem[]> {
  return apiClient<AdminDoctorListItem[]>({
    url: "/api/v1/admin/doctors",
    method: "GET",
    params: status ? { status } : undefined,
  });
}
export function getAdminDoctor(doctorId: number): Promise<AdminDoctorDetail> {
  return apiClient<AdminDoctorDetail>({
    url: `/api/v1/admin/doctors/${doctorId}`,
    method: "GET",
  });
}

export function approveDoctor(
  doctorId: number,
): Promise<AdminDoctorDecisionResponse> {
  return apiClient<AdminDoctorDecisionResponse>({
    url: `/api/v1/admin/doctors/${doctorId}/approve`,
    method: "PATCH",
  });
}

export function rejectDoctor(
  doctorId: number,
): Promise<AdminDoctorDecisionResponse> {
  return apiClient<AdminDoctorDecisionResponse>({
    url: `/api/v1/admin/doctors/${doctorId}/reject`,
    method: "PATCH",
  });
}
