export interface User {
  _id: string;
  name: string;
  email: string;
  bio: string;
  interests: string[];
  totalPoints: number;
  totalMeetings: number;
}
