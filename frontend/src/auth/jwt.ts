import type { AuthUser, UserRole } from '../types/auth';

interface AccessTokenPayload { sub?: string; auth?: string; exp?: number; }
const ROLES: UserRole[] = ['PATIENT', 'DOCTOR', 'ADMIN'];

function decodePayload(token: string): AccessTokenPayload | null {
  try {
    const encodedPayload = token.split('.')[1];
    const base64 = encodedPayload.replace(/-/g, '+').replace(/_/g, '/');
    const decodedPayload = decodeURIComponent(
      atob(base64).split('').map((character) =>
        `%${character.charCodeAt(0).toString(16).padStart(2, '0')}`,
      ).join(''),
    );
    return JSON.parse(decodedPayload) as AccessTokenPayload;
  } catch {
    return null;
  }
}

export function getUserFromAccessToken(accessToken: string): AuthUser | null {
  const payload = decodePayload(accessToken);
  const role = payload?.auth?.split(',')
    .map((authority) => authority.replace(/^ROLE_/, ''))
    .find((authority): authority is UserRole => ROLES.includes(authority as UserRole));

  return payload?.sub && role ? { email: payload.sub, role } : null;
}

export function isAccessTokenExpired(accessToken: string): boolean {
  const expiration = decodePayload(accessToken)?.exp;
  return !expiration || expiration * 1000 <= Date.now();
}