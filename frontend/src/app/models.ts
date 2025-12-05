export interface UserResponse {
  id: string;
  name: string;
  email: string;
  bio?: string;
  interests?: string[];
  totalPoints: number;
}

export interface MeetingResponse {
  id: string;
  participants: string[];
  date: string;
  location: string;
  interest: string;
  points: number;
  status: string;
  description?: string;
}

export interface EventResponse {
  id: string;
  title: string;
  description?: string;
  eventType: string;
  date: string;
  location: string;
  organizer: string;
  participants: string[];
  maxParticipants?: number;
  interests?: string[];
  status: string;
}

