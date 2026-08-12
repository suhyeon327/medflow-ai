import { useQuery } from "@tanstack/react-query";
import {
  getHospital,
  getHospitalDoctors,
  getHospitals,
} from "../../api/hospitalApi";

export const hospitalKeys = {
  all: ["hospitals"] as const,
  list: (keyword: string) => ["hospitals", "list", keyword] as const,
  detail: (hospitalId: number) => ["hospitals", hospitalId] as const,
  doctors: (hospitalId: number) =>
    ["hospitals", hospitalId, "doctors"] as const,
};

export function useHospitalsQuery(keyword: string) {
  return useQuery({
    queryKey: hospitalKeys.list(keyword),
    queryFn: () => getHospitals(keyword || undefined),
  });
}

export function useHospitalQuery(hospitalId: number) {
  return useQuery({
    queryKey: hospitalKeys.detail(hospitalId),
    queryFn: () => getHospital(hospitalId),
    enabled: Number.isInteger(hospitalId) && hospitalId > 0,
  });
}

export function useHospitalDoctorsQuery(hospitalId: number) {
  return useQuery({
    queryKey: hospitalKeys.doctors(hospitalId),
    queryFn: () => getHospitalDoctors(hospitalId),
    enabled: Number.isInteger(hospitalId) && hospitalId > 0,
  });
}
