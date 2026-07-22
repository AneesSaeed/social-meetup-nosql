export interface UserSearchDocument {
  userId: string;
  name: string;
  email: string;
  bio?: string;
  interests?: string[];
  totalScore?: number;
  lastActive?: string; // LocalDateTime serialized as ISO string
}
