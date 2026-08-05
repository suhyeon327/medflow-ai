export type ReservationStatus = 'REQUESTED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
export type ReservationPeriod = 'UPCOMING' | 'TODAY' | 'PAST';

export interface DoctorSchedule {
  scheduleId: number;
  date: string;
  startTime: string;
  endTime: string;
}

export interface ReservationCreateRequest { scheduleId: number; }
export interface ReservationCreateResponse { reservationId: number; status: ReservationStatus; }
export interface ReservationCancelResponse { reservationId: number; status: ReservationStatus; }

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