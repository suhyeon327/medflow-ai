import { useQuery } from "@tanstack/react-query";
import { getAdminUser, getAdminUsers } from "../../../api/adminUserApi";
import type { AdminUserFilters } from "../../../types/adminUser";

export const adminUserKeys = {
  all: ["admin", "users"] as const,
  list: (filters: AdminUserFilters) =>
    ["admin", "users", "list", filters] as const,
  detail: (userId: number) => ["admin", "users", userId] as const,
};

export function useAdminUsersQuery(filters: AdminUserFilters) {
  return useQuery({
    queryKey: adminUserKeys.list(filters),
    queryFn: () => getAdminUsers(filters),
  });
}

export function useAdminUserQuery(userId: number) {
  return useQuery({
    queryKey: adminUserKeys.detail(userId),
    queryFn: () => getAdminUser(userId),
    enabled: Number.isInteger(userId) && userId > 0,
  });
}
