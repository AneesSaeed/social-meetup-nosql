export type Completion = 'COMPLETED' | 'CANCELLED' | 'UPCOMING';

export interface Meeting {
  id: string;

  title: string;
  description: string;
  eventType: string;

  date: string;            // LocalDateTime -> ISO string
  location: string;

  organizer: string;       // user id
  participants: string[];

  maxParticipants: number;
  interests: string[];

  status: Completion;
  points: number;          // 0 for upcoming, >0 when completed
  createdAt: string;
}

export interface CreateMeetingRequest {
  title: string;
  description: string;
  eventType: string;

  date: string;
  location: string;

  organizer: string;
  maxParticipants: number;
  interests: string[];
  participants?: string[];
}
