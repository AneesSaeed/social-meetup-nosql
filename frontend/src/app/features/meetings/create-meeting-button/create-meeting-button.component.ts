import { Component, EventEmitter, Output, ChangeDetectionStrategy } from '@angular/core';
import { SessionService } from 'src/app/core/state/session.service';

@Component({
    selector: 'app-create-meeting-button',
    templateUrl: './create-meeting-button.component.html',
    styleUrls: ['./create-meeting-button.component.scss'],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: true
})
export class CreateMeetingButtonComponent {
  @Output() clicked = new EventEmitter<void>();

  constructor(public session: SessionService) {}

  onClick() {
    if (!this.session.isLoggedIn()) return;
    this.clicked.emit();
  }
}
