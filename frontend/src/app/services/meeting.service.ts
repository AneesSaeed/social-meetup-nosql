import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { MeetingResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class MeetingService {
  private apiUrl = 'http://localhost:8080/api/meetings';

  constructor(private http: HttpClient) {}

  create(payload: {
    participants: string[];
    interest: string;
    location: string;
    points: number;
    description?: string;
  }): Observable<MeetingResponse> {
    return this.http.post<MeetingResponse>(this.apiUrl, payload);
  }

  getForUser(userId: string): Observable<MeetingResponse[]> {
    return this.http.get<MeetingResponse[]>(`${this.apiUrl}/user/${userId}`);
  }

  getScore(userId: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/score/${userId}`);
  }
}

