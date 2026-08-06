import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getPatientProfile, updatePatientProfile } from "../../api/patientApi";

export const patientProfileKey = ["patient", "profile"] as const;

export function usePatientProfileQuery() {
  return useQuery({ queryKey: patientProfileKey, queryFn: getPatientProfile });
}

export function useUpdatePatientProfileMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updatePatientProfile,
    onSuccess: (profile) =>
      queryClient.setQueryData(patientProfileKey, profile),
  });
}
