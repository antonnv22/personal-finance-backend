export interface JwtPayload {
  sub: string;
  userId: string;
  iat: number;
  exp: number;
}

/**
 * Decodes the JWT payload client-side without verifying the signature.
 * Verification happens on the backend; this is only used to read claims
 * (email, userId, expiry) for the UI.
 */
export function decodeJwt(token: string): JwtPayload | null {
  try {
    const payload = token.split(".")[1];
    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(normalized)
        .split("")
        .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join("")
    );
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

export function isTokenExpired(payload: JwtPayload): boolean {
  return payload.exp * 1000 <= Date.now();
}
