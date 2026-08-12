export interface HospitalListItem {
  id: number;
  name: string;
  region: string;
  address: string;
  tel: string;
  doctorCount: number;
  specialties: string[];
}

export interface HospitalDetail {
  id: number;
  name: string;
  address: string;
  region: string;
  tel: string;
}

export type HospitalStatus = "ACTIVE" | "CLOSED";

export interface AdminHospital extends HospitalDetail {
  status: HospitalStatus;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
}

export interface AdminHospitalCreateRequest {
  name: string;
  address: string;
  region: string;
  tel: string;
}

export interface AdminHospitalUpdateRequest extends AdminHospitalCreateRequest {
  status: HospitalStatus;
}

export interface AdminHospitalDeleteResponse {
  hospitalId: number;
  deleteAt: string;
  message: string;
}
