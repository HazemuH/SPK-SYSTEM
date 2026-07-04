/** Matches the backend `user` object (see BACKEND/docs/04_API_REFERENCE.md). */
export interface User {
  id: string;
  name: string;
  email: string;
  avatar_url: string | null;
  role: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  user: User;
  token: string;
}
