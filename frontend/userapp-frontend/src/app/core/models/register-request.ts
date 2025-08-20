export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  fullName: string;
  roles?: string[];
}