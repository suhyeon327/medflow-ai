import type { UserRole } from './auth';

export type UserStatus = 'ACTIVE' | 'LOCKED' | 'WITHDRAWN';

export interface AdminUser {
  userId: number;
  email: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  updatedAt: string | null;
  deletedAt: string | null;
}

export interface AdminUserPage {
  content: AdminUser[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AdminUserFilters {
  role?: UserRole;
  status?: UserStatus;
  page: number;
  size: number;
}