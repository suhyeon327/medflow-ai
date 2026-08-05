export type Gender = 'MALE' | 'FEMALE';

export interface PatientProfile {
  id: number;
  name: string;
  birth: string;
  gender: Gender;
  phone: string;
}

export type PatientProfileUpdateRequest = Omit<PatientProfile, 'id'>;
