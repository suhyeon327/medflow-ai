import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import {
  getHospital,
  getHospitalDoctors,
  getHospitals,
} from "../../api/hospitalApi";

export const hospitalKeys = {
  all: ["hospitals"] as const,
  list: (keyword: string, page: number, size: number) =>
    ["hospitals", "list", keyword, page, size] as const,
  options: ["hospitals", "options"] as const,
  detail: (hospitalId: number) => ["hospitals", hospitalId] as const,
  doctors: (hospitalId: number) =>
    ["hospitals", hospitalId, "doctors"] as const,
};

export function useHospitalsQuery(keyword: string, page = 0, size = 20) {
  return useQuery({
    queryKey: hospitalKeys.list(keyword, page, size),
    queryFn: () => getHospitals(keyword || undefined, page, size),
  });
}

export function useHospitalOptionsQuery() {
  return useInfiniteQuery({
    queryKey: hospitalKeys.options,
    queryFn: ({ pageParam }) => getHospitals(undefined, pageParam, 20),
    initialPageParam: 0,
    getNextPageParam: (lastPage) =>
      lastPage.last ? undefined : lastPage.page + 1,
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
