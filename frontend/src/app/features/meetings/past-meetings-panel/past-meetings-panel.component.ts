import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { MeetingApi } from 'src/app/core/api/meeting.api';
import { MeetingEventsService } from 'src/app/core/events/meeting-events.service';
import { Meeting, Completion } from 'src/app/core/models/meeting.model';
import { MeetingDetailsComponent } from '../meeting-details/meeting-details.component';
import { ToastService } from 'src/app/shared/toast/toast.service';

@Component({
  selector: 'app-past-meeting-panel',
  templateUrl: './past-meetings-panel.component.html',
  styleUrls: ['./past-meetings-panel.component.scss']
})
export class PastMeetingsPanelComponent implements OnInit, OnDestroy {
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
    private meetingEvents: MeetingEventsService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.loadPast();

    this.sub.add(
      this.meetingEvents.updated$.subscribe(() => {
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
