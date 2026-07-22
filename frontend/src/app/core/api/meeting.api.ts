import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateMeetingRequest, Meeting, Completion } from '../models/meeting.model';

@Injectable({ providedIn: 'root' })
export class MeetingApi {
  private readonly baseUrl = 'http://localhost:8080/api/meetings';

  constructor(private http: HttpClient) {}

  createMeeting(payload: CreateMeetingRequest): Observable<Meeting> {
    return this.http.post<Meeting>(this.baseUrl, payload);
  }

  getAll(status?: Completion): Observable<Meeting[]> {
    const url = status ? `${this.baseUrl}?status=${status}` : this.baseUrl;
    return this.http.get<Meeting[]>(url);
  }

  getById(id: string): Observable<Meeting> {
    return this.http.get<Meeting>(`${this.baseUrl}/${id}`);
  }

  getByUser(userId: string, status?: Completion): Observable<Meeting[]> {
    const url = status
      ? `${this.baseUrl}/user/${userId}?status=${status}`
      : `${this.baseUrl}/user/${userId}`;
    return this.http.get<Meeting[]>(url);
  }

  join(meetingId: string, userId: string): Observable<Meeting> {
    return this.http.post<Meeting>(`${this.baseUrl}/${meetingId}/join?userId=${userId}`, {});
  }

  leave(meetingId: string, userId: string): Observable<Meeting> {
    return this.http.post<Meeting>(`${this.baseUrl}/${meetingId}/leave?userId=${userId}`, {});
  }

  complete(meetingId: string): Observable<Meeting> {
    return this.http.post<Meeting>(`${this.baseUrl}/${meetingId}/complete`, {});
  }

  cancel(meetingId: string): Observable<Meeting> {
    return this.http.post<Meeting>(`${this.baseUrl}/${meetingId}/cancel`, {});
  }

  searchByInterest(interest: string): Observable<Meeting[]> {
    return this.http.get<Meeting[]>(`${this.baseUrl}/search?interest=${encodeURIComponent(interest)}`);
  }
}
