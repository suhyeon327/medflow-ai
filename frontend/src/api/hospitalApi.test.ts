import { beforeEach, describe, expect, it, vi } from "vitest";
import type { AdminHospitalUpdateRequest } from "../types/hospital";
import { apiClient } from "./apiClient";
import { deleteAdminHospital, updateAdminHospital } from "./hospitalApi";

vi.mock("./apiClient", () => ({
  apiClient: vi.fn(),
}));

const mockedApiClient = vi.mocked(apiClient);

describe("관리자 병원 API", () => {
  beforeEach(() => {
    mockedApiClient.mockReset();
  });

  it("병원 수정 요청 URL에 병원 ID를 경로로 포함한다", () => {
    const request = {} as AdminHospitalUpdateRequest;

    updateAdminHospital(1, request);

    expect(mockedApiClient).toHaveBeenCalledWith({
      url: "/api/v1/admin/hospitals/1",
      method: "PUT",
      data: request,
    });
  });

  it("병원 삭제 요청 URL에 병원 ID를 경로로 포함한다", () => {
    deleteAdminHospital(1);

    expect(mockedApiClient).toHaveBeenCalledWith({
      url: "/api/v1/admin/hospitals/1",
      method: "DELETE",
    });
  });
});
