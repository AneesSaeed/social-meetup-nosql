import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Meeting } from '../models/meeting.model';

// Simple app-wide stream for "meeting created/updated" events.
@Injectable({ providedIn: 'root' })
export class MeetingEventsService {
  private createdSubject = new Subject<Meeting>();
  created$ = this.createdSubject.asObservable();

  emitCreated(meeting: Meeting) {
    this.createdSubject.next(meeting);
  }
}
