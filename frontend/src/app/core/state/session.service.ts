// Handles storing and retrieving the logged-in user
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class SessionService {
  // Holds the current user and emits updates
  private userSubject = new BehaviorSubject<User | null>(this.loadUser());

  // Observable for components to react to user changes
  user$ = this.userSubject.asObservable();

  // Load user from localStorage on startup
  private loadUser(): User | null {
    const data = localStorage.getItem('user');
    return data ? JSON.parse(data) : null;
  }

  // Save user to localStorage and update stream
  setUser(user: User) {
    localStorage.setItem('user', JSON.stringify(user));
    this.userSubject.next(user);
  }

  // Update fields for the currently logged-in user
  updateCurrentUser(
    patch: Partial<{ totalPoints: number; totalMeetings: number }>
  ) {
    const u = this.currentUser;
    if (!u) return;

    const updated = { ...u, ...patch };
    this.setUser(updated);
  }

  // Remove user from localStorage and reset stream
  clearUser() {
    localStorage.removeItem('user');
    this.userSubject.next(null);
  }

  // Synchronous access to current user
  get currentUser(): User | null {
    return this.userSubject.value;
  }

  // Check if a user is logged in
  isLoggedIn(): boolean {
    return !!this.userSubject.value;
  }
}
