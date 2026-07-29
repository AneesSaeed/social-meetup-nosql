import { Component, ChangeDetectionStrategy } from '@angular/core';
import { SessionService } from 'src/app/core/state/session.service';
import { MeetingCreateFormComponent } from '../meetings/meeting-create-form/meeting-create-form.component';
import { BaseModalComponent } from '../../shared/modal/base-modal.component';
import { PastMeetingsPanelComponent } from '../meetings/past-meetings-panel/past-meetings-panel.component';
import { UpcomingMeetingsRowComponent } from '../meetings/upcoming-meetings-row/upcoming-meetings-row.component';
import { SuggestionsPanelComponent } from '../suggestions/suggestions-panel/suggestions-panel.component';
import { CreateMeetingButtonComponent } from '../meetings/create-meeting-button/create-meeting-button.component';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    imports: [CreateMeetingButtonComponent, SuggestionsPanelComponent, UpcomingMeetingsRowComponent, PastMeetingsPanelComponent, BaseModalComponent]
})
export class HomeComponent {
  isCreateOpen = false;

  modalTitle = 'Create meeting';
  modalComponent = MeetingCreateFormComponent;

  constructor(
    public session: SessionService,
  ) {}

  openCreate() {
    if (!this.session.currentUser) return;
    this.isCreateOpen = true;
  }

  onCreateClosed() {
    this.isCreateOpen = false;
  }
}
