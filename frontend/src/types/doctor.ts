export interface PublicDoctor {
  doctorId: number;
  doctorName: string;
  hospitalId: number;
  hospitalName: string;
  specialty: string | null;
  introduction: string | null;
  contact: string;
}