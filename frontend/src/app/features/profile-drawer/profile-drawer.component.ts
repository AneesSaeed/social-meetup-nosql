import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { catchError, filter, Observable, of, Subscription, switchMap } from 'rxjs';
import { SessionService } from 'src/app/core/state/session.service';
import { User } from 'src/app/core/models/user.model';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { UserApi } from 'src/app/core/api/user.api';

@Component({
  selector: 'app-profile-drawer',
  templateUrl: './profile-drawer.component.html',
  styleUrls: ['./profile-drawer.component.scss']
})
export class ProfileDrawerComponent implements OnInit, OnDestroy {
  @Input() open = false;
  @Output() close = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  user$: Observable<User | null> = this.session.user$;

  private sub = new Subscription();

  constructor(
    private session: SessionService,
    private meetingEvents: MeetingEventsService,
    private userApi: UserApi
  ) {}

  ngOnInit(): void {
    this.sub.add(
      this.meetingEvents.updated$.pipe(
        // Only react when a meeting becomes COMPLETED
        filter(m => m.status === 'COMPLETED'),

        // We need a logged-in user id to refresh score
        filter(() => !!this.session.currentUser?.id),

        // fetch latest user from Mongo (has totalPoints / totalMeetings)
        switchMap(() =>
          this.userApi.getById(this.session.currentUser!.id).pipe(
            // ignore errors but keep listening
            catchError(() => of(null))
          )
        )
      ).subscribe((fresh) => {
        if (!fresh) return;

        // patch session user so drawer updates immediately
        this.session.updateCurrentUser({
          totalPoints: fresh.totalPoints ?? 0,
          totalMeetings: fresh.totalMeetings ?? 0,
        });
      })
    );
  }

  ngOnDestroy(): void {
    // Stop listening to meeting events when component is destroyed
    this.sub.unsubscribe();
  }
}
