import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Meeting } from '../models/meeting.model';

@Injectable({ providedIn: 'root' })
export class MeetingEventsService {
  private createdSubject = new Subject<Meeting>();
  created$ = this.createdSubject.asObservable();

  private updatedSubject = new Subject<Meeting>();
  updated$ = this.updatedSubject.asObservable();

  emitCreated(meeting: Meeting) {
    this.createdSubject.next(meeting);
  }

  emitUpdated(meeting: Meeting) {
    this.updatedSubject.next(meeting);
  }
}
