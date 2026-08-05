export type DoctorStatus = 'PENDING' | 'ACTIVE' | 'REJECTED';

export interface PublicDoctor {
  doctorId: number;
  doctorName: string;
  hospitalId: number;
  hospitalName: string;
  specialty: string | null;
  introduction: string | null;
  contact: string;
}

export interface DoctorProfile extends PublicDoctor {
  licenseNumber: string;
  status: DoctorStatus;
}

export interface DoctorProfileUpdateRequest {
  hospitalId: number;
  name: string;
  licenseNumber: string;
  specialty: string;
  introduction: string;
  contact: string;
}

export interface DoctorSchedule {
  scheduleId: number;
  date: string;
  startTime: string;
  endTime: string;
}

export interface DoctorScheduleCreateRequest {
  date: string;
  startTime: string;
  endTime: string;
  slotMinutes: number;
}

export interface AdminDoctorDetail extends DoctorProfile {
  email: string;
}

export interface AdminDoctorDecisionResponse {
  doctorId: number;
  message: string;
}
export interface AdminDoctorListItem {
  doctorId: number;
  doctorName: string;
  hospitalName: string;
  licenseNumber: string;
  status: DoctorStatus;
}
