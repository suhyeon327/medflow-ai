import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import {
  getHospital,
  getHospitalDoctors,
  getHospitals,
  getHospitalSummary
} from "../../api/hospitalApi";

// 병원 queryKey
export const hospitalKeys = {
  all: ["hospitals"] as const,   // 병원과 관련된 모든 Query의 가장 상위 Key
  
  list: (keyword: string, page: number) =>   // 병원 목록 Key
    ["hospitals", "list", keyword, page] as const,
  
  options: ["hospitals", "options"] as const,   // 병원 옵션 Key
  
  detail: (hospitalId: number) =>   // 병원 상세 Key
    ["hospitals", hospitalId] as const,
  
  doctors: (hospitalId: number) =>   // 병원별 의사 Key
    ["hospitals", hospitalId, "doctors"] as const,
};

// 병원 목록 조회
export function useHospitalsQuery(keyword: string, page = 0) {
  return useQuery({
    queryKey: hospitalKeys.list(keyword, page),
    queryFn: () => getHospitals(keyword || undefined, page)
  });
}

// 병원 전체 목록을 여러 페이지에 걸쳐 계속 불러오기
export function useHospitalOptionsQuery() {
  return useInfiniteQuery({
    queryKey: hospitalKeys.options,
    queryFn: ({ pageParam }) => getHospitals(undefined, pageParam),
    initialPageParam: 0,
    getNextPageParam: (lastPage) =>
      lastPage.last ? undefined : lastPage.page + 1,
  });
}

// 특정 병원 하나의 상세 정보를 조회
export function useHospitalQuery(hospitalId: number) {
  return useQuery({
    queryKey: hospitalKeys.detail(hospitalId),
    queryFn: () => getHospital(hospitalId),
    enabled: Number.isInteger(hospitalId) && hospitalId > 0,
  });
}

// 특정 병원의 의사 목록을 조회
export function useHospitalDoctorsQuery(hospitalId: number) {
  return useQuery({
    queryKey: hospitalKeys.doctors(hospitalId),
    queryFn: () => getHospitalDoctors(hospitalId),
    enabled: Number.isInteger(hospitalId) && hospitalId > 0,
  });
}

// 전체 병원 및 의료진 통계 조회
export function useHospitalSummaryQuery() {
  return useQuery({
    queryKey: ["hospitals", "summary"],
    queryFn: getHospitalSummary
  });
}