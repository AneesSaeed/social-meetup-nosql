import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { Meeting } from 'src/app/core/models/meeting.model';
import { MeetingService } from 'src/app/core/services/meeting.service';
import { MeetingEventsService } from 'src/app/core/services/meeting-events.service';
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
    private meetingService: MeetingService,
    private meetingEvents: MeetingEventsService
  ) {}

  ngOnInit(): void {
    this.loadUpcoming();

    // refresh row when a meeting is created
    this.sub.add(
      this.meetingEvents.created$.subscribe(() => this.loadUpcoming())
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  loadUpcoming() {
    this.loading = true;
    this.errorMsg = '';

    this.meetingService.getAll('UPCOMING').subscribe({
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

  onModalClosed(result?: any) {
    this.isOpen = false;

    // If details component returns something, you can refresh.
    // (Also useful after join/leave)
    if (result?.refresh) {
      this.loadUpcoming();
    }
  }
}
