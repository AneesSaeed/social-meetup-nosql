// stores logged user
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { User } from '../models/user.model';

// This makes the service available everywhere in the app.
// Angular will create only ONE instance of this service (singleton).
@Injectable({
  providedIn: 'root'
})
export class SessionService {

  // BehaviorSubject stores a value and allows components to listen for changes.
  // It starts with "null" because no user is logged in initially.
  private userSubject = new BehaviorSubject<User | null>(null);

  // Expose the BehaviorSubject as an observable so components can subscribe.
  // Components will be notified whenever the user changes.
  user$ = this.userSubject.asObservable(); // the $ in user$ is a naming convention, The $ tells anyone reading the code that user$ is an observable stream, not a normal value.

  // Update the current user (e.g., after login)
  setUser(user: User) {
    this.userSubject.next(user);
  }

  // Remove the current user (e.g., after logout)
  clearUser() {
    this.userSubject.next(null);
  }

  // Get the current user value synchronously
  get currentUser(): User | null {
    return this.userSubject.value;
  }

  // Returns true if a user is logged in, false otherwise
  isLoggedIn(): boolean {
    return !!this.userSubject.value;
  }
}
