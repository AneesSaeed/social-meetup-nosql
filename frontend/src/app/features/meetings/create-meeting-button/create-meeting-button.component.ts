import { Component, EventEmitter, Output } from '@angular/core';
import { SessionService } from 'src/app/core/services/session.service';

@Component({
  selector: 'app-create-meeting-button',
  templateUrl: './create-meeting-button.component.html',
  styleUrls: ['./create-meeting-button.component.scss']
})
export class CreateMeetingButtonComponent {
  @Output() clicked = new EventEmitter<void>();

  constructor(public session: SessionService) {}

  onClick() {
    if (!this.session.isLoggedIn()) return;
    this.clicked.emit();
  }
}
