import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Subscription, debounceTime, distinctUntilChanged } from 'rxjs';
import { MeetingApi } from 'src/app/core/api/meeting.api';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { Meeting, Completion } from 'src/app/core/models/meeting.model';
import { MeetingDetailsComponent } from '../meeting-details/meeting-details.component';
import { ToastService } from 'src/app/shared/toast/toast.service';
import { SearchApi } from 'src/app/core/api/search.api';
import { MeetingSearchDocument } from 'src/app/core/models/meeting-search.model';

@Component({
  selector: 'app-past-meeting-panel',
  templateUrl: './past-meetings-panel.component.html',
  styleUrls: ['./past-meetings-panel.component.scss']
})
export class PastMeetingsPanelComponent implements OnInit, OnDestroy {
  meetings: Meeting[] = [];
  loading = false;
  errorMsg = '';

  // filter
  searchQuery = new FormControl<string>('', { nonNullable: true });

  // modal
  isOpen = false;
  modalTitle = 'Meeting details';
  modalComponent = MeetingDetailsComponent;
  modalData: any = null;

  private sub = new Subscription();

  constructor(
    private meetingApi: MeetingApi,
    private meetingEvents: MeetingEventsService,
    private toast: ToastService,
    private searchApi: SearchApi
  ) {}

  ngOnInit(): void {
    this.loadPast();

    this.sub.add(
      this.searchQuery.valueChanges
        .pipe(debounceTime(300), distinctUntilChanged())
        .subscribe(() => this.applyFilter())
    );

    this.sub.add(
      this.meetingEvents.updated$.subscribe(() => {
        // reload base list then apply current filter
        this.loadPast();
      })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  loadPast(): void {
    this.loading = true;
    this.errorMsg = '';

    const status: Completion = 'COMPLETED';

    this.meetingApi.getAll(status).subscribe({
      next: (list) => {
        this.meetings = [...(list ?? [])].sort((a, b) =>
          a.date < b.date ? 1 : -1
        );
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Failed to load past meetings';
        this.toast.error(this.errorMsg);
        this.loading = false;
      }
    });
  }

  private applyFilter() {
    const qRaw = this.searchQuery.value;
    const q = (qRaw || '').trim();

    if (!q) {
      // no query: keep existing sorted list
      return;
    }

    this.loading = true;
    this.searchApi.searchMeetingsByStatus('COMPLETED', q).subscribe({
      next: (results: MeetingSearchDocument[]) => {
        const mapped: Meeting[] = (results || []).map(r => ({
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
          status: 'COMPLETED' as Completion,
          points: r.points ?? 0,
          createdAt: r.createdAt || ''
        })).sort((a, b) => (a.date < b.date ? 1 : -1));
        this.meetings = mapped;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.toast.error('Search failed, showing default list');
        this.loading = false;
      }
    });
  }

  openDetails(meeting: Meeting): void {
    this.modalTitle = meeting.title || 'Meeting details';
    this.modalData = { meeting, readonly: true };
    this.isOpen = true;
  }

  onModalClosed(): void {
    this.isOpen = false;
  }

  trackById(_: number, m: Meeting) {
    return m.id;
  }
}
