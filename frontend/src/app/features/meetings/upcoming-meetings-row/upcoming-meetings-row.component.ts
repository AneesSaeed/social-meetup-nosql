import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { Meeting } from 'src/app/core/models/meeting.model';
import { MeetingApi } from 'src/app/core/api/meeting.api';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { MeetingDetailsComponent } from '../meeting-details/meeting-details.component';
import { SessionService } from 'src/app/core/state/session.service';

import { FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-upcoming-meetings-row',
  templateUrl: './upcoming-meetings-row.component.html',
  styleUrls: ['./upcoming-meetings-row.component.scss']
})
export class UpcomingMeetingsRowComponent implements OnInit, OnDestroy {
  meetings: Meeting[] = [];
  loading = false;
  errorMsg = '';

  interestQuery = new FormControl<string>('', { nonNullable: true });
  private lastMode: 'upcoming' | 'search' = 'upcoming';

  // modal
  isOpen = false;
  modalTitle = 'Meeting details';
  modalComponent = MeetingDetailsComponent;
  modalData: any = null;

  private sub = new Subscription();

  constructor(
    private meetingApi: MeetingApi,
    private meetingEvents: MeetingEventsService,
    public session: SessionService,
  ) {}

  ngOnInit(): void {
    this.loadUpcoming();

    this.sub.add(
      this.interestQuery.valueChanges
        .pipe(debounceTime(250), distinctUntilChanged())
        .subscribe((q) => this.applyInterestFilter(q))
    );

    this.sub.add(
      this.meetingEvents.created$.subscribe((created) => {
        if (this.interestQuery.value.trim()) return; // don’t inject into filtered list
        this.meetings = [...this.meetings, created].sort((a, b) => (a.date > b.date ? 1 : -1));
      })
    );


    this.sub.add(
      this.meetingEvents.updated$.subscribe((updated) => {
        const idx = this.meetings.findIndex(m => m.id === updated.id);
        if (idx === -1) return;

        const next = [...this.meetings];
        next[idx] = updated;
        this.meetings = next;
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
        // optional: sort by date ascending
        this.meetings = [...list].sort((a, b) => (a.date > b.date ? 1 : -1));
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Failed to load upcoming meetings';
        this.loading = false;
      }
    });
  }

  private applyInterestFilter(q: string) {
    const query = q.trim();

    if (!query) {
      this.lastMode = 'upcoming';
      this.loadUpcoming();
      return;
    }

    this.lastMode = 'search';
    this.loading = true;
    this.errorMsg = '';

    this.meetingApi.searchByInterest(query).subscribe({
      next: (list) => {
        // keep only upcoming if you want this section to stay "Upcoming"
        const upcomingOnly = list.filter(m => (m as any).completion
          ? (m as any).completion === 'UPCOMING'
          : true
        );

        this.meetings = [...upcomingOnly].sort((a, b) => (a.date > b.date ? 1 : -1));
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Failed to search meetings by interest';
        this.loading = false;
      }
    });
  }

  isJoined(m: Meeting): boolean {
    const uid = this.session.currentUser?.id;
    return !!uid && m.participants?.includes(uid);
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
