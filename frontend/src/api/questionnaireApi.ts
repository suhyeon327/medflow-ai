import type { QuestionnaireAnalysis, QuestionnaireCreateRequest, QuestionnaireDetail, QuestionnaireUpdateRequest } from '../types/questionnaire';
import { apiClient } from './apiClient';

export function getQuestionnaire(reservationId: number): Promise<QuestionnaireDetail> {
  return apiClient<QuestionnaireDetail>({ url: `/api/v1/questionnaires/${reservationId}/questionnaire`, method: 'GET' });
}

export function createQuestionnaire(request: QuestionnaireCreateRequest): Promise<QuestionnaireDetail> {
  return apiClient<QuestionnaireDetail>({ url: '/api/v1/questionnaires', method: 'POST', data: request });
}

export function updateQuestionnaire(questionnaireId: number, request: QuestionnaireUpdateRequest): Promise<QuestionnaireDetail> {
  return apiClient<QuestionnaireDetail>({ url: `/api/v1/questionnaires/${questionnaireId}`, method: 'PUT', data: request });
}

export function getQuestionnaireAnalysis(questionnaireId: number): Promise<QuestionnaireAnalysis> {
  return apiClient<QuestionnaireAnalysis>({ url: `/api/v1/questionnaires/${questionnaireId}/analysis`, method: 'GET' });
}

export function getDoctorQuestionnaireAnalysis(questionnaireId: number): Promise<QuestionnaireAnalysis> {
  return apiClient<QuestionnaireAnalysis>({ url: `/api/v1/doctors/questionnaires/${questionnaireId}/analysis`, method: 'GET' });
}
