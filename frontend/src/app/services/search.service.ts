import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private apiUrl = 'http://localhost:8080/api/search';

  constructor(private http: HttpClient) {}

  searchUsers(q: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/users`, { params: { q } });
  }
}

