export interface User {
  id: string;
  username: string;
  email: string;
  role: "user" | "admin";
  status: "active" | "disabled";
  createdAt: string;
}

export const mockUsers: User[] = [
  { id: "1", username: "student01", email: "student01@univ.edu", role: "user", status: "active", createdAt: "2024-01-15" },
  { id: "2", username: "student02", email: "student02@univ.edu", role: "user", status: "active", createdAt: "2024-02-20" },
  { id: "3", username: "prof_zhang", email: "zhang@univ.edu", role: "user", status: "active", createdAt: "2023-12-01" },
  { id: "4", username: "admin", email: "admin@univ.edu", role: "admin", status: "active", createdAt: "2023-01-01" },
  { id: "5", username: "student05", email: "student05@univ.edu", role: "user", status: "disabled", createdAt: "2024-03-10" },
];

export const mockCurrentUser: User = {
  id: "1",
  username: "student01",
  email: "student01@univ.edu",
  role: "user",
  status: "active",
  createdAt: "2024-01-15",
};
