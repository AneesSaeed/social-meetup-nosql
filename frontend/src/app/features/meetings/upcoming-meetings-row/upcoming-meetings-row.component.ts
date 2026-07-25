import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Subscription, debounceTime, distinctUntilChanged } from 'rxjs';
import { Meeting } from 'src/app/core/models/meeting.model';
import { MeetingApi } from 'src/app/core/api/meeting.api';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { MeetingDetailsComponent } from '../meeting-details/meeting-details.component';
import { SessionService } from 'src/app/core/state/session.service';
import { SearchApi } from 'src/app/core/api/search.api';
import { MeetingSearchDocument } from 'src/app/core/models/meeting-search.model';
import { ColorBucketPipe } from '../../../shared/pipes/color-bucket.pipe';
import { DatePipe } from '@angular/common';
import { BaseModalComponent } from '../../../shared/modal/base-modal.component';

@Component({
    selector: 'app-upcoming-meetings-row',
    templateUrl: './upcoming-meetings-row.component.html',
    styleUrls: ['./upcoming-meetings-row.component.scss'],
    standalone: true,
    imports: [ReactiveFormsModule, BaseModalComponent, DatePipe, ColorBucketPipe]
})
export class UpcomingMeetingsRowComponent implements OnInit, OnDestroy {
  private allMeetings: Meeting[] = [];
  meetings: Meeting[] = [];
  loading = false;
  errorMsg = '';

  interestQuery = new FormControl<string>('', { nonNullable: true });

  isOpen = false;
  modalTitle = 'Meeting details';
  modalComponent = MeetingDetailsComponent;
  modalData: any = null;

  private sub = new Subscription();

  constructor(
    private meetingApi: MeetingApi,
    private meetingEvents: MeetingEventsService,
    public session: SessionService,
    private searchApi: SearchApi
  ) {}

  ngOnInit(): void {
    this.loadUpcoming();

    this.sub.add(
      this.interestQuery.valueChanges
        .pipe(debounceTime(300), distinctUntilChanged())
        .subscribe(() => this.applyFilter())
    );

    this.sub.add(
      this.meetingEvents.created$.subscribe((created) => {
        if (created.status !== 'UPCOMING') return;
        this.allMeetings = [...this.allMeetings, created].sort((a, b) => (a.date > b.date ? 1 : -1));
        this.applyFilter();
      })
    );

    this.sub.add(
      this.meetingEvents.updated$.subscribe((updated) => {
        this.allMeetings = this.allMeetings.filter(m => m.id !== updated.id);
        if (updated.status === 'UPCOMING') {
          this.allMeetings = [...this.allMeetings, updated].sort((a, b) => (a.date > b.date ? 1 : -1));
        }
        this.applyFilter();
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
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Failed to load upcoming meetings';
        this.loading = false;
      }
    });
  }

  private applyFilter() {
    const qRaw = this.interestQuery.value;
    const q = (qRaw || '').trim();
    if (!q) {
      this.meetings = [...this.allMeetings];
      return;
    }
    this.loading = true;
    this.searchApi.searchMeetingsByStatus('UPCOMING', q).subscribe({
      next: (results: MeetingSearchDocument[]) => {
        const mapped = (results || []).map(r => ({
          id: r.meetingId,
          title: r.title || '',
          description: '',
          eventType: r.eventType || '',
          date: r.date || '',
          location: r.location || '',
          organizer: r.organizer || '',
          participants: r.participants || [],
          maxParticipants: r.maxParticipants ?? 0,
          interests: r.interests || [],
          status: 'UPCOMING' as const,
          points: r.points ?? 0,
          createdAt: r.createdAt || ''
        })).sort((a, b) => (a.date > b.date ? 1 : -1));
        this.meetings = mapped;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        const ql = q.toLowerCase();
        this.meetings = this.allMeetings.filter(m => (m.interests ?? []).some(it => it.toLowerCase().includes(ql)));
        this.loading = false;
      }
    });
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
