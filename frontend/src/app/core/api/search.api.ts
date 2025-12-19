import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserSearchDocument } from '../models/user-search.model';

@Injectable({ providedIn: 'root' })
export class SearchApi {
  private readonly baseUrl = 'http://localhost:8080/api/search';

  constructor(private http: HttpClient) {}

  searchUsers(query: string): Observable<UserSearchDocument[]> {
    const params = new HttpParams().set('query', query.trim());
    return this.http.get<UserSearchDocument[]>(`${this.baseUrl}/users`, { params });
  }

  searchByInterests(interests: string[]): Observable<UserSearchDocument[]> {
    let params = new HttpParams();
    interests
      .map(i => i.trim())
      .filter(Boolean)
      .forEach(i => (params = params.append('interests', i)));

    return this.http.get<UserSearchDocument[]>(`${this.baseUrl}/users/by-interests`, { params });
  }

  searchByInterestsAll(interests: string[]): Observable<UserSearchDocument[]> {
    let params = new HttpParams();
    interests
      .map(i => i.trim())
      .filter(Boolean)
      .forEach(i => (params = params.append('interests', i)));

    return this.http.get<UserSearchDocument[]>(`${this.baseUrl}/users/by-interests-all`, { params });
  }
}
