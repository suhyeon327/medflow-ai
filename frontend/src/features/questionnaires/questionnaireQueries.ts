import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  createQuestionnaire,
  getDoctorQuestionnaireAnalysis,
  getQuestionnaire,
  getQuestionnaireAnalysis,
  updateQuestionnaire,
} from "../../api/questionnaireApi";
import type {
  QuestionnaireCreateRequest,
  QuestionnaireUpdateRequest,
} from "../../types/questionnaire";

export const questionnaireKeys = {
  all: ["questionnaires"] as const,
  reservation: (reservationId: number) =>
    ["questionnaires", "reservation", reservationId] as const,
  analysis: (questionnaireId: number) =>
    ["questionnaires", questionnaireId, "analysis"] as const,
  doctorAnalysis: (questionnaireId: number) =>
    ["doctor", "questionnaires", questionnaireId, "analysis"] as const,
};

export function useQuestionnaireQuery(reservationId: number) {
  return useQuery({
    queryKey: questionnaireKeys.reservation(reservationId),
    queryFn: () => getQuestionnaire(reservationId),
    retry: false,
    enabled: Number.isInteger(reservationId) && reservationId > 0,
  });
}

export function useQuestionnaireAnalysisQuery(questionnaireId: number | null) {
  return useQuery({
    queryKey: questionnaireKeys.analysis(questionnaireId ?? 0),
    queryFn: () => getQuestionnaireAnalysis(questionnaireId as number),
    enabled: questionnaireId !== null,
    refetchInterval: (query) =>
      query.state.data?.status === "PENDING" ||
      query.state.data?.status === "PROCESSING"
        ? 3000
        : false,
  });
}

export function useDoctorQuestionnaireAnalysisQuery(questionnaireId: number) {
  return useQuery({
    queryKey: questionnaireKeys.doctorAnalysis(questionnaireId),
    queryFn: () => getDoctorQuestionnaireAnalysis(questionnaireId),
    enabled: Number.isInteger(questionnaireId) && questionnaireId > 0,
    retry: false,
  });
}

export function useCreateQuestionnaireMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: QuestionnaireCreateRequest) =>
      createQuestionnaire(request),
    onSuccess: (data) =>
      queryClient.setQueryData(
        questionnaireKeys.reservation(data.reservationId),
        data,
      ),
  });
}

export function useUpdateQuestionnaireMutation(reservationId: number) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      questionnaireId,
      request,
    }: {
      questionnaireId: number;
      request: QuestionnaireUpdateRequest;
    }) => updateQuestionnaire(questionnaireId, request),
    onSuccess: (data) =>
      queryClient.setQueryData(
        questionnaireKeys.reservation(reservationId),
        data,
      ),
  });
}
