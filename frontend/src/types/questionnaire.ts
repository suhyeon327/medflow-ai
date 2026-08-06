export type QuestionnaireAnalysisStatus =
  | "PENDING"
  | "PROCESSING"
  | "COMPLETED"
  | "FAILED";
export type QuestionnairePriority = "NORMAL" | "CAUTION" | "HIGH_PRIORITY";

export interface QuestionnaireFormData {
  chiefComplaint: string;
  symptomStartedAt: string;
  symptomDescription: string;
  painLevel: number | null;
  temperature: number | null;
  associatedSymptoms: string;
  medicalHistory: string;
  medications: string;
  allergies: string;
  additionalNote: string;
}

export interface QuestionnaireCreateRequest extends QuestionnaireFormData {
  reservationId: number;
}
export type QuestionnaireUpdateRequest = QuestionnaireFormData;

export interface QuestionnaireDetail extends QuestionnaireFormData {
  questionnaireId: number;
  reservationId: number;
  patientId: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface QuestionnaireAnalysis {
  analysisId: number;
  questionnaireId: number;
  reservationId?: number;
  summary: string;
  keyFindings: string[];
  riskSignals: string[];
  doctorCheckpoints: string[];
  priorityLevel: QuestionnairePriority;
  status: QuestionnaireAnalysisStatus;
}
