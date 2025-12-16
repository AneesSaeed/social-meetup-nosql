import { Component, Inject } from '@angular/core';
import { Meeting } from 'src/app/core/models/meeting.model';
import { MeetingService } from 'src/app/core/services/meeting.service';
import { SessionService } from 'src/app/core/services/session.service';
import { MODAL_DATA } from 'src/app/shared/modal/modal.tokens';
import { ModalRef } from 'src/app/shared/modal/modal-ref';

type MeetingDetailsData = { meeting: Meeting };

@Component({
  selector: 'app-meeting-details',
  templateUrl: './meeting-details.component.html',
  styleUrls: ['./meeting-details.component.scss']
})
export class MeetingDetailsComponent {
  meeting: Meeting;
  busy = false;
  errorMsg = '';

  constructor(
    @Inject(MODAL_DATA) data: MeetingDetailsData,
    private modalRef: ModalRef,
    private meetingService: MeetingService,
    public session: SessionService
  ) {
    this.meeting = data.meeting;
  }

  get userId(): string | null {
    return this.session.currentUser?.id ?? null;
  }

  get isParticipant(): boolean {
    return !!this.userId && this.meeting.participants.includes(this.userId);
  }

  get isFull(): boolean {
    return this.meeting.maxParticipants > 0 &&
      this.meeting.participants.length >= this.meeting.maxParticipants;
  }

  join() {
    if (!this.userId || this.busy || this.isParticipant || this.isFull) return;

    this.busy = true;
    this.errorMsg = '';

    this.meetingService.join(this.meeting.id, this.userId).subscribe({
      next: (updated) => {
        this.meeting = updated;
        this.busy = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Join failed';
        this.busy = false;
      }
    });
  }

  leave() {
    if (!this.userId || this.busy || !this.isParticipant) return;

    this.busy = true;
    this.errorMsg = '';

    this.meetingService.leave(this.meeting.id, this.userId).subscribe({
      next: (updated) => {
        this.meeting = updated;
        this.busy = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Leave failed';
        this.busy = false;
      }
    });
  }

  close(refresh = false) {
    this.modalRef.close({ refresh });
  }
}
