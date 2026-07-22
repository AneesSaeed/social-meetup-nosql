import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

export interface UpdateUserProfilePayload {
  bio?: string;
  interests?: string[];
}

@Injectable({ providedIn: 'root' })
export class UserApi {
  private readonly baseUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}


  // Returns the full User from Mongo
  getById(id: string): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  // Backend should return the updated full User
  updateProfile(id: string, payload: UpdateUserProfilePayload): Observable<User> {
    return this.http.patch<User>(`${this.baseUrl}/${id}`, payload);
  }
}
