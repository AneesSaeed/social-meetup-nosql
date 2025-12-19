import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { Meeting } from 'src/app/core/models/meeting.model';
import { MeetingApi } from 'src/app/core/api/meeting.api';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { MeetingDetailsComponent } from '../meeting-details/meeting-details.component';

@Component({
  selector: 'app-upcoming-meetings-row',
  templateUrl: './upcoming-meetings-row.component.html',
  styleUrls: ['./upcoming-meetings-row.component.scss']
})
export class UpcomingMeetingsRowComponent implements OnInit, OnDestroy {
  meetings: Meeting[] = [];
  loading = false;
  errorMsg = '';

  // modal
  isOpen = false;
  modalTitle = 'Meeting details';
  modalComponent = MeetingDetailsComponent;
  modalData: any = null;

  private sub = new Subscription();

  constructor(
    private meetingApi: MeetingApi,
    private meetingEvents: MeetingEventsService
  ) {}

  ngOnInit(): void {
    this.loadUpcoming();

    this.sub.add(
      this.meetingEvents.created$.subscribe((created) => {
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

  openDetails(meeting: Meeting) {
    this.modalTitle = meeting.title || 'Meeting details';
    this.modalData = { meeting };
    this.isOpen = true;
  }

  onModalClosed() {
    this.isOpen = false;
  }
}
