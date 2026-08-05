import type { AdminUser, AdminUserFilters, AdminUserPage } from '../types/adminUser';
import { apiClient } from './apiClient';

export function getAdminUsers(filters: AdminUserFilters): Promise<AdminUserPage> {
  return apiClient<AdminUserPage>({
    url: '/api/v1/admin/users',
    method: 'GET',
    params: filters,
  });
}

export function getAdminUser(userId: number): Promise<AdminUser> {
  return apiClient<AdminUser>({
    url: `/api/v1/admin/users/${userId}`,
    method: 'GET',
  });
}