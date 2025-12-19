import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Subscription, debounceTime, distinctUntilChanged } from 'rxjs';
import { Meeting } from 'src/app/core/models/meeting.model';
import { MeetingApi } from 'src/app/core/api/meeting.api';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { MeetingDetailsComponent } from '../meeting-details/meeting-details.component';
import { SessionService } from 'src/app/core/state/session.service';

@Component({
  selector: 'app-upcoming-meetings-row',
  templateUrl: './upcoming-meetings-row.component.html',
  styleUrls: ['./upcoming-meetings-row.component.scss']
})
export class UpcomingMeetingsRowComponent implements OnInit, OnDestroy {
  // source list (always UPCOMING)
  private allMeetings: Meeting[] = [];

  // displayed list (filtered)
  meetings: Meeting[] = [];

  loading = false;
  errorMsg = '';

  // filter
  interestQuery = new FormControl<string>('', { nonNullable: true });

  // modal
  isOpen = false;
  modalTitle = 'Meeting details';
  modalComponent = MeetingDetailsComponent;
  modalData: any = null;

  private sub = new Subscription();

  constructor(
    private meetingApi: MeetingApi,
    private meetingEvents: MeetingEventsService,
    public session: SessionService
  ) {}

  ngOnInit(): void {
    this.loadUpcoming();

    this.sub.add(
      this.interestQuery.valueChanges
        .pipe(debounceTime(250), distinctUntilChanged())
        .subscribe(() => this.applyLocalFilter())
    );

    this.sub.add(
      this.meetingEvents.created$.subscribe((created) => {
        if (created.status !== 'UPCOMING') return;

        this.allMeetings = [...this.allMeetings, created].sort((a, b) => (a.date > b.date ? 1 : -1));
        this.applyLocalFilter();
      })
    );

    this.sub.add(
      this.meetingEvents.updated$.subscribe((updated) => {
        // remove old
        this.allMeetings = this.allMeetings.filter(m => m.id !== updated.id);

        // re-add if still UPCOMING
        if (updated.status === 'UPCOMING') {
          this.allMeetings = [...this.allMeetings, updated].sort((a, b) => (a.date > b.date ? 1 : -1));
        }

        this.applyLocalFilter();
      })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  loadUpcoming() {
    this.loading = true;
    this.errorMsg = '';

    this.meetingApi.getAll('UPCOMING').subscribe({
      next: (list) => {
        const sorted = [...list].sort((a, b) => (a.date > b.date ? 1 : -1));
        this.allMeetings = sorted;
        this.applyLocalFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Failed to load upcoming meetings';
        this.loading = false;
      }
    });
  }

  private applyLocalFilter() {
    const q = this.interestQuery.value.trim().toLowerCase();

    if (!q) {
      this.meetings = [...this.allMeetings];
      return;
    }

    this.meetings = this.allMeetings.filter(m =>
      (m.interests ?? []).some(it => it.toLowerCase().includes(q))
    );
  }

  isJoined(m: Meeting): boolean {
    const uid = this.session.currentUser?.id;
    return !!uid && (m.participants ?? []).includes(uid);
  }

  isFull(m: Meeting): boolean {
    return m.maxParticipants > 0 && (m.participants?.length ?? 0) >= m.maxParticipants;
  }

  openDetails(meeting: Meeting) {
    this.modalTitle = meeting.title || 'Meeting details';
    this.modalData = { meeting };
    this.isOpen = true;
  }

  onModalClosed() {
    this.isOpen = false;
  }
}
