export type ReservationStatus =
  | "PENDING"
  | "APPROVED"
  | "REJECTED"
  | "COMPLETED"
  | "CANCELLED";
export type ReservationPeriod = "UPCOMING" | "TODAY" | "PAST";

export interface DoctorSchedule {
  scheduleId: number;
  date: string;
  startTime: string;
  endTime: string;
}

export interface ReservationCreateRequest {
  scheduleId: number;
}
export interface ReservationCreateResponse {
  reservationId: number;
  status: ReservationStatus;
}
export interface ReservationCancelResponse {
  reservationId: number;
  status: ReservationStatus;
}

export interface PatientReservation {
  reservationId: number;
  hospitalId: number;
  hospitalName: string;
  doctorId: number;
  doctorName: string;
  reservationDate: string;
  startTime: string;
  endTime: string;
  reservationStatus: ReservationStatus;
}

export interface PatientReservationPage {
  content: PatientReservation[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ReservationFilters {
  status?: ReservationStatus;
  date?: string;
  hospitalId?: number;
  doctorId?: number;
  period?: ReservationPeriod;
  page: number;
  size: number;
}

export interface DoctorReservation {
  reservationId: number;
  patientName: string;
  reservationDate: string;
  startTime: string;
  endTime: string;
  reservationStatus: ReservationStatus;
  questionnaireId: number | null;
}

export interface DoctorReservationPage {
  content: DoctorReservation[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface DoctorReservationFilters {
  date?: string;
  status?: ReservationStatus;
  page: number;
  size: number;
}

export interface DoctorReservationPatient {
  patientId: number;
  patientName: string;
  gender: "MALE" | "FEMALE";
  birthDate: string;
  phoneNumber: string;
  reservationId: number;
  reservationDate: string;
  startTime: string;
  endTime: string;
  reservationStatus: ReservationStatus;
}

export interface ReservationStatusResponse {
  reservationId: number;
  status: ReservationStatus;
}

export type DoctorReservationStatusUpdate =
  | "APPROVED"
  | "REJECTED"
  | "COMPLETED";

export interface AdminReservation extends DoctorReservation {
  hospitalId: number;
  hospitalName: string;
  doctorId: number;
  doctorName: string;
  patientId: number;
  createdAt: string;
}

export interface AdminReservationPage {
  content: AdminReservation[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AdminReservationFilters {
  hospitalId?: number;
  doctorId?: number;
  patientId?: number;
  date?: string;
  status?: ReservationStatus;
  page: number;
  size: number;
}
