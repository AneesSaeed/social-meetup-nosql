import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface RecommendationDto {
  userId: string;
  userName: string;
  mutualFriends: number;
}

export interface NetworkDto {
  userId: string;
  userName: string;
  distance: number; // 1..3
}


@Injectable({ providedIn: 'root' })
export class SocialGraphApi {
  private readonly baseUrl = 'http://localhost:8080/api/social';

  constructor(private http: HttpClient) {}

  recommendations(userId: string): Observable<RecommendationDto[]> {
    return this.http.get<RecommendationDto[]>(`${this.baseUrl}/recommendations/${userId}`);
  }

     network(userId: string): Observable<NetworkDto[]> {
    return this.http.get<NetworkDto[]>(`${this.baseUrl}/network/${userId}`);
  }
}
