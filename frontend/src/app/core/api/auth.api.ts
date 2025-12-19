// calls backend login/register
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

// @Injectable: Makes this service available everywhere in the app.
@Injectable({
  providedIn: 'root' // 'root' mean create one single instance of this service for the entire application.
})
export class AuthApi {

  private baseUrl = 'http://localhost:8080/api';

  // HttpClient is injected so we can make HTTP requests (POST, GET, etc.)
  constructor(private http: HttpClient) {}

  // Sends a POST request to /login
  // Returns an Observable that will emit a User when the server responds
  login(email: string): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/login`, { email });
  }

  // Sends a POST request to /register
  // Returns an Observable that will emit a User
  // when the server responds
  register(name: string, email: string, bio: string, interests: string[]): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/register`, {
      name,
      email,
      bio,
      interests
    });
  }

  //“emit a User” means:
  // The observable will give you the User object inside
  // your .subscribe() once the HTTP request completes successfully.
}
