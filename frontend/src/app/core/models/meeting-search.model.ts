export interface MeetingSearchDocument {
  meetingId: string;
  title?: string;
  eventType?: string;
  date?: string;           // ISO
  location?: string;
  organizer?: string;
  participants?: string[];
  maxParticipants?: number;
  interests?: string[];
  points?: number;
  status?: string;         // lowercased in ES
  createdAt?: string;      // ISO
}
