export interface AuthResponse {
  token: string;
  type?: string; // si ton backend envoie "Bearer"
  username?: string;
}
