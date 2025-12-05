import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EventResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class EventService {
  private apiUrl = 'http://localhost:8080/api/events';

  constructor(private http: HttpClient) {}

  create(payload: any): Observable<EventResponse> {
    return this.http.post<EventResponse>(this.apiUrl, payload);
  }

  list(): Observable<EventResponse[]> {
    return this.http.get<EventResponse[]>(this.apiUrl);
  }
}

