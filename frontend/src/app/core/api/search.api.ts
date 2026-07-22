import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserSearchDocument } from '../models/user-search.model';
import { MeetingSearchDocument } from '../models/meeting-search.model';

@Injectable({ providedIn: 'root' })
export class SearchApi {
  private readonly baseUrl = 'http://localhost:8080/api/search';

  constructor(private http: HttpClient) {}

  searchUsers(query: string): Observable<UserSearchDocument[]> {
    const params = new HttpParams().set('query', query.trim());
    return this.http.get<UserSearchDocument[]>(`${this.baseUrl}/users`, { params });
  }

  searchByInterestsAll(interests: string[]): Observable<UserSearchDocument[]> {
    let params = new HttpParams();
    interests
      .map(i => i.trim())
      .filter(Boolean)
      .forEach(i => (params = params.append('interests', i)));

    return this.http.get<UserSearchDocument[]>(`${this.baseUrl}/users/by-interests-all`, { params });
  }

  // Meetings fuzzy search filtered by status (UPCOMING/COMPLETED)
  searchMeetingsByStatus(status: 'UPCOMING' | 'COMPLETED' | 'CANCELLED', query: string): Observable<MeetingSearchDocument[]> {
    const params = new HttpParams().set('status', status).set('query', query.trim());
    return this.http.get<MeetingSearchDocument[]>(`${this.baseUrl}/meetings/by-status`, { params });
  }

  searchMeetingsByUserAndStatus(
    userId: string,
    status: 'UPCOMING' | 'COMPLETED' | 'CANCELLED',
    query: string
  ): Observable<MeetingSearchDocument[]> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('status', status)
      .set('query', query.trim());

    return this.http.get<MeetingSearchDocument[]>(
      `${this.baseUrl}/meetings/by-user-status`,
      { params }
    );
  }
}
