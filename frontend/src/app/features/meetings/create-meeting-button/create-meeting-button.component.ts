import { Component } from '@angular/core';
import { SessionService } from 'src/app/core/services/session.service';
import { MeetingCreateFormComponent } from '../meeting-create-form/meeting-create-form.component';
import { Meeting } from 'src/app/core/models/meeting.model';
import { MeetingEventsService } from 'src/app/core/services/meeting-events.service';

@Component({
  selector: 'app-create-meeting-button',
  templateUrl: './create-meeting-button.component.html',
  styleUrls: ['./create-meeting-button.component.scss']
})
export class CreateMeetingButtonComponent {
  isOpen = false;
  formComponent = MeetingCreateFormComponent;

  constructor(
    public session: SessionService,
    private meetingEvents: MeetingEventsService
  ) {}

  open() {
    if (!this.session.currentUser) return;
    this.isOpen = true;
  }

  onClosed(result?: Meeting) {
    this.isOpen = false;
    if (result) {
      this.meetingEvents.emitCreated(result);
    }
  }
}
