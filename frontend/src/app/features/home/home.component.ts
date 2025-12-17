import { Component } from '@angular/core';
import { SessionService } from 'src/app/core/services/session.service';
import { MeetingCreateFormComponent } from '../meetings/meeting-create-form/meeting-create-form.component';
import { Meeting } from 'src/app/core/models/meeting.model';
import { MeetingEventsService } from 'src/app/core/services/meeting-events.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  isCreateOpen = false;

  modalTitle = 'Create meeting';
  modalComponent = MeetingCreateFormComponent;

  constructor(
    public session: SessionService,
    private meetingEvents: MeetingEventsService
  ) {}

  openCreate() {
    if (!this.session.currentUser) return;
    this.isCreateOpen = true;
  }

  onCreateClosed(result?: Meeting) {
    this.isCreateOpen = false;
    if (result) this.meetingEvents.emitCreated(result);
  }
}
